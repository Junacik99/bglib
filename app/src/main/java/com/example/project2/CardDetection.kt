package com.example.project2

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.suspendCoroutine

class CardDetection {

    private var baseAPI : TessBaseAPI? = null

    // Draw rectangle around contours inside the input image
    private fun drawRectangle(
        frame : Mat,
        contours : MutableList<MatOfPoint>,
        retBoundingBoxes : Boolean=false,
        contourColor : Scalar=Scalar(0.0, 255.0, 0.0),
        boundingBoxColor : Scalar=Scalar(255.0, 0.0, 0.0),
    ) : MutableList<Rect>
    {
        val boundingBoxes = mutableListOf<Rect>()

        for (contour in contours) {
            // Filter out small contours that are not likely to be cards
            if (Imgproc.contourArea(contour) < 1000) continue

            // Approximate contour to a polygon
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true), true)

            // Check if polygon has 4 vertices (rectangle-like), which is likely a card
            if (approx.toArray().size == 4) {
                // Draw the contour on the original frame
                Imgproc.drawContours(frame, listOf(MatOfPoint(*approx.toArray())), -1, contourColor, 2)

                if (retBoundingBoxes) {
                    // Calculate and draw bounding rectangle
                    val rect = Imgproc.boundingRect(MatOfPoint(*approx.toArray()))
                    Imgproc.rectangle(frame, rect, boundingBoxColor, 2)
                    boundingBoxes.add(rect)
                }
            }
        }
        return boundingBoxes
    }

    /*****************************************************************************************/
    /* Rectangle detection */

    // Preprocess the input image for edge detection
    private fun preprocess(inputImg: Mat) : Mat {
        // Convert the input frame to grayscale
        val gray = Mat()
        Imgproc.cvtColor(inputImg, gray, Imgproc.COLOR_RGBA2GRAY)

        // Apply Gauss blur
        Imgproc.GaussianBlur(gray, gray, Size(5.0,5.0), 0.0)

        return gray
    }

    // Detect rectangle using Canny edge detection
    fun detectRectCanny(frame: Mat): MutableList<Rect> {
        val gray = preprocess(frame)

        // Canny edge detection
        val edges = Mat()
        Imgproc.Canny(gray, edges, 100.0, 200.0)

        // Contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        val rectangles = drawRectangle(frame, contours)

        // Release resources
        gray.release()

        return rectangles
    }

    // Detect rectangle using Otsu method
    fun detectRectOtsu(frame: Mat): MutableList<Rect> {
        // Convert to grayscale and apply Gauss blur
        val gray = preprocess(frame)

        // Apply Otsu's thresholding to segment the cards
        val binary = Mat()
        Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

        // Find contours in the binary image
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        val rectangles = drawRectangle(frame, contours, true)

        // Release resources
        gray.release()
        binary.release()

        return rectangles
    }

    /*****************************************************************************************/
    /* OCR */

    // Convert Mat to Bitmap
    fun mat2bitmap(mat: Mat): Bitmap {
        // create bitmap
        var gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY, 4)
        val bmp = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(gray, bmp)
        return bmp
    }

    fun detectText(frame: Mat, rotation: Int, textRecognizer: TextRecognizer, onResult: (String) -> Unit) {
        // create bitmap
        val bmp = mat2bitmap(frame)

        // Convert to image
        val image = InputImage.fromBitmap(bmp, rotation)

        val result = textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                onResult(visionText.text)
            }
            .addOnFailureListener { e: Exception ->
                // Task failed with an exception
                onResult("Uh oh: ${e.message}")
            }

    }

    // Suspend function wrapper to detect text
    suspend fun detectTextSuspend(frame: Mat, rotation: Int, textRecognizer: TextRecognizer): String =
        suspendCoroutine { continuation ->
            detectText(frame, rotation, textRecognizer) { text ->
                continuation.resumeWith(Result.success(text))
            }
        }


    fun initTessTwo(
        context: Context,
        dataName : String,
        lang : String,
        TAG : String): TessBaseAPI {
        // Copy tess data
        var out: OutputStream? = null
        try {
            val inputStream: InputStream = context.assets.open(dataName)
            val tessPath = "${context.getExternalFilesDir(null)}"+"/tessdata/"
            val tessFolder = File(tessPath)
            if (!tessFolder.exists()) tessFolder.mkdir()
            val tessData = "$tessPath/$dataName"
            val tessFile = File(tessData)
            if (!tessFile.exists()) {
                out = FileOutputStream(tessData)
                val buffer = ByteArray(1024)
                var read = inputStream.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = inputStream.read(buffer)
                }
                Log.d(TAG, "Finished copy tess file  ")
            } else Log.d(TAG, " tess file exist  ")
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "couldn't copy with the following error : $e")
        } finally {
            try {
                out?.close()
            } catch (exx: java.lang.Exception) {
                Log.e(TAG, "couldn't close the stream with the following error : $exx")
            }
        }

        val baseAPI = TessBaseAPI()
        val dataPath = context.getExternalFilesDir(null)?.absolutePath
        baseAPI.init(dataPath, lang)

        return baseAPI
    }

    fun detectTextTessTwo(frame: Mat, api: TessBaseAPI?, onResult: (String) -> Unit){
        // create bitmap
        val bmp = mat2bitmap(frame)

        api!!.setImage(bmp)
        val text = api.utF8Text

        onResult(text)
    }

    // TODO:
    // clustering (K means?)
    // create card class with various properties like text, picture, number, etc on different locations
    // Arrange cards in matrix
}
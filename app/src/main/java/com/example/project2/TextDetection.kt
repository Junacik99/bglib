package com.example.project2

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.suspendCoroutine

class TextDetection {

    /* OCR */
    companion object{
        // Convert Mat to Bitmap
        fun mat2bitmap(mat: Mat): Bitmap {
            // create bitmap
            var gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY, 4)
            val bmp = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(gray, bmp)
            return bmp
        }

        /* Firebase */

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


        /* Tess Two */

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

    }







}
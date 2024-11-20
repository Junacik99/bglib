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
    companion object {
        private var baseAPI : TessBaseAPI? = null

        // Draw rectangle around contours inside the input image
        private fun drawRectangle(
            frame : Mat,
            contours : MutableList<MatOfPoint>,
            drawBoundingBoxes : Boolean=false,
            drawContours : Boolean=true,
            contourColor : Scalar=Scalar(0.0, 255.0, 0.0), // Green - contours
            boundingBoxColor : Scalar=Scalar(255.0, 0.0, 0.0), // Red - bounding box
        ) : MutableList<Rect>
        {
            val boundingBoxes = mutableListOf<Rect>()

            val frameArea = frame.size().height * frame.size().width

            for (contour in contours) {
                // Filter out small and big contours that are not likely to be cards
                if (Imgproc.contourArea(contour) < frameArea * 0.02) continue
                if (Imgproc.contourArea(contour) > frameArea * 0.9) continue

                // Approximate contour to a polygon
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true), true)

                // Check if polygon has 4 vertices (rectangle-like), which is likely a card
                if (approx.toArray().size == 4) {
                    // Draw the contour on the original frame
                    if (drawContours) Imgproc.drawContours(frame, listOf(MatOfPoint(*approx.toArray())), -1, contourColor, 2)

                    // Calculate and draw bounding rectangle
                    val rect = Imgproc.boundingRect(MatOfPoint(*approx.toArray()))
                    if (drawBoundingBoxes) Imgproc.rectangle(frame, rect, boundingBoxColor, 2)
                    boundingBoxes.add(rect)

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
        fun detectRectOtsu(
            frame: Mat,
            drawBoundingBoxes: Boolean = false,
            drawContours: Boolean = false
        ): MutableList<Rect> {
            // Convert to grayscale and apply Gauss blur
            val gray = preprocess(frame)

            // Apply Otsu's thresholding to segment the cards
            val binary = Mat()
            Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

            // Find contours in the binary image
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

            val rectangles = drawRectangle(frame, contours, drawBoundingBoxes, drawContours)

            // Release resources
            gray.release()
            binary.release()

            return rectangles
        }


    }




    /*****************************************************************************************/


    // TODO:
    // clustering (K means?)
    // create card DATA class with various properties like text, picture, number, etc on different locations
    // Arrange cards in matrix
}
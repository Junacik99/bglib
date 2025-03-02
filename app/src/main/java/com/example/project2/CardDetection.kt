package com.example.project2

import android.content.Context
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CardDetection {
    companion object {
        private var baseAPI : TessBaseAPI? = null

        /*****************************************************************************************/
        /* Model Interpreter */
        class ModelInterpreter(context: Context, modelName: String, inputSize: Int = 128) {
            private var interpreter: Interpreter
            private var mInputSize: Int = 0

            init {
                val assetManager = context.assets
                val inputStream = assetManager.open(modelName)
                val buffer = inputStream.readBytes().let { ByteBuffer.allocateDirect(it.size).apply {
                    order(ByteOrder.nativeOrder())
                    put(it)
                } }
                interpreter = Interpreter(buffer)
                mInputSize = inputSize
            }

            fun predict(inputData: FloatArray): FloatArray {
                // Prepare the input buffer
                val inputBuffer = ByteBuffer.allocateDirect(inputData.size * 4) // 4 bytes per float
                inputBuffer.order(ByteOrder.nativeOrder())
                for (value in inputData) {
                    inputBuffer.putFloat(value)
                }

                // Output buffer
                val output = Array(1) { FloatArray(1) }

                // Run inference
                interpreter.run(inputBuffer, output)

                return output[0]
            }

            fun preprocessMat(mat: Mat): FloatArray {
                // Resize the Mat to the model's expected input size
                val resizedMat = Mat()
                Imgproc.resize(mat, resizedMat, Size(mInputSize.toDouble(), mInputSize.toDouble()))

                // Convert to RGB if needed
                val rgbMat = Mat()
                Imgproc.cvtColor(resizedMat, rgbMat, Imgproc.COLOR_BGR2RGB)

                // Flatten and normalize the Mat
                val floatArray = FloatArray(mInputSize * mInputSize * 3)
                val buffer = ByteBuffer.allocateDirect(mInputSize * mInputSize * 3 * 4) // 4 bytes per float
                buffer.order(ByteOrder.nativeOrder())
                rgbMat.convertTo(rgbMat, CvType.CV_32FC3, 1.0 / 255.0) // Normalize

                // Copy Mat data into the FloatArray
                rgbMat.get(0, 0, floatArray)
                return floatArray
            }
        }


        /*****************************************************************************************/
        /* Utility functions */

        fun drawRectangle(
            frame: Mat,
            rectangle: MatOfPoint2f,
            contourColor: Scalar = Scalar(0.0, 255.0, 0.0),
        ){
            Imgproc.drawContours(frame, listOf(MatOfPoint(*rectangle.toArray())), -1, contourColor, 2)
        }

        fun getBoundingBoxes(
            frame: Mat,
            rectangles: MutableList<MatOfPoint2f>,
            drawBoundingBoxes: Boolean = false,
            boundingBoxColor: Scalar = Scalar(255.0, 0.0, 0.0),
        ): MutableList<Rect> {
            val bbs = mutableListOf<Rect>()
            for (rectangle in rectangles) {
                val bb = Imgproc.boundingRect(MatOfPoint(*rectangle.toArray()))
                bbs.add(bb)
                if (drawBoundingBoxes) Imgproc.rectangle(frame, bb, boundingBoxColor, 2)
            }
            return bbs
        }


        // Draw rectangle around contours inside the input image
        private fun drawRectangleOld(
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

        // Get rectangles from contours inside the input image
        private fun getRectangles(
            frame : Mat,
            contours : MutableList<MatOfPoint>,
            filterMaxArea : Float = 0.9f,
            filterMinArea : Float = 0.02f
        ) : MutableList<MatOfPoint2f>
        {
            val frameArea = frame.size().height * frame.size().width

            val rectangles = mutableListOf<MatOfPoint2f>()

            for (contour in contours) {
                // Filter out small and big contours that are not likely to be cards
                if (Imgproc.contourArea(contour) < frameArea * filterMinArea) continue
                if (Imgproc.contourArea(contour) > frameArea * filterMaxArea) continue

                // Approximate contour to a polygon
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true), true)

                // Check if polygon has 4 vertices (rectangle-like), which is likely a card
                if (approx.toArray().size == 4) {
                    rectangles.add(approx)
                }
            }

            return rectangles
        }


        /*****************************************************************************************/
        /* Rectangle detection */

        // Preprocess the input image for edge detection
        fun grayGauss(inputImg: Mat) : Mat {
            // Convert the input frame to grayscale
            val gray = Mat()
            Imgproc.cvtColor(inputImg, gray, Imgproc.COLOR_RGBA2GRAY)

            // Apply Gauss blur
            Imgproc.GaussianBlur(gray, gray, Size(5.0,5.0), 0.0)

            return gray
        }

        // Detect rectangle using Canny edge detection
        fun detectRectCanny(
            frame: Mat,
            drawBoundingBoxes: Boolean = false,
            drawContours: Boolean = false
        ): MutableList<MatOfPoint2f> {
            if (frame.channels() != 3) Log.e("OCVSample::Activity", "Input frame must have 3 channels")

            val gray = grayGauss(frame)

            // Canny edge detection
            val edges = Mat()
            Imgproc.Canny(gray, edges, 100.0, 200.0)

            // Contours
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

            val rectangles = getRectangles(frame, contours)

            if (drawContours) {
                for (rect in rectangles) {
                    drawRectangle(frame, rect)
                }
            }

            if (drawBoundingBoxes)
                getBoundingBoxes(frame, rectangles, true)

            // Release resources
            gray.release()

            return rectangles
        }

        // Detect rectangle using Otsu method
        fun detectRectOtsu(
            frame: Mat,
            drawBoundingBoxes: Boolean = false,
            drawContours: Boolean = false
        ): MutableList<MatOfPoint2f> {
            if (frame.channels() != 3) Log.e("CardDetection", "Input frame must have 3 channels")

            // Convert to grayscale and apply Gauss blur
            val gray = grayGauss(frame)

            // Apply Otsu's thresholding to segment the cards
            val binary = Mat()
            Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

            // Find contours in the binary image
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

            val rectangles = getRectangles(frame, contours)

            if (drawContours) {
                for (rect in rectangles) {
                    drawRectangle(frame, rect)
                }
            }

            if (drawBoundingBoxes)
                getBoundingBoxes(frame, rectangles, true)

            // Release resources
            gray.release()
            binary.release()

            return rectangles
        }


    }




    /*****************************************************************************************/


    // TODO:
    // clustering (K means?)
    // add various properties like text, picture, number, etc on different locations to Card data class
}
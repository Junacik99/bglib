package com.example.project2

import android.gesture.OrientedBoundingBox
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class CardDetection {

    // Draw rectangle around contours inside the input image
    private fun drawRectangle(
        frame:Mat,
        contours:MutableList<MatOfPoint>,
        retBoundingBoxes:Boolean=false,
        contourColor:Scalar=Scalar(0.0, 255.0, 0.0),
        boundingBoxColor:Scalar=Scalar(255.0, 0.0, 0.0),
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

    private fun preprocess(inputImg: Mat) : Mat {
        // Convert the input frame to grayscale
        val gray = Mat()
        Imgproc.cvtColor(inputImg, gray, Imgproc.COLOR_RGBA2GRAY)

        // Apply Gauss blur
        Imgproc.GaussianBlur(gray, gray, Size(5.0,5.0), 0.0)

        return gray
    }

    // Detect rectangle using Canny edge detection
    fun detectRectCanny(frame: Mat) {
        val gray = preprocess(frame)

        // Canny edge detection
        val edges = Mat()
        Imgproc.Canny(gray, edges, 100.0, 200.0)

        // Contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        drawRectangle(frame, contours)

        // Release resources
        gray.release()
    }

    // Detect rectangle using Otsu method
    fun detectRectOtsu(frame: Mat) {
        // Convert to grayscale and apply Gauss blur
        val gray = preprocess(frame)

        // Apply Otsu's thresholding to segment the cards
        val binary = Mat()
        Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

        // Find contours in the binary image
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        drawRectangle(frame, contours)

        // Release resources
        gray.release()
        binary.release()
    }

    // TODO:
    // thresholding
    // clustering
}
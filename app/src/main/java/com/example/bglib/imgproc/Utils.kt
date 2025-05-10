package com.example.bglib.imgproc

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.atan2

/************************************************
 * Utility functions:                           *
 * 1. Camera permission                         *
 * 2. Save image to MediaStore                  *
 * 3. Convert Mat to Bitmap (and vice versa)    *
 * 4. Save frame(s)                             *
 * 5. Resize frames                             *
 * 6. Calculate rotation angle                  *
 * 7. Convert MatOfPoint2f to Mat               *
 * 8. Multiply Point by Mat                     *
 * 9. Get largest rectangle                     *
 ***********************************************/

// Check if camera permission is granted
fun checkCamPermission(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        // Log.d(TAG, "Permission was not granted yet")
        return false
    }
    return true
}

// Save image to MediaStore (Doesn't require permission)
fun saveImageToMediaStore(context: Context, bitmap: Bitmap, displayName: String, mimeType: String): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "CardDetector")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val imageUri = context.contentResolver.insert(collection, values)

    imageUri?.let {
        context.contentResolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(it, values, null, null)
    }

    return imageUri
}

fun mat2bitmap(frame: Mat): Bitmap {
    val bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(frame, bitmap)
    return bitmap
}

fun bitmap2mat(bitmap: Bitmap): Mat {
    val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4) // 4 channels for RGBA
    Utils.bitmapToMat(bitmap, mat)
    return mat
}

// Save frame
fun saveFrame(frame: Mat, context: Context){
    // Convert to bitmap
    val bitmap = mat2bitmap(frame)

    val displayName = "card_${System.currentTimeMillis()}.png"
    val mimeType = "image/png"

    val imageUri = saveImageToMediaStore(context, bitmap, displayName, mimeType)

    if (imageUri != null) {
        // Image saved successfully
        // Log.d(TAG, "Image saved successfully")
        Toast.makeText(context, "Image saved!", Toast.LENGTH_LONG).show()

    } else {
        // Error saving image
        // Log.e(TAG, "Error saving image")
        Toast.makeText(context, "Error saving image!", Toast.LENGTH_LONG).show()
    }
}

// Save multiple frames
fun saveFrames(frames: MutableList<Mat>, context: Context){
    if (frames.isNotEmpty()){
        for (frame in frames){
            saveFrame(frame, context)
        }
    }
}

fun resizeFrames(frames: MutableList<Mat>, width: Int, height: Int): MutableList<Mat> {
    var resizedFrames = mutableListOf<Mat>()
    for (frame in frames){
        val resizedFrame = Mat()
        Imgproc.resize(frame, resizedFrame, Size(width.toDouble(), height.toDouble()), 0.0, 0.0, Imgproc.INTER_AREA)
        resizedFrames.add(resizedFrame)
    }
    return resizedFrames
}

fun getLargestRect(rects: MutableList<Rect>):Rect? {
    var largestRect: Rect? = null
    var largestArea = 0

    for (rect in rects) {
        val area = rect.width * rect.height
        if (area > largestArea) {
            largestArea = area
            largestRect = rect
        }
    }
    return largestRect
}

fun getSmallestRect(rects: MutableList<Rect>):Rect? {
    var smallestRect: Rect? = null
    var smallestArea = 0

    for (rect in rects) {
        val area = rect.width * rect.height
        if (area > smallestArea) {
            smallestArea = area
            smallestRect = rect
        }
    }
    return smallestRect
}

// Calculate rotation angle of the rectangle
fun getRotationAngle(cornerPoints: MatOfPoint2f? = null): Double {
    if (cornerPoints != null && cornerPoints.toArray().size == 4) {
        // Calculate angle from corner points
        val points = cornerPoints.toArray()
        val dx = points[1].x - points[0].x
        val dy = points[1].y - points[0].y
        return Math.toDegrees(atan2(dy, dx))
    } else {
        return 0.0
    }
}

fun matOfPoint2ftoMat(matOfPoint2f: MatOfPoint2f): Mat {
    // Get the array of Point objects from MatOfPoint2f
    val points = matOfPoint2f.toArray()

    // Determine the number of points
    val numPoints = points.size

    // Create a Mat with the appropriate Dimensions and type
    val mat = Mat(numPoints, 1, CvType.CV_32FC2)

    // Iterate through the points and populate the Mat
    for (i in points.indices) {
        // Get the data array for the current row
        val data = FloatArray(2)
        data[0] = points[i].x.toFloat()
        data[1] = points[i].y.toFloat()

        // Put the data into the Mat
        mat.put(i, 0, data)
    }

    return mat
}


fun mulPointbyMat(point: Point, mat: Mat): Point {
    // Ensure the transformation matrix is a 2x3
    if (mat.rows() != 2 || mat.cols() != 3) {
        throw IllegalArgumentException("Transformation matrix must be 2x3.")
    }

    // Ensure the transformation matrix is of type CV_64F
    if (mat.type() != CvType.CV_64F) {
        throw IllegalArgumentException("Transformation matrix type must be CV_64F.")
    }

    // 1. Create a 3x1 matrix (column vector) to represent the point in homogeneous coordinates
    val pointMat = Mat(3, 1, CvType.CV_64F)

    // 2. Put the point's x, y, and the homogeneous coordinate (1.0) into the matrix
    pointMat.put(0, 0, point.x)
    pointMat.put(1, 0, point.y)
    pointMat.put(2, 0, 1.0)

    // 3. Perform the matrix multiplication
    val transformMat = Mat(3, 1, CvType.CV_64F)
    val resultMat = Mat(2, 1, CvType.CV_64F)

    Core.gemm(mat, pointMat, 1.0, Mat(), 0.0, resultMat)

    val transformedData = DoubleArray(2)

    resultMat.get(0, 0, transformedData)

    val transformedX = transformedData[0]
    val transformedY = transformedData[1]

    pointMat.release()
    transformMat.release()
    resultMat.release()

    return Point(transformedX, transformedY)
}

package com.example.project2

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File

class Utils {
    companion object {

        // Check if camera permission is granted
        fun checkCamPermission(context: Context): Boolean {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Log.d(TAG, "Permission was not granted yet")
                return false
            }
            return true
        }

        // Save image to MediaStore (Doesn't require permission)
        @RequiresApi(Build.VERSION_CODES.Q)
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
        @RequiresApi(Build.VERSION_CODES.Q)
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
        @RequiresApi(Build.VERSION_CODES.Q)
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

    }
}
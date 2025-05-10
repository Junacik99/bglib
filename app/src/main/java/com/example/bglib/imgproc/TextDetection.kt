package com.example.bglib.imgproc

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.CameraActivity.CAMERA_SERVICE
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.suspendCoroutine


/************************************************
 * OCR methods:                                 *
 * 1. Tesseract                                 *
 * 2. Firebase ML Kit                           *
 ***********************************************/

/* OCR */
/* Firebase */

// Get the angle by which an image must be rotated given the device's current orientation
@Throws(CameraAccessException::class)
fun getRotationCompensation(cameraId: String, activity: Activity, isFrontFacing: Boolean): Int {
    // Get the device's current rotation relative to its "native" orientation.
    val orientations = SparseIntArray()
    orientations.append(Surface.ROTATION_0, 0)
    orientations.append(Surface.ROTATION_90, 90)
    orientations.append(Surface.ROTATION_180, 180)
    orientations.append(Surface.ROTATION_270, 270)
    val deviceRotation = activity.windowManager.defaultDisplay.rotation
    var rotationCompensation = orientations.get(deviceRotation)

    // Get the device's sensor orientation.
    val cameraManager = activity.getSystemService(CAMERA_SERVICE) as CameraManager
    val sensorOrientation = cameraManager
        .getCameraCharacteristics(cameraId)
        .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    if (isFrontFacing) {
        rotationCompensation = (sensorOrientation + rotationCompensation) % 360
    } else { // back-facing
        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
    }
    return rotationCompensation
}

fun detectTextMLKit(frame: Mat, rotation: Int, textRecognizer: TextRecognizer, onResult: (Text) -> Unit) {
    var gray = Mat()
    Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY, 4)

    // create bitmap
    val bmp = mat2bitmap(gray)

    // Convert to image
    val image = InputImage.fromBitmap(bmp, rotation)

    val result = textRecognizer.process(image)
        .addOnSuccessListener { visionText ->
            // Task completed successfully
            // onResult(visionText.text)
            onResult(visionText)
        }
        .addOnFailureListener { e: Exception ->
            // Task failed with an exception
            onResult(Text("Uh oh: ${e.message}", listOf(1)))
        }

}

// Suspend function wrapper to detect text
suspend fun detectTextSuspend(frame: Mat, rotation: Int, textRecognizer: TextRecognizer): String =
    suspendCoroutine { continuation ->
        detectTextMLKit(frame, rotation, textRecognizer) { text ->
            continuation.resumeWith(Result.success(text.text))
        }
    }


/* Tesseract */

fun initTess(
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

fun detectTextTess(frame: Mat, api: TessBaseAPI?, onResult: (String) -> Unit){
    // create bitmap
    val bmp = mat2bitmap(frame)

    api!!.setImage(bmp)
    val text = api.utF8Text

    onResult(text)
}

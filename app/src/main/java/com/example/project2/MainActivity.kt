package com.example.project2

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import androidx.core.content.ContextCompat as ContextCompat1


class MainActivity : CameraActivity(), CvCameraViewListener2 {

    private val TAG = "OCVSample::Activity"
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private val cd = CardDetection()
    lateinit var textView: TextView
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val CAMERA_ID = "0"
    private val coroutineScope = MainScope()
    private var baseAPI : TessBaseAPI? = null

    private val ORIENTATIONS = SparseIntArray()
    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

    // Get the angle by which an image must be rotated given the device's current orientation
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(cameraId: String, activity: Activity, isFrontFacing: Boolean): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

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

    private fun initCamera(){
        mOpenCvCameraView = findViewById(R.id.surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        Log.i(TAG, "OpenCV Camera initialized")
    }

    private fun checkCamPermission(): Boolean {
        if (ContextCompat1.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission was not granted yet")
            return false
        }
        return true
    }


    init {
        Log.i(TAG, "Instantiated new $javaClass")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "called onCreate")

        // init view
        setContentView(R.layout.activity_main)

        // Text view bind
        textView = findViewById(R.id.cardText)

        // add FLAG_KEEP_SCREEN_ON
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // init camera
        initCamera()

        if (checkCamPermission()) {
            Log.d(TAG, "Permissions granted")
            mOpenCvCameraView.setCameraPermissionGranted()

            // Init Opencv
            if (OpenCVLoader.initLocal()) {
                Log.i(TAG, "OpenCV loaded successfully")
            } else {
                Log.e(TAG, "OpenCV initialization failed!")
                Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
                return
            }

        } else {
            // request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        // Tess two: Copy tess data
        val language = "slk"
        val dataName = "$language.traineddata"
        baseAPI = cd.initTessTwo(this, dataName, language, TAG)


    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        mOpenCvCameraView.enableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
        coroutineScope.cancel()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()

        val rectangles = cd.detectRectOtsu(frame)

        val rotation = getRotationCompensation(CAMERA_ID, this, false)

        cd.detectTextTessTwo(frame, baseAPI) { detectedText ->
            runOnUiThread {
                textView.text = detectedText
            }
        } // TODO: this doesn't work
        // Loads only the first frame and then freezes

//        coroutineScope.launch {
//            try {
//                val cardTextBuilder = StringBuilder()
//                for (rect in rectangles) {
//                    val subframe = frame.submat(rect)
//                    val detectedText = withContext(Dispatchers.IO) {
//                        cd.detectTextSuspend(subframe, rotation, recognizer)
//                    }
//                    cardTextBuilder.append(detectedText).append("\n")
//
//                }
//                withContext(Dispatchers.Main) {
////                    Log.d(TAG, "Card text: ${cardTextBuilder.toString()}")
//                    textView.text = cardTextBuilder.toString()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error during text recognition: ${e.message}")
//                // Handle the error
//            }
//        }
        // TODO: this works better than coroutines and rects
        // Possible solution: use coordinates of found text to assign to found card
//        cd.detectText(frame, rotation, recognizer) { detectedText ->
//            textView.text = detectedText
//        }

        return frame
    }


}


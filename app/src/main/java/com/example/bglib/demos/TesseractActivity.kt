package com.example.bglib.demos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bglib.R
import com.example.bglib.imgproc.detectRectOtsu
import com.example.bglib.imgproc.detectTextTess
import com.example.bglib.imgproc.initTess
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
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
import androidx.core.content.ContextCompat as ContextCompat1

/************************************************
 * OCR demo activity.                           *
 * Using Tesseract models.                      *
 ***********************************************/
class TesseractActivity : CameraActivity(), CvCameraViewListener2 {

    private val TAG = "Tesseract::Activity"
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    lateinit var textView: TextView
    private val coroutineScope = MainScope()
    private var baseAPI : TessBaseAPI? = null

    private val ORIENTATIONS = SparseIntArray()
    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
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
        setContentView(R.layout.activity_detect_text)

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
        val language = "slk4"
        val dataName = "$language.traineddata"
        baseAPI = initTess(this, dataName, language, TAG)


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

        // Object Detection using Coroutines
        CoroutineScope(Dispatchers.Default).launch {
            val rectangles = detectRectOtsu(frame.clone()) // Clone frame to avoid concurrency issues
            // Process rectangles (e.g., draw bounding boxes) on the UI thread
            withContext(Dispatchers.Main) {
                // ... draw rectangles on frame ...
            }
        }

        // Text Recognition using Coroutines
        // Note: Tesseract 4 is extremely slow not suitable for real-time detection
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                detectTextTess(frame.clone(), baseAPI) { detectedText ->
                    textView.text = detectedText
                }
            }
        }

        return frame
    }


}


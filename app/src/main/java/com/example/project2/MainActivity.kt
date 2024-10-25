package com.example.project2

import android.content.pm.PackageManager
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import androidx.core.content.ContextCompat as ContextCompat1

class MainActivity : CameraActivity(), CvCameraViewListener2 {

    private val TAG = "OCVSample::Activity"
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase

    private fun initCamera(){
        // init view
        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById(R.id.surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        Log.i(TAG, "OpenCV Camera initialized")
    }

    private fun checkCamPermission(): Boolean {
        if (ContextCompat1.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 100)
        }



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
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val cd = CardDetection()

        cd.detectRectCanny(frame)

        return frame
    }
}


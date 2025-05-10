package com.example.bglib.demos

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bglib.R
import com.example.bglib.imgproc.checkCamPermission
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

/************************************************
 * This Activity serves as a base class         *
 * for activities that require camera access,   *
 * using CameraActivity and OpenCV.             *
 * Such as card detection.                      *
 ************************************************/
open class CardBaseActivity
    : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "CardBase::Activity"
    protected lateinit var mOpenCvCameraView: CameraBridgeViewBase
    protected val CAMERA_ID = "0"
    protected val coroutineScope = MainScope()

    private val ORIENTATIONS = SparseIntArray()
    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }


    protected fun initCamera(){
        mOpenCvCameraView = findViewById(R.id.surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        Log.i(TAG, "OpenCV Camera initialized")
    }


    init {
        Log.i(TAG, "Instantiated new $javaClass")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "called onCreate")

        // init view
        setContentView(R.layout.activity_save_cards)

        // add FLAG_KEEP_SCREEN_ON
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // init camera
        initCamera()

        if (checkCamPermission(this)) {
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

        // Process frame here
        // TODO: Override this method in a subclass to process the camera frame

        return frame
    }


}
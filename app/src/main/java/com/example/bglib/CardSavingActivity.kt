package com.example.bglib

import android.Manifest
import android.annotation.SuppressLint

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.bglib.CardDetection.Companion.detectRectOtsu
import com.example.bglib.CardDetection.Companion.getBoundingBoxes
import com.example.bglib.Utils.Companion.checkCamPermission
import com.example.bglib.Utils.Companion.resizeFrames
import com.example.bglib.Utils.Companion.saveFrames
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


class CardSavingActivity : CameraActivity(), CvCameraViewListener2 {

    protected lateinit var mOpenCvCameraView: CameraBridgeViewBase
    protected lateinit var button: Button
    protected val TAG = "OCVSample::Activity"

    private var subframes = mutableListOf<Mat>()
    private val frameSize = 512 // Change this according to the input layer size of your model

    @SuppressLint("NewApi")
    protected fun buttonPressed() {
        subframes = resizeFrames(subframes, frameSize, frameSize)
        saveFrames(subframes, this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_save_cards)

        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            buttonPressed()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mOpenCvCameraView = findViewById(R.id.surface_view)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)

        // Init Opencv
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully")
        } else {
            Log.e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }

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

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

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

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val rects = detectRectOtsu(frame, drawContours = false, drawBoundingBoxes = true)
        val bbs = getBoundingBoxes(frame, rects)

        subframes = bbs.map { rect -> Mat(frame, rect)}.toMutableList()

        return frame
    }
}

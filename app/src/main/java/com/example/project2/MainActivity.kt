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
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
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
        val mat = inputFrame.rgba()

        val contours = findContoursInImg(mat)

        // Draw rectangle around contours
        for (contour in contours){
            val approx = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())

            // approximate contour
            Imgproc.approxPolyDP(contour2f, approx, Imgproc.arcLength(contour2f, true)*0.02, true)

            // Check if 4 points
            if (approx.total() == 4L){
                // Draw rectangle
                val points = approx.toArray()
                for (i in 0 until 3){
                    Imgproc.line(mat, points[i], points[(i+1)%4], Scalar(75.0, 0.0, 130.0), 3)
                }
            }

        }

        return mat
    }
}

fun findContoursInImg(inputImg: Mat) : MutableList<MatOfPoint> {
    // Convert the input frame to grayscale
    val gray = Mat()
    Imgproc.cvtColor(inputImg, gray, Imgproc.COLOR_RGBA2GRAY)

    // Apply Gauss blur
    Imgproc.GaussianBlur(gray, gray, Size(5.0,5.0), 0.0)

    // Canny edge detection
    val edges = Mat()
    Imgproc.Canny(gray, edges, 100.0, 200.0)

    // Contours
    val contours = mutableListOf<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

    return contours
}


package com.example.bglib.demos

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bglib.CardBaseActivity
import com.example.bglib.CardDetection
import com.example.bglib.ImageProcessing
import com.example.bglib.R
import com.example.bglib.Utils
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream

class KeyDetectorActivity: CardBaseActivity() {
    val TAG = "KeyDetector::Activity"

    private var button: Button? = null
    private var lastKey: Mat? = null
    private var keyIndicator: TextView? = null
    private var keyRotation: Double = 0.0

    fun getCardKey(frame: Mat) : Mat? {
        val rects = CardDetection.Companion.detectRectOtsu(frame, drawBoundingBoxes = true)
        val boundingBoxes = CardDetection.Companion.getBoundingBoxes(frame, rects)

        // Whole card and the key
        if (boundingBoxes.size == 2){
            // The smaller rect is the key
            val keyBB = boundingBoxes.minBy { it.area() }
            val key = rects.minBy { it.height()*it.width() }
            val subframe = frame.submat(keyBB)

            // Get key rotation
            keyRotation = Utils.Companion.getRotationAngle(key)

            return subframe
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_key_detector)

        keyIndicator = findViewById<TextView>(R.id.keyIndicator)
        keyIndicator?.text = "Key not detected"
        val color = Color.rgb(255, 0, 0)
        keyIndicator?.setTextColor(color)

        button = findViewById<Button>(R.id.process_button)
        button?.setOnClickListener {
            // Pass the frame to the next activity
            try {
                // Convert last detected key to bitmap
                val stream = ByteArrayOutputStream()
                val frameBitmap = Utils.Companion.mat2bitmap(lastKey!!)
                frameBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) // or JPEG, WebP
                val byteArray = stream.toByteArray()

                // Pass as intent (byte array) to the next activity
                val intent = Intent(this, DetectedKeyActivity::class.java)
                intent.putExtra("frame", byteArray)
                startActivity(intent)


            }
            catch (e: Exception){
                Log.e(TAG, "Error passing frame to next activity", e)
            }

        }

        // init camera
        initCamera()

        if (Utils.Companion.checkCamPermission(this)) {
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

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val key = getCardKey(frame) ?: return frame

        // Rotate the key
        val rotatedKey = ImageProcessing.Companion.rotateImage(key, keyRotation)

        // Log.d(TAG, "Key rotation: $keyRotation")

        lastKey = rotatedKey

        runOnUiThread {
            keyIndicator?.text = "Key detected"
            val color = Color.rgb(0, 255, 0)
            keyIndicator?.setTextColor(color)
        }



        // TODO: Process a frame and return it probably in a coroutine

        return frame
    }
}
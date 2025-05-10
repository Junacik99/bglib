package com.example.bglib.demos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bglib.previews.CameraPreview
import com.example.bglib.ui.theme.BglibTheme
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/************************************************
 * A simple demo activity that shows            *
 * how to obtain and visualize hand landmarks.  *
 ************************************************/
class HandDetectionActivity : ComponentActivity() {
    val TAG = "HandDetection"

    var handLandmarkerHelper: HandLandmarkerHelper? = null
    lateinit var cameraExecutor: ExecutorService

    private var landmarks by mutableStateOf<List<PointF>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, 0)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            runningMode = RunningMode.IMAGE,
        )

        setContent{
            BglibTheme{
                HandDetectionScreen()
            }
        }
    }

    private fun checkPermissions() : Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handLandmarkerHelper?.clearHandLandmarker()
        cameraExecutor.shutdown()
    }

    inner class HandImageAnalyzer() : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            val resultsBundle = handLandmarkerHelper?.detectImage(imageProxy.toBitmap())

            if (resultsBundle != null && resultsBundle.results[0].landmarks().isNotEmpty()) {
                val normalizedLandmarks = resultsBundle.results[0].landmarks()[0]

                val newLandmarks = mutableListOf<PointF>()
                normalizedLandmarks.forEach { landmark ->
                    newLandmarks.add(PointF(1-landmark.y(), landmark.x()))
                }
                landmarks = newLandmarks
            }
            else {
                landmarks = emptyList()
            }

            imageProxy.close()
        }
    }




    @Composable
    fun HandDetectionScreen() {
        val context = LocalContext.current
        val analyzer = remember {
            HandImageAnalyzer()
        }
        val controller = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_ANALYSIS
                )
                setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    analyzer
                )
                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()

        ) {
            // Show camera
            CameraPreview(controller, Modifier.fillMaxSize())

            // Draw landmarks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                landmarks.forEach { landmark ->
                    val x = landmark.x * canvasWidth
                    val y = landmark.y * canvasHeight

                    drawCircle(
                        color = Color.Red,
                        radius = 10f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }

}





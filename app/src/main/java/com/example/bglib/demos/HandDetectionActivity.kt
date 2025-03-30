package com.example.bglib.demos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.LiveData
import com.example.bglib.CameraPreview
import com.example.bglib.ui.theme.BglibTheme
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val TAG = "HandDetection"


class HandDetectionActivity : ComponentActivity() {
    var handLandmarkerHelper: HandLandmarkerHelper? = null
    lateinit var cameraExecutor: ExecutorService

    // val _landmarks : MutableList<PointF> = mutableListOf()
    private var _landmarks by mutableStateOf<List<PointF>>(emptyList())

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
                _landmarks = newLandmarks

                // [<Normalized Landmark (x=0.7570929 y=0.19982518 z=-2.482477E-7 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.7474586 y=0.38236845 z=-0.025679823 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.6678848 y=0.502527 z=-0.03349236 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.57888246 y=0.56316686 z=-0.03427584 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.52734876 y=0.62431735 z=-0.031797975 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.54276335 y=0.40182742 z=-0.032107655 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.42899278 y=0.47113404 z=-0.033936426 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.3779015 y=0.50740486 z=-0.031656656 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.34817812 y=0.5311481 z=-0.02790484 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.5185526 y=0.30954298 z=-0.015969789 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.39884678 y=0.38414928 z=-0.012317917 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.3508834 y=0.4405719 z=-9.207361E-4 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.32895696 y=0.48076588 z=0.008651688 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.5162275 y=0.2373501 z=-6.399547E-4 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.40726835 y=0.28789914 z=7.736071E-4 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.3653081 y=0.33842173 z=0.0098766545 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.35052866 y=0.3814154 z=0.018853808 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.5270922 y=0.18148409 z=0.013030314 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.44959912 y=0.22972342 z=0.018852867 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.41394988 y=0.2721375 z=0.027974086 visibility= Optional.empty presence=Optional.empty)>, <Normalized Landmark (x=0.39720738 y=0.31117898 z=0.036988772 visibility= Optional.empty presence=Optional.empty)>]

                // Log.d(TAG, landmarks.toString())
            }
            else {
                _landmarks = emptyList()
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
            CameraPreview(controller, Modifier.fillMaxSize())

            // Draw landmarks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                _landmarks.forEach { landmark ->
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





package com.library.bglib.demos

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.library.bglib.previews.CameraPreview
import com.library.bglib.classes.Dice
import com.library.bglib.classes.Player
import com.library.bglib.ui.theme.BglibTheme
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

/************************************************
 * Demo activity to show hand detection.        *
 * Roll a virtual dice on hand movement.        *
 ***********************************************/
class DiceRollActivity : ComponentActivity() {
    val TAG = "DiceRoll::Activity"

    var handLandmarkerHelper: HandLandmarkerHelper? = null
    lateinit var cameraExecutor: ExecutorService

    private var landmarks by mutableStateOf<List<PointF>>(emptyList())

    // Init dices
    private val d4 = Dice(4)
    private val d6 = Dice(6)
    private val d10 = Dice(10)
    private val d12 = Dice(12)
    private val d20 = Dice(20)

    var currentDice by mutableStateOf(d6)

    // Normalized hand center position
    private var handCenter by mutableStateOf(PointF(0.5f, 0.5f))
    private var lastCenter = PointF(0.5f, 0.5f)

    private var result : Int? = null
    var resultText by mutableStateOf("Throw dice")
    private var diceThrown : Boolean = false
    private var multiplier by mutableIntStateOf(1)

    private val player : Player = Player("Kaicho")
    private var score by mutableIntStateOf(player.score)

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

                // Calculate hand center
                // Optional: landmarks[9] can be used instead to avoid additional computation
                var sumX = 0f
                var sumY = 0f
                landmarks.forEach { landmark ->
                    sumX += landmark.x
                    sumY += landmark.y
                }
                handCenter = PointF(sumX/landmarks.size, sumY/landmarks.size)

                // Calculate distance traveled from last frame
                val distance = sqrt(
                    (handCenter.x - lastCenter.x) * (handCenter.x - lastCenter.x) +
                    (handCenter.y - lastCenter.y) * (handCenter.y - lastCenter.y)
                )

                if (!diceThrown && distance > 0.2f) {
                    diceThrown = true
                    result = 0
                    for (i in 1..multiplier) {
                        result = result?.plus(currentDice.roll())
                    }
                    resultText = "Result: $result"

                    // Update score
                    player.score += result!!
                    score = player.score
                    player.deltaScore = result!!
                }

                lastCenter = handCenter
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
        var menuExpanded by remember { mutableStateOf(false) }

        Column (modifier = Modifier
            .fillMaxSize()
        ){
            // Player info row
            Row (modifier = Modifier.weight(0.1f).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(player.name, fontSize = 25.sp, modifier = Modifier.padding(5.dp))

                    Text("Score: $score", fontSize = 20.sp, modifier = Modifier.padding(5.dp))
                }
            }

            // Camera view
            Box(modifier = Modifier
                .weight(0.75f)
            ) {
                // Show camera
                CameraPreview(controller, modifier = Modifier.fillMaxSize())

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

                    // Draw hand center
                    if (landmarks.isNotEmpty()) {
                        drawCircle(
                            color = Color.Green,
                            radius = 10f,
                            center = Offset(handCenter.x * canvasWidth, handCenter.y * canvasHeight)
                        )
                    }

                }
            }

            // Control row
            Row (modifier = Modifier.weight(0.15f)){
                Column (horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(resultText, fontSize = 25.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp))

                    Text("Current dice: $multiplier${currentDice.name}", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

                    Row (horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                        Button(onClick = { if (multiplier > 1) multiplier-- }, modifier = Modifier.padding(5.dp)) {
                            Text("-1d", fontSize = 15.sp)
                        }
                        Button(onClick = { multiplier++ }, modifier = Modifier.padding(5.dp)) {
                            Text("+1d", fontSize = 15.sp)
                        }

                        Box {
                            Button(onClick = { menuExpanded = !menuExpanded }, modifier = Modifier.padding(5.dp)) {
                                Text("Dice", fontSize = 15.sp)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("d4") },
                                    onClick = { currentDice = d4; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("d6") },
                                    onClick = { currentDice = d6; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("d10") },
                                    onClick = { currentDice = d10; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("d12") },
                                    onClick = { currentDice = d12; menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("d20") },
                                    onClick = { currentDice = d20; menuExpanded = false }
                                )

                            }
                        }

                        Button(onClick = {
                            resultText = "Throw dice"
                            diceThrown = false
                            result = null
                                         }, modifier = Modifier.padding(5.dp)) {
                            Text("Reset", fontSize = 15.sp)
                        }
                    }
                }
            }
        }


    }


}
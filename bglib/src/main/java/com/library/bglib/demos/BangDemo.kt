package com.library.bglib.demos

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.library.bglib.previews.CameraPreview
import com.library.bglib.fragments.HelpFragment
import com.library.bglib.imgproc.ModelInterpreter
import com.library.bglib.imgproc.bitmap2mat
import com.library.bglib.imgproc.detectRectOtsu
import com.library.bglib.imgproc.getBoundingBoxes
import com.library.bglib.ui.theme.BglibTheme
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/************************************************
 * Demo activity for Bang card recognition.     *
 ***********************************************/
class BangDemo : AppCompatActivity() {
    val TAG = "Bang::Activity"

    lateinit var cameraExecutor: ExecutorService
    val fragmentContainerId = R.id.content

    var isProcessingFrame = false

    private lateinit var modelInterpreter : ModelInterpreter
    val modelName = "bang_classifier_224.tflite"

    data class bangCard(val name: String, val description: String)
    val labels = listOf(
        bangCard(name="bang", description = "Vystřelíš na jiného hráče (pokud je na dostřel)."),
        bangCard(name="barel", description = "Když na tebe někdo vystřelí, můžeš zkusit štěstí (srdce = vyhnul ses)."),
        bangCard(name="catbalou", description = "Přiměješ hráče odhodit 1 kartu (z ruky nebo před sebou)."),
        bangCard(name="dostavnik", description = "Vezmeš si 2 karty z balíčku."),
        bangCard(name="duel", description = "Vyzyvatel a cíl postupně odhazují Bang!, kdo nemůže, ztrácí život."),
        bangCard(name="dynamit", description = "Předáš si kartu, po 3 tazích můžeš vybuchnout (ztratíš 3 životy)."),
        bangCard(name="Volt 1", description = "Dostřel 1 (základní zbraň)."),
        bangCard(name="Remington 2", description = "Dostřel 2."),
        bangCard(name="Carabine 3", description = "Dostřel 3."),
        bangCard(name="Winchester 4", description = "Dostřel 4."),
        bangCard(name="Schofield 5", description="Dostřel 5."),
        bangCard(name="hledi", description=""),
        bangCard(name="hokynarstvi", description = "Všichni si rozdělí otevřené karty z balíčku."),
        bangCard(name="indiani", description="Všichni kromě hráče, který ji zahrál, musí odhodlit Bang!, jinak ztratí život."),
        bangCard(name="kulomet", description="Všichni ostatní musí odhodlit Vedle, jinak ztratí život."),
        bangCard(name="mustang", description="Ostatní tě mohou zasáhnout jen z větší vzdálenosti (+1 k jejich dostřelu)."),
        bangCard(name="panika", description="Ukradneš 1 kartu od hráče v dostřelu 1."),
        bangCard(name="pivo", description="Obnovíš si 1 život (nelze přes maximum)."),
        bangCard(name="saloon", description="Všichni hráči si obnoví 1 život."),
        bangCard(name="vedle", description="Vyhneš se střele (Bang!)."),
        bangCard(name="vezeni", description="Vybereš hráče, který při svém tahu musí zkusit štěstí (srdce = hraje normálně, jinak vynechá tah)."),
        bangCard(name="wellsfargo", description="Vezmeš si 3 karty z balíčku."),
        )

    var detectedCard by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, 0)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        try {
            modelInterpreter = ModelInterpreter(this, modelName, 224, 22)
        } catch (e: Exception) {
            e(TAG, "Error initializing model interpreter", e)
        }

        // Init Opencv
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully")
        } else {
            e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }


        setContent {
            BglibTheme {
                BangScreen()
            }
        }

    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        Log.d(TAG, "Start processing")
        // Get the bitmap from the ImageProxy.
        // This operation can be slow, so we do it in another thread.
        val bitmap = imageProxy.toBitmap()
        imageProxy.close()
        // Process the bitmap here
        processFrame(bitmap)
        Log.d(TAG, "End processing")
        runOnUiThread {
            imageProxy.close()
        }
    }

    private fun processFrame(bitmap: Bitmap){
        val mat = bitmap2mat(bitmap)

        val rects = detectRectOtsu(mat)

        if (rects.isNotEmpty()){
            // Get just the first rectangle (only 1 card)
            val boundingBox = getBoundingBoxes(mat, rects)[0]
            try {
                val subframe = Mat(mat, boundingBox)

                val inputData = modelInterpreter.preprocessMat(subframe)
                val output = modelInterpreter.predict(inputData)

                // Card index with max value
                val cardIndex = output.indices.maxByOrNull { output[it] }
                Log.d(TAG, "Card prediction: ${labels[cardIndex!!]}")
                Log.d(TAG, "Probability: ${output[cardIndex]}")
                detectedCard = cardIndex
            } catch (e: Exception) {
                e(TAG, "Error during rectangle classification", e)
            }
            finally {
                // isProcessingFrame = false
                return
            }
        }
        else{
            e(TAG, "No cards detected")
        }


    }

    @Composable
    private fun BangFragment() {

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.8f))
            .padding(16.dp)
        ){
            Text(
                text = "Karta: ${labels[detectedCard].name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Popis:",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = labels[detectedCard].description,
                modifier = Modifier.padding(bottom = 8.dp)
            )

        }
    }

    @Composable
    private fun BangScreen() {
        val context = LocalContext.current
        val controller = remember {
            LifecycleCameraController(context).apply {
                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                // setImageAnalysisAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                //     if (isProcessingFrame) {
                //         imageProxy.close()
                //     } else {
                //         isProcessingFrame = true
                //         processImageProxy(imageProxy)
                //     }
                // })
            }
        }
        var isFragmentActive by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()){
            CameraPreview(controller, modifier = Modifier.fillMaxSize())

            Button(
                onClick = {
                    // Predict card
                    if(!isProcessingFrame){
                        // Process a frame from camera here
                        controller.clearImageAnalysisAnalyzer()
                        controller.setImageAnalysisAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                            if (!isProcessingFrame) {
                                isProcessingFrame = true
                                processImageProxy(imageProxy)
                            }
                            imageProxy.close()
                        })
                    }


                    isFragmentActive = !isFragmentActive

                    // Show help
                    if (isFragmentActive) {
                        supportFragmentManager.commit {
                            replace(fragmentContainerId, HelpFragment({ BangFragment() }))
                            addToBackStack(null)
                        }
                    }
                    else {
                        isProcessingFrame = false
                        supportFragmentManager.popBackStack()
                    }

                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(15.dp)
            ) {
                Text("Bang Card")
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
}
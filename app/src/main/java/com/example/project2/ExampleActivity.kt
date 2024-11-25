package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.compose.foundation.layout.add
import com.example.project2.CardDetection.Companion.detectRectCanny
import com.example.project2.CardDetection.Companion.detectRectOtsu
import com.example.project2.TextDetection.Companion.detectText
import com.example.project2.TextDetection.Companion.getRotationCompensation
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.core.Rect
import kotlin.collections.toTypedArray

class ExampleActivity : CustomClassActivity() {
    val context = this

    // Get parameters from HomeActivity
    private val cardDetectMethod: String by lazy { intent.extras?.getString("cardDetectMethod", "") ?: "" }
    private val ocrMethod: String by lazy { intent.extras?.getString("ocrMethod", "") ?: "" }
    private val numberOfCards: Int by lazy { intent.extras?.getInt("numberOfCards", 0) ?: 0 }

    private lateinit var modelInterpreter : CardDetection.Companion.ModelInterpreter

    lateinit var button: Button

    private var latestRects: List<Rect>? = null
    private var latestFrame: Mat? = null
    private var activityStarted = false

    private fun logRectsInfo(rects: List<Rect>, frame: Mat) {
        Log.d(TAG, "Number of rectangles: ${rects.size}")
        Log.d(TAG, "Screen size: ${frame.size().width}x${frame.size().height}")
        for (rect in rects) {
            Log.d(TAG, "x: ${rect.x}, y: ${rect.y}, width: ${rect.width}, height: ${rect.height}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            logRectsInfo(latestRects ?: emptyList(), latestFrame ?: Mat())
        }

        try {
            modelInterpreter = CardDetection.Companion.ModelInterpreter(this, "binary_classifier_model.tflite")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing model interpreter", e)
        }

    }



    // Detect number of cards
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val rects = when (cardDetectMethod) {
            "Otsu" -> detectRectOtsu(frame)
            "Canny" -> detectRectCanny(frame)
            else -> mutableListOf<Rect>()
        }

        if (rects.size == numberOfCards && !activityStarted) {
            // Check if rects are cards
            try {
                rects.forEachIndexed{
                        index, rect ->
                    val subframe = Mat(frame, rect)
                    val inputData = modelInterpreter.preprocessMat(subframe)
                    val output = modelInterpreter.predict(inputData)
                    val isCard = output[0] <= 0.5 // Class 0 is card, class 1 is not a card
                    Log.d(TAG, "For rect $index is card: $isCard")

                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during rectangle classification", e)
            }




            Log.d(TAG, "All cards detected $numberOfCards")
            activityStarted = true
            // TODO: Check the alignment of the cards?

            // Once all the cards are detected, get rotation and start the OCR
            val rotation = getRotationCompensation(CAMERA_ID, this, false)
            CoroutineScope(Dispatchers.Default).launch {
                val deferredTexts = mutableListOf<Deferred<String>>()

                rects.forEachIndexed { index, rect ->
                    val subframe = Mat(frame, rect)
                    val deferredText = async {
                        val textDeferred = CompletableDeferred<String>()
                        detectText(subframe, rotation, TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)) { detectedText ->
                            Log.d(TAG, "For rect $index the text is $detectedText")
                            textDeferred.complete(detectedText) // Complete the CompletableDeferred with the detected text
                        }
                        textDeferred.await() // Wait for the CompletableDeferred to be completed
                    }
                    deferredTexts.add(deferredText)
                }

                val texts = deferredTexts.awaitAll()

                Log.d(TAG, "Texts: $texts")

                withContext(Dispatchers.Main) {
                    val intent = Intent(context, DetectedCardsActivity::class.java)
                    intent.putExtra("numberOfCards", numberOfCards)
                    intent.putExtra("texts", texts.toTypedArray())
                    context.startActivity(intent)
                }
            }



        }

        latestRects = rects
        latestFrame = frame

        return frame
    }
}
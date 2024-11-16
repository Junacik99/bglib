package com.example.project2

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.compose.ui.platform.LocalContext
import com.example.project2.CardDetection.Companion.detectRectCanny
import com.example.project2.CardDetection.Companion.detectRectOtsu
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.core.Rect

class ExampleActivity : CustomClassActivity() {
    val context = this

    // Get parameters from HomeActivity
    private val cardDetectMethod: String by lazy { intent.extras?.getString("cardDetectMethod", "") ?: "" }
    private val ocrMethod: String by lazy { intent.extras?.getString("ocrMethod", "") ?: "" }
    private val numberOfCards: Int by lazy { intent.extras?.getInt("numberOfCards", 0) ?: 0 }

    lateinit var button: Button

    private var latestRects: List<Rect>? = null
    private var latestFrame: Mat? = null

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

    }



    // Detect number of cards
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val rects = when (cardDetectMethod) {
            "Otsu" -> detectRectOtsu(frame)
            "Canny" -> detectRectCanny(frame)
            else -> mutableListOf<Rect>()
        }

        if (rects.size == numberOfCards) {
            Log.d(TAG, "All cards detected $numberOfCards")
            // TODO: Check the alignment of the cards?

            // Once all the cards are detected, start the OCR and align them
            val intent = Intent(context, DetectedCardsActivity::class.java)
            intent.putExtra("numberOfCards", numberOfCards)
            context.startActivity(intent)
        }

        latestRects = rects
        latestFrame = frame

        return frame
    }
}
package com.example.project2

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.project2.CardDetection.Companion.detectRectCanny
import com.example.project2.CardDetection.Companion.detectRectOtsu
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.core.Rect

class ExampleActivity : CustomClassActivity() {
    // Get parameters from HomeActivity
    private val cardDetectMethod: String by lazy { intent.extras?.getString("cardDetectMethod", "") ?: "" }
    private val ocrMethod: String by lazy { intent.extras?.getString("ocrMethod", "") ?: "" }
    private val noCards: Int by lazy { intent.extras?.getInt("noCards", 0) ?: 0 }

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

        // TODO: Once all the cards are detected, start the OCR and align them
        if (rects.size == noCards) {
            Log.d(TAG, "All cards detected $noCards")
        }

        latestRects = rects
        latestFrame = frame

        return frame
    }
}
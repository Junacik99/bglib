package com.example.project2

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
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

    // Detect number of cards
    // TODO: Once all the cards are detected, start the OCR and align them
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val rects = when (cardDetectMethod) {
            "Otsu" -> detectRectOtsu(frame)
            "Canny" -> detectRectCanny(frame)
            else -> mutableListOf<Rect>()
        }

        if (rects.size == noCards) {
            Log.d(TAG, "All cards detected")
        }

        return frame
    }
}
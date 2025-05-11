package com.library.bglib.demos

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import com.library.bglib.R
import com.library.bglib.imgproc.detectRectOtsu
import com.library.bglib.imgproc.getBoundingBoxes
import com.library.bglib.imgproc.resizeFrames
import com.library.bglib.imgproc.saveFrames
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

/************************************************
 * This is an Activity that serves              *
 * as a Dataset Builder.                        *
 * It inherits from CardBaseActivity            *
 * to initialize camera and OpenCV.             *
 * On successful launch of this activity        *
 * a user will be able to take                  *
 * pictures of cards.                           *
 * Those will be automatically cropped          *
 * and resized to the specified frameSize.      *
 * Images will be saved in the MediaStore       *
 * in the CardDetector folder.                  *
 ************************************************/
open class DatasetBuilderActivity
    : CardBaseActivity() {

    protected lateinit var button: Button
    protected val TAG = "DatasetBuilder::Activity"

    private var subframes = mutableListOf<Mat>()
    private val frameSize = 512 // Change this according to the input layer Dimensions of your model

    @SuppressLint("NewApi")
    protected fun buttonPressed() {
        subframes = resizeFrames(subframes, frameSize, frameSize)
        saveFrames(subframes, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            buttonPressed()
        }

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        val rects = detectRectOtsu(
            frame,
            drawContours = false,
            drawBoundingBoxes = true
        )
        val bbs = getBoundingBoxes(frame, rects)

        subframes = bbs.map { rect -> Mat(frame, rect) }.toMutableList()

        return frame
    }
}
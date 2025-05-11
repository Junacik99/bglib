package com.library.bglib.demos

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.library.bglib.imgproc.detectRectOtsu
import com.library.bglib.imgproc.divideFrameIntoGrid
import com.library.bglib.imgproc.getAvgColor
import com.library.bglib.imgproc.getClosestColor
import com.library.bglib.imgproc.bitmap2mat
import com.library.bglib.imgproc.mat2bitmap
import com.library.bglib.ui.theme.BglibTheme
import org.opencv.core.Mat
import androidx.core.graphics.createBitmap

/************************************************
 * Shows a detected key                         *
 * from KeyDetectorActivity.                    *
 ***********************************************/
class DetectedKeyActivity: ComponentActivity() {

    var globalBitmap: Bitmap? = null

    fun getKey(bitmap: Bitmap, numRows: Int, numCols: Int): MutableList<Int> {
        val mat : Mat = bitmap2mat(bitmap)

        val targetColors = listOf(
            Color.rgb(0, 0, 255), // blue
            Color.rgb(255, 0, 0), // red
            Color.rgb(0, 0, 0), // black
            Color.rgb(245, 214, 147)) // yellowish

        // TODO: Crop the image to the key
        val rects = detectRectOtsu(mat, drawContours = true, drawBoundingBoxes = true)
        for (rect in rects) {
            Log.d("OCVSample::Activity", "Rect:")
            for (point in rect.toArray()) {
                Log.d("OCVSample::Activity", "Point: (${point.x}, ${point.y})")
            }
        }
        globalBitmap = mat2bitmap(mat)

        val grid = divideFrameIntoGrid(mat, numRows, numCols)


        val guessedColors = mutableListOf<Int>()
        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val subframe = grid[i * numCols + j]
                val avgColor = getAvgColor(subframe)

                val closestColor = getClosestColor(avgColor, targetColors)
                guessedColors.add(closestColor)
            }
        }

        return guessedColors
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get an image from the intent
        val byteArray = intent.getByteArrayExtra("frame")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        val numRows = 5
        val numCols = 5

        val guessedColors = getKey(bitmap, numRows, numCols)

        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    globalBitmap?.let { it1 -> DetectedKey(it1, guessedColors, numRows, numCols) }
                }
            }
        }
    }
}

@Composable
fun DetectedKey(
    picture: Bitmap,
    guessedColors: MutableList<Int>,
    numRows: Int,
    numCols: Int){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // Key reference
        Text("Key Reference", modifier = Modifier.padding(10.dp))
        Image(
            bitmap = picture.asImageBitmap(),
            contentDescription = "Detected Key",
        )
        Spacer(modifier = Modifier.padding(40.dp))

        // Detected key
        Text("Detected Key", modifier = Modifier.padding(10.dp))
        for (i in 0 until numRows) {
            Row {
                for (j in 0 until numCols) {
                    val bm = createBitmap(98, 98)
                    bm.eraseColor(guessedColors[i * numCols + j])
                    Image(
                        bitmap = bm.asImageBitmap(),
                        contentDescription = "Detected Key",
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun DetectedKeyPreview() {
    val emptyBitmap = createBitmap(500, 500)
    emptyBitmap.eraseColor(Color.RED)

    BglibTheme {
        DetectedKey(emptyBitmap, MutableList(25){ Color.BLUE}, 5, 5)
    }
}
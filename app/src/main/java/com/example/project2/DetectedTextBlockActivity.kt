package com.example.project2

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.ui.theme.Project2Theme
import com.google.mlkit.vision.text.Text
import org.opencv.core.Rect

class DetectedTextBlockActivity : ComponentActivity() {

    lateinit var textBlocks: List<ParcelableTextBlock>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("OCVSample::Activity", "NEW ACTIVITY")

        val parcelable : ParcelableText? = intent.getParcelableExtra("cardText")

        if (parcelable != null) {
            Log.d("OCVSample::Activity", parcelable.text)
            textBlocks = parcelable.textBlocks
        }
        else
            Log.e("OCVSample::Activity", "Parcelable is null")



        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DetectedCardText(textBlocks)
                }
            }
        }
    }
}

@Composable
fun DetectedCardText(textBlocks: List<ParcelableTextBlock>){
    Column {
        Spacer(modifier = Modifier.padding(25.dp))

        OutlinedCard(modifier = Modifier.padding(10.dp).fillMaxSize()) {
            for (textBlock in textBlocks){
                OutlinedCard(modifier = Modifier.padding(5.dp)) {
                    for (line in textBlock.lines) {
                        Text(text = line.text, fontSize = 20.sp, modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }

}


fun createSampleParcelableTextBlocks(): List<ParcelableTextBlock> {
    // Create some sample ParcelableTextElements
    val element1 = ParcelableTextElement("Hello", RectParcelable(Rect(10, 10, 50, 20)), arrayOf(intArrayOf(10, 10), intArrayOf(60, 10), intArrayOf(60, 30), intArrayOf(10, 30)))
    val element2 = ParcelableTextElement("World", RectParcelable(Rect(70, 10, 50, 20)), arrayOf(intArrayOf(70, 10), intArrayOf(120, 10), intArrayOf(120, 30), intArrayOf(70, 30)))
    val element3 = ParcelableTextElement("This", RectParcelable(Rect(10, 40, 30, 20)), arrayOf(intArrayOf(10, 40), intArrayOf(40, 40), intArrayOf(40, 60), intArrayOf(10, 60)))
    val element4 = ParcelableTextElement("is", RectParcelable(Rect(50, 40, 20, 20)), arrayOf(intArrayOf(50, 40), intArrayOf(70, 40), intArrayOf(70, 60), intArrayOf(50, 60)))
    val element5 = ParcelableTextElement("a", RectParcelable(Rect(80, 40, 10, 20)), arrayOf(intArrayOf(80, 40), intArrayOf(90, 40), intArrayOf(90, 60), intArrayOf(80, 60)))
    val element6 = ParcelableTextElement("test", RectParcelable(Rect(100, 40, 40, 20)), arrayOf(intArrayOf(100, 40), intArrayOf(140, 40), intArrayOf(140, 60), intArrayOf(100, 60)))

    // Create some sample ParcelableTextLines
    val line1 = ParcelableTextLine("Hello World", RectParcelable(Rect(10, 10, 110, 20)), arrayOf(intArrayOf(10, 10), intArrayOf(120, 10), intArrayOf(120, 30), intArrayOf(10, 30)), listOf(element1, element2))
    val line2 = ParcelableTextLine("This is a test", RectParcelable(Rect(10, 40, 130, 20)), arrayOf(intArrayOf(10, 40), intArrayOf(140, 40), intArrayOf(140, 60), intArrayOf(10, 60)), listOf(element3, element4, element5, element6))

    // Create some sample ParcelableTextBlocks
    val block1 = ParcelableTextBlock("Hello World", RectParcelable(Rect(10, 10, 110, 20)), arrayOf(intArrayOf(10, 10), intArrayOf(120, 10), intArrayOf(120, 30), intArrayOf(10, 30)), listOf(line1))
    val block2 = ParcelableTextBlock("This is a test", RectParcelable(Rect(10, 40, 130, 20)), arrayOf(intArrayOf(10, 40), intArrayOf(140, 40), intArrayOf(140, 60), intArrayOf(10, 60)), listOf(line2))

    return listOf(block1, block2)
}
@Preview(showBackground = true)
@Composable
fun DetectedCardTextPreview() {

    Project2Theme {
        DetectedCardText(createSampleParcelableTextBlocks())
    }
}
package com.example.project2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.ui.theme.Project2Theme

class DetectedCardsActivity : ComponentActivity() {
    private var numberOfCards = 0

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        numberOfCards = intent.getIntExtra("numberOfCards", 0)
        val texts = intent.getStringArrayExtra("texts") ?: emptyArray()

        Log.d("OCVSample::Activity", "Text onCreate: ${texts.toString()}")



        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DetectedCards(numberOfCards, texts)
                }
            }
        }
    }
}

@Composable
fun DetectedCards(numberOfCards: Int, texts: Array<String>){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Cards Result", fontSize = 24.sp)

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Detected cards: $numberOfCards")

        Spacer(modifier = Modifier.padding(40.dp))
        for (text in texts) {
            // Log.d("OCVSample::Activity", "Text: $text")
            Text(text)
            Spacer(modifier = Modifier.padding(10.dp))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DetectedCardsPreview() {
    Project2Theme {
        DetectedCards(4, listOf("Card 1", "Card 2", "Card 3", "Card 4").toTypedArray())
    }
}
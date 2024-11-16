package com.example.project2

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.compose.ui.input.pointer.motionEventSpy
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

        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DetectedCards(numberOfCards)
                }
            }
        }
    }
}

@Composable
fun DetectedCards(numberOfCards: Int){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Cards Result", fontSize = 24.sp)

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Detected cards: $numberOfCards")

    }
}

@Preview(showBackground = true)
@Composable
fun DetectedCardsPreview() {
    Project2Theme {
        DetectedCards(4)
    }
}
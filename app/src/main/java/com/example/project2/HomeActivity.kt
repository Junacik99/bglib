package com.example.project2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.ui.theme.Project2Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Surface(modifier = Modifier.fillMaxSize()){
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(){
    val context = LocalContext.current

    var cardDetectMethod by remember { mutableStateOf("Otsu") }
    var ocrMethod by remember { mutableStateOf("Firebase") }
    var numberOfCards by remember { mutableStateOf(4) }

    // Everything Column
    Column (modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // Header
        Spacer(modifier = Modifier.padding(40.dp))
        Text(
            "Welcome to the Card Detector",
            fontSize = 24.sp)

        Spacer(modifier = Modifier.padding(100.dp))

        // Buttons
        Button(
            onClick = {
                context.startActivity(Intent(context, ExampleActivity::class.java))
            }) {
            Text("Card Detection")
        }
        Button(
                onClick = {
                    context.startActivity(Intent(context, CardSavingActivity::class.java))
                }) {
            Text("Dataset builder")
        }
        Button(
            onClick = {
                context.startActivity(Intent(context, KeyDetectorActivity::class.java))
            }) {
            Text("Key Detection")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Project2Theme {
        HomeScreen()
    }
}
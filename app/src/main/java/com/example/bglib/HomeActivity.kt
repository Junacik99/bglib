package com.example.bglib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bglib.demos.AchievementsActivity
import com.example.bglib.demos.DiceRollActivity
import com.example.bglib.demos.HandDetectionActivity
import com.example.bglib.demos.HelpActivity
import com.example.bglib.demos.KMeansActivity
import com.example.bglib.demos.KeyDetectorActivity
import com.example.bglib.demos.LivesActivity
import com.example.bglib.demos.ScoreActivity
import com.example.bglib.demos.TimerActivity
import com.example.bglib.ui.theme.BglibTheme
import kotlin.jvm.java

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BglibTheme {
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
    var rows by remember { mutableIntStateOf(2) }
    var cols by remember { mutableIntStateOf(3) }

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

        Text("How many cards to detect:")
        Row(){
            OutlinedTextField(
                value = rows.toString(),
                onValueChange = {
                    rows = it.toIntOrNull() ?: 0
                },
                modifier = Modifier
                    .size(width = 100.dp, height = 60.dp),
                label = { Text("rows")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = cols.toString(),
                onValueChange = {
                    cols = it.toIntOrNull() ?: 0
                },
                modifier = Modifier
                    .size(width = 100.dp, height = 60.dp),
                label = { Text("cols")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.padding(50.dp))

        // Buttons
        LazyColumn {
            item {
                Button(
                    onClick = {
                        val intent = Intent(context, ExampleActivity::class.java)
                        intent.putExtra("rows", rows)
                        intent.putExtra("cols", cols)
                        intent.putExtra("cardDetectMethod", cardDetectMethod)
                        intent.putExtra("ocrMethod", ocrMethod)
                        context.startActivity(intent)
                    }) {
                    Text("Card Detection")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, CardSavingActivity::class.java))
                    }) {
                    Text("Dataset builder")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, KeyDetectorActivity::class.java))
                    }) {
                    Text("Key Detection")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, HelpActivity::class.java))
                    }
                ) {
                    Text("Help")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, KMeansActivity::class.java))
                    }) {
                    Text("KMeans Segmentation")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, DiceRollActivity::class.java))
                    }) {
                    Text("Dice Roll")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, HandDetectionActivity::class.java))
                    }) {
                    Text("Hand Landmarks")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, ScoreActivity::class.java))
                    }) {
                    Text("Score")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LivesActivity::class.java))
                    }) {
                    Text("Lives")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, TimerActivity::class.java))
                    }) {
                    Text("Timer")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, AchievementsActivity::class.java))
                    }) {
                    Text("Achievements")
                }
            }
            item {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, TestActivity::class.java))
                    }) {
                    Text("Test")
                }
            }

            item {
                Spacer(modifier = Modifier.padding(25.dp))
            }
        }


    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BglibTheme {
        HomeScreen()
    }
}
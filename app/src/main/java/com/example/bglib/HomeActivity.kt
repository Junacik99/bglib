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
import com.example.bglib.ui.theme.BglibTheme

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
        Button(
            onClick = {
                context.startActivity(Intent(context, TestActivity::class.java))
            }) {
            Text("Test")
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
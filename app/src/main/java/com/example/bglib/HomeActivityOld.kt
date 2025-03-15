package com.example.bglib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bglib.ui.theme.BglibTheme

class HomeActivityOld : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Surface(modifier = Modifier.fillMaxSize()){
                    HomeScreenOld()
                }
            }
        }
    }
}

@Composable
fun HomeScreenOld(){
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

        // Selection and Preview
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            // Selection column
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Select Card Detection model:")
                Box {
                    var cdExpanded by remember { mutableStateOf(false) }

                    Button(onClick = { cdExpanded = !cdExpanded }) {
                        Text("Card Detection")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(expanded = cdExpanded, onDismissRequest = { cdExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Otsu") },
                            onClick = {
                                cardDetectMethod = "Otsu"
                                cdExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Canny") },
                            onClick = {
                                cardDetectMethod = "Canny"
                                cdExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text("Select OCR model:")
                Box {
                    var ocrExpanded by remember { mutableStateOf(false) }

                    Button(onClick = { ocrExpanded = !ocrExpanded }) {
                        Text("OCR")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(expanded = ocrExpanded, onDismissRequest = { ocrExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Firebase") },
                            onClick = {
                                ocrMethod = "Firebase"
                                ocrExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("TessTwo") },
                            onClick = {
                                ocrMethod = "TessTwo"
                                ocrExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Text("How many cards to detect:")
                OutlinedTextField(
                    value = numberOfCards.toString(),
                    onValueChange = {
                        numberOfCards = it.toIntOrNull() ?: 0
                    },
                    modifier = Modifier
                        .size(width = 100.dp, height = 60.dp),
                    label = { Text("# of cards")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.padding(20.dp))

            // Preview column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Card Detection:")
                Text(cardDetectMethod)
                Spacer(modifier = Modifier.padding(5.dp))
                Text("OCR:")
                Text(ocrMethod)
                Spacer(modifier = Modifier.padding(5.dp))
                Text("Cards to detect:")
                Text(numberOfCards.toString())
            }

        }

        Spacer(modifier = Modifier.padding(40.dp))

        // Start Button
        Button(
            onClick = {
                // TODO: Decide which activity to start
                val intent = Intent(context, ExampleActivity::class.java)
                intent.putExtra("cardDetectMethod", cardDetectMethod)
                intent.putExtra("ocrMethod", ocrMethod)
                intent.putExtra("numberOfCards", numberOfCards)
                context.startActivity(intent)
        }) {
            Text("START")
        }


            // Button(onClick = {
            //     context.startActivity(Intent(context, FirebaseActivity::class.java))
            // }) {
            //     Text("Firebase")
            // }
            //
            // Button(onClick = {
            //     context.startActivity(Intent(context, TessTwoActivity::class.java))
            // }) {
            //     Text("Tess Two")
            // }
            //
            // Button(onClick = {
            //     context.startActivity(Intent(context, ExampleActivity::class.java))
            // }) {
            //     Text("Example Activity")
            // }


    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenOldPreview() {
    BglibTheme {
        HomeScreen()
    }
}
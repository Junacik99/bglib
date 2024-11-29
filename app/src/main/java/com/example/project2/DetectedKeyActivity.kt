package com.example.project2

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.example.project2.ui.theme.Project2Theme

class DetectedKeyActivity: ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get an image from the intent
        val byteArray = intent.getByteArrayExtra("frame")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        //
        // val imageView = findViewById<ImageView>(R.id.imageView)
        // imageView.setImageBitmap(bitmap)

        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DetectedKey(bitmap)
                }
            }
        }
    }
}

@Composable
fun DetectedKey(picture: Bitmap){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Image(
            bitmap = picture.asImageBitmap(),
            contentDescription = "Detected Key",
        )

    }

}

@Preview(showBackground = true)
@Composable
fun DetectedKeyPreview() {
    val emptyBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    emptyBitmap.eraseColor(android.graphics.Color.RED)

    Project2Theme {
        DetectedKey(emptyBitmap)
    }
}
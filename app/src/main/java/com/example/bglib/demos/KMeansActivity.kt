package com.example.bglib.demos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.bglib.ImageProcessing.Companion.segment_kmeans
import com.example.bglib.ui.theme.BglibTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.graphics.scale


class KMeansActivity : ComponentActivity() {
    val TAG = "KMeans::Activity"
    private lateinit var segmentationExecutor: ExecutorService
    private lateinit var segmentationScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        segmentationExecutor = Executors.newSingleThreadExecutor()
        segmentationScope = CoroutineScope(segmentationExecutor.asCoroutineDispatcher())
        setContent{
            BglibTheme {
                GalleryScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        segmentationExecutor.shutdown()
    }

    @Composable
    fun GalleryScreen() {
        val context = LocalContext.current
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var segmentedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                if (imageUri != null) {
                    isLoading = true
                    segmentedBitmap = null
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri!!)
                        bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                    } catch (e: IOException) {
                        Log.e("GalleryActivity", "Error loading image: ", e)
                        isLoading = false
                    }
                }
            }
        }

        LaunchedEffect(key1 = bitmap) {
            if (bitmap != null) {
                segmentationScope.launch {
                    val resizedBitmap = bitmap!!.scale(512, 512)
                    val (finalBitmap, _) = segment_kmeans(resizedBitmap, 6)
                    withContext(Dispatchers.Main) {
                        segmentedBitmap = finalBitmap
                        isLoading = false
                    }
                }

            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }) {
                Text(text = "Open Gallery")
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                if (segmentedBitmap != null) {
                    Image(
                        bitmap = segmentedBitmap!!.asImageBitmap(),
                        contentDescription = "Segmented Image",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}
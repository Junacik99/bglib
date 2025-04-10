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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.sp
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
import com.example.bglib.ImageProcessing.Companion.segment_meanshift


class ImageSegmentationActivity : ComponentActivity() {
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
        var menuExpanded by remember { mutableStateOf(false) }

        var segmentAlg by remember { mutableStateOf("kmeans") }

        var segmentSize by remember { mutableStateOf(256) }
        var segmentCount by remember { mutableStateOf(6) }

        var spacialBandwith by remember { mutableStateOf(21) }
        var colorBandwith by remember { mutableStateOf(30.0) }


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
                when (segmentAlg) {
                    "kmeans" -> {
                        // k-means segmentation
                        segmentationScope.launch {
                            val origWidth = bitmap!!.width
                            val origHeight = bitmap!!.height
                            val resizedBitmap = bitmap!!.scale(segmentSize, segmentSize)
                            val (segmentedResult, _) = segment_kmeans(resizedBitmap, segmentCount)
                            val finalBitmap = segmentedResult.scale(origWidth, origHeight)
                            withContext(Dispatchers.Main) {
                                segmentedBitmap = finalBitmap
                                isLoading = false
                            }
                        }
                    }
                    "meanshift" -> {
                        // mean shift segmentation
                        segmentationScope.launch {
                            val origWidth = bitmap!!.width
                            val origHeight = bitmap!!.height
                            val resizedBitmap = bitmap!!.scale(segmentSize, segmentSize)

                            val (segmentedResult, _) = segment_meanshift(
                                resizedBitmap,
                                spatialBandwidth = spacialBandwith,
                                colorBandwidth = colorBandwith,
                                threshold = 0.1)
                            val finalBitmap = segmentedResult.scale(origWidth, origHeight)

                            withContext(Dispatchers.Main) {
                                segmentedBitmap = finalBitmap
                                isLoading = false
                            }
                        }
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
            Row (
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    onClick = {
                        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryLauncher.launch(galleryIntent)
                    },
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text(text = "Open Gallery")
                }
                Box {
                    Button(onClick = { menuExpanded = !menuExpanded }, modifier = Modifier.padding(5.dp)) {
                        Text(segmentAlg, fontSize = 15.sp)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("kmeans") },
                            onClick = { segmentAlg = "kmeans"; menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("mean shift") },
                            onClick = { segmentAlg = "meanshift"; menuExpanded = false }
                        )
                    }
                }

            }

            when (segmentAlg) {
                "kmeans" -> {
                    Row (
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "K = $segmentCount")
                        Slider(
                            value = segmentCount.toFloat(),
                            onValueChange = { newValue ->
                                segmentCount = newValue.toInt()
                            },
                            valueRange = 2f..32f,)
                    }
                    Row (
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Segment Size = $segmentSize")
                        Slider(
                            value = segmentSize.toFloat(),
                            onValueChange = { newValue ->
                                segmentSize = newValue.toInt()
                            },
                            valueRange = 32f..1024f,)
                    }
                }

                "meanshift" -> {
                //     mean shift controls
                    Row (
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "spacial W = $spacialBandwith")
                        Slider(
                            value = spacialBandwith.toFloat(),
                            onValueChange = { newValue ->
                                spacialBandwith = newValue.toInt()
                            },
                            valueRange = 2f..32f,)
                    }
                    Row (
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "color W = $colorBandwith")
                        Slider(
                            value = colorBandwith.toFloat(),
                            onValueChange = { newValue ->
                                colorBandwith = newValue.toInt().toDouble()
                            },
                            valueRange = 2f..64f,)
                    }
                    Row (
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Segment Size = $segmentSize")
                        Slider(
                            value = segmentSize.toFloat(),
                            onValueChange = { newValue ->
                                segmentSize = newValue.toInt()
                            },
                            valueRange = 32f..1024f,)
                    }
                }
            }

            // Output
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
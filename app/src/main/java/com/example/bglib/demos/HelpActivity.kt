package com.example.bglib.demos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.bglib.CameraPreview
import com.example.bglib.HelpFragment
import com.example.bglib.ui.theme.BglibTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HelpActivity : AppCompatActivity() {
    val TAG = "Help::Activity"

    lateinit var cameraExecutor: ExecutorService
    val fragmentContainerId = android.R.id.content

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, 0)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()


        setContent {
            BglibTheme {
                HelpScreen()
            }
        }

    }

    @Composable
    private fun HelpScreen() {
        val context = LocalContext.current
        val controller = remember {
            LifecycleCameraController(context).apply {
                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
            }
        }
        var isFragmentActive by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()){
            CameraPreview(controller, modifier = Modifier.fillMaxSize())

            Button(
                onClick = {
                    isFragmentActive = !isFragmentActive

                    // Show help
                    if (isFragmentActive) {
                        supportFragmentManager.commit {
                            replace(fragmentContainerId, HelpFragment())
                            addToBackStack(null)
                        }
                    }
                    else {
                        supportFragmentManager.popBackStack()
                    }

                },
                modifier = Modifier.align(Alignment.TopEnd).padding(15.dp)
            ) {
                Text("Help")
            }
        }
    }

    private fun checkPermissions() : Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    companion object {
        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}
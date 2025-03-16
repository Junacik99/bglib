package com.example.bglib.demos

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bglib.R
import com.example.bglib.Timer
import com.example.bglib.ui.theme.BglibTheme

private var timerTask: java.util.Timer? = null
private var mediaPlayer: MediaPlayer? = null


class TimerActivity : ComponentActivity(){
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.r2d2_beep)

        val timer = Timer(1,1,7)

        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    TimerScreen(timer)
                }
            }
        }
    }
}

@Composable
fun TimerScreen(timer: Timer = Timer()){
    var isStarted by remember { mutableStateOf(false) }

    var seconds by remember { mutableIntStateOf(timer.getSeconds()) }
    var minutes by remember { mutableIntStateOf(timer.getMinutes()) }
    var hours by remember { mutableIntStateOf(timer.getHours()) }

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            "$hours:$minutes:$seconds",
            fontSize = 70.sp
        )

        Spacer(modifier = Modifier.padding(30.dp))


        Button(onClick = {
            if (isStarted) return@Button
            isStarted = true
            timerTask = timer.start() {
                seconds = timer.getSeconds()
                minutes = timer.getMinutes()
                hours = timer.getHours()
                if (timer.time <= 0){
                    mediaPlayer?.start()
                }
            }
        }){
            Text("Start", fontSize = 50.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTimerScreen(){
    val timer = Timer(100,23)
    TimerScreen(timer)
}

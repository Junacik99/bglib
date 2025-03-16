package com.example.bglib.demos

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import com.example.bglib.Player
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bglib.ui.theme.BglibTheme
import java.nio.file.WatchEvent


class LivesActivity: ComponentActivity(){

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val player1 = Player("Striker")
        val player2 = Player("Champion")

        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    PlayersBattle(player1, player2)
                }
            }
        }
    }
}

@Composable
fun DisplayPlayer(player: Player, lives: Int, color: Color = Color.Black){
    Column {
        Spacer(modifier = Modifier.padding(10.dp))
        Text("Player ${player.id.toString()}")
        Spacer(modifier = Modifier.padding(10.dp))
        Text(player.name, fontSize = 30.sp, color = color)
        Spacer(modifier = Modifier.padding(10.dp))
        Text("Lives: ${player.lives}", fontSize = 25.sp, color = color)
        Spacer(modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun PlayersBattle(player1: Player, player2: Player){
    var lives1 by remember { mutableIntStateOf(player1.lives) }
    var lives2 by remember { mutableIntStateOf(player2.lives) }

    Column(modifier = Modifier.fillMaxSize()){
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(20.dp))

                DisplayPlayer(player1, lives1, Color.Red)

                Spacer(modifier = Modifier.padding(20.dp))

                // Display attacks
                Row {
                    Button( onClick = {
                        player2.lives -= 5
                        lives2 = player2.lives
                    }){
                        Text("Attack 5")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))

                    Button( onClick = {
                        player2.lives -= 30
                        lives2 = player2.lives
                    }){
                        Text("Attack 30")
                    }
                }

            }


        }

        Row(
            modifier = Modifier.weight(0.4f).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            if (player1.hasLost()){
                Text("Player ${player1.id} - ${player1.name} has lost!", fontSize = 30.sp, color = Color.Blue)
            }
            else if (player2.hasLost()){
                Text("Player ${player2.id} - ${player2.name} has lost!", fontSize = 30.sp, color = Color.Red)
            }
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // display attacks
                Row {
                    Button( onClick = {
                        player1.lives -= 10
                        lives1 = player1.lives
                    }){
                        Text("Attack 10")
                    }
                    Spacer(modifier = Modifier.padding(10.dp))

                    Button( onClick = {
                        player1.lives -= 20
                        lives1 = player1.lives
                    }){
                        Text("Attack 20")
                    }
                }

                Spacer(modifier = Modifier.padding(20.dp))

                DisplayPlayer(player2, lives2, Color.Blue)

                Spacer(modifier = Modifier.padding(20.dp))
            }


        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayersBattlePreview() {
    val player1 = Player("Striker")
    val player2 = Player("Champion")
    PlayersBattle(player1, player2)
}

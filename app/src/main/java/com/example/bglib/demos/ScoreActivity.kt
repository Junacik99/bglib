package com.example.bglib.demos

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
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
import com.example.bglib.Player
import com.example.bglib.R
import com.example.bglib.Team
import com.example.bglib.ui.theme.BglibTheme

var mediaPlayer: MediaPlayer? = null

class ScoreActivity: ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create teams and players
        val teamA = Team("Team A")
        val teamB = Team("Team B")

        for (i in 1..5) {
            if (i < 4)
                teamA.players.add(Player())
            else
                teamB.players.add(Player())
        }

        teamA.isWinner = { teamA.score >= 1000 }
        teamB.isWinner = { teamB.score >= 1000 }

        mediaPlayer = MediaPlayer.create(this, R.raw.r2d2_beep)


        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    TeamBattle(teamA, teamB)
                }
            }
        }
    }


}

fun playerOnClick(team: Team, player: Player){
    player.updateScore(100)
    team.updateScore()
    Log.d("OCVSample::Activity", "${team.name} score: ${team.score}")
}

@Composable
fun TeamBattle(teamA: Team = Team("Team A"), teamB: Team = Team("Team B")) {

    var scoreA by remember { mutableIntStateOf(teamA.score) }
    var scoreB by remember { mutableIntStateOf(teamB.score) }
    var winner by remember { mutableStateOf("") }

    Column (modifier = Modifier.fillMaxSize()){
        Row (modifier = Modifier.weight(1f)) {
            Column (modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(30.dp))
                Text(teamA.name, fontSize = 30.sp)
                Spacer(modifier = Modifier.padding(30.dp))

                Text(scoreA.toString(), fontSize = 30.sp)
                Spacer(modifier = Modifier.padding(30.dp))

                for (player in teamA.players) {
                    Button(
                        onClick = {
                            if (winner != "") return@Button
                            playerOnClick(teamA, player)
                            scoreA = teamA.score

                            if (teamA.isWinner()){
                                winner = teamA.name
                            //     play sound
                                mediaPlayer?.start()
                            }
                        }
                    ) {
                        Text("player ${player.id}", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                }

            }

            Column (modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(30.dp))
                Text(teamB.name, fontSize = 30.sp)
                Spacer(modifier = Modifier.padding(30.dp))

                Text(scoreB.toString(), fontSize = 30.sp)
                Spacer(modifier = Modifier.padding(30.dp))

                for (player in teamB.players) {
                    Button(
                        onClick = {
                            if (winner != "") return@Button
                            playerOnClick(teamB, player)
                            scoreB = teamB.score

                            if (winner == "" && teamB.isWinner()){
                                winner = teamB.name
                                //     play sound
                                mediaPlayer?.start()
                            }
                        }
                    ) {
                        Text("player ${player.id}", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
        }

        Row (
            modifier = Modifier
                .weight(0.5f)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            ){
            OutlinedCard {
                Text(winner, fontSize = 40.sp)
            }

        }

    }


}

@Preview(showBackground = true)
@Composable
fun TeamBattlePreview() {

    // Create teams and players
    val teamA = Team("Team A")
    val teamB = Team("Team B")

    for (i in 1..5) {
        if (i < 4)
            teamA.players.add(Player())
        else
            teamB.players.add(Player())
    }

    TeamBattle(teamA, teamB)
}

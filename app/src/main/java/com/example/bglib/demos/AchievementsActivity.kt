package com.example.bglib.demos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bglib.Achievement
import com.example.bglib.Player
import com.example.bglib.R
import com.example.bglib.ui.theme.BglibTheme

var room = "Bedroom"

class AchievementsActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val player = Player("Juraj")

        val achievements = mutableListOf<Achievement>()

        val a1 = Achievement(
            name = "Easy achievement",
            description = "Earn 10 score",
            imageLocked = R.drawable.locked,
            imageUnlocked = R.drawable.unlocked,
            trigger = {
                player.score >= 10
            },
            reward = {
                Log.d("Demo::Achievement", "Achievement unlocked")
            }
        )
        achievements.add(a1)

        val a2 = Achievement(
            name = "Medium achievement",
            description = "Earn 30 or more score 5 times, while in the kitchen",
            imageLocked = R.drawable.locked,
            imageUnlocked = R.drawable.unlocked,
            trigger = {
                player.deltaScore >= 30
            },
            multiplier = 5,
            reward = {
                Log.d("Demo::Achievement", "Achievement unlocked")
            },
            conditions = listOf {
                room == "Kitchen"
            }
        )
        Log.d("Demo::Achievement", a2.conditions.size.toString())
        achievements.add(a2)

        setContent {
            BglibTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AchievementsScreen(player, achievements)
                }
            }
        }
    }
}

@Composable
fun PrintAchievement(achievement: Achievement){
    Column (
        horizontalAlignment = Alignment.End,
    ) {
        Text(achievement.name, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(5.dp))
        Text(achievement.description, fontSize = 15.sp, modifier = Modifier.padding(10.dp))

        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Column (
                modifier = Modifier.weight(1f),
            ) {
            // TODO: display icon
                val painter = painterResource(id = achievement.image)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .height(75.dp)
                        .width(75.dp)
                        .padding(10.dp)
                        .border(2.dp, Color.Black, CircleShape)
                )
            }

            // State
            Column (
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                if (achievement.unlocked) {
                    Text("Unlocked", fontSize = 20.sp, color = Color.Green)
                } else {
                    Text("Locked", fontSize = 20.sp, color = Color.Red)
                }
            }
        }
    }
}

fun checkAchievements(achievements: List<Achievement>){
    for (achievement in achievements) {
        achievement.check()
    }
}

@Composable
fun AchievementsScreen(player: Player, achievements: List<Achievement>) {
    var score by remember { mutableIntStateOf(player.score) }
    var _room by remember { mutableStateOf(room) }

    Row(
        modifier = Modifier.fillMaxSize().fillMaxWidth()
    ) {
        // Player
        Column (
            modifier = Modifier.weight(1f)
        ){
            Spacer(modifier = Modifier.padding(35.dp))

            Text(player.name, fontSize = 50.sp, modifier = Modifier.padding(10.dp))

            Spacer(modifier = Modifier.padding(10.dp))

            Text("Score: $score", fontSize = 30.sp, modifier = Modifier.padding(10.dp))

            Spacer(modifier = Modifier.padding(10.dp))

            Text("Room: $_room", fontSize = 30.sp, modifier = Modifier.padding(10.dp))

            Button(onClick = {
                if (room == "Bedroom") {
                    room = "Kitchen"
                } else {
                    room = "Bedroom"
                }
                _room = room
            }) {
                Text("Change room", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.padding(50.dp))

            Button(onClick = {
                // Add 10 score
                player.updateScore(10)
                score = player.score
                checkAchievements(achievements)
            }, modifier = Modifier.padding(10.dp)){
                Text("10", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Button(onClick = {
                // Add 30 score
                player.updateScore(30)
                score = player.score
                checkAchievements(achievements)
            }, modifier = Modifier.padding(10.dp)){
                Text("30", fontSize = 30.sp)
            }


        }

        // Achievements
        Column (
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Achievements", fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            Spacer(modifier = Modifier.padding(10.dp))

            for (achievement in achievements) {
                PrintAchievement(achievement)
                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAchievementsScreen(){
    val player = Player("Juraj")

    val achievements = mutableListOf<Achievement>()

    val a1 = Achievement(
        name = "Easy achievement",
        description = "This is a simple achievement",
        imageLocked = R.drawable.locked,
        imageUnlocked =  R.drawable.unlocked,
        trigger = {
            player.score >= 10
        },
    )
    achievements.add(a1)

    val a2 = Achievement(
        name = "Medium achievement",
        description = "Earn 50 score",
        imageLocked = R.drawable.locked,
        imageUnlocked = R.drawable.unlocked,
        trigger = {
            player.score >= 50
        },
        reward = {
            Log.d("Demo::Achievement", "Achievement unlocked")
        }
    )
    achievements.add(a2)

    AchievementsScreen(player, achievements)
}
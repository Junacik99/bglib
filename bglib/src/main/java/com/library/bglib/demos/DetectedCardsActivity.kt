package com.library.bglib.demos

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.library.bglib.classes.Card
import com.library.bglib.classes.GridPosition
import com.library.bglib.ui.theme.BglibTheme
import org.opencv.core.Rect

/************************************************
 * An activity to show detected cards           *
 * from CodenamesDemoActivity.                  *
 ***********************************************/
class DetectedCardsActivity : ComponentActivity() {
    private var numberOfCards = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        numberOfCards = intent.getIntExtra("numberOfCards", 0)
        val cards: List<Card> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("cards", Card::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("cards") ?: emptyList()
        }
        val rows = intent.getIntExtra("rows", 1)
        val cols = intent.getIntExtra("cols", 1)

        Log.d("OCVSample::Activity", "${cards.size} cards: $cards")


        enableEdgeToEdge()
        setContent {
            BglibTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DetectedCards(rows, cols, cards)
                }
            }
        }
    }
}

@Composable
fun DetectedCards(numberOfRows: Int, numberOfCols: Int, cards: List<Card>){
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Cards Result", fontSize = 24.sp)

        Spacer(modifier = Modifier.padding(40.dp))
        Text("Detected cards: $numberOfRows x $numberOfCols")

        Spacer(modifier = Modifier.padding(40.dp))
        for (i in 0 until numberOfRows) {
            Row {
                for (j in 0 until numberOfCols) {
                    val card = cards.firstOrNull { it.GridPosition == GridPosition(i, j) }
                    Text(card!!.text, modifier = Modifier.padding(10.dp))
                }
            }
            Spacer(modifier = Modifier.padding(10.dp))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DetectedCardsPreview() {
    val cards = listOf(
        Card(Rect(0, 0, 100, 100), "Card 1"),
        Card(Rect(100, 0, 100, 100), "Card 2"),
        Card(Rect(20, 0, 35, 100), "Card 3"),
        Card(Rect(15, 0, 42, 100), "Card 4"),
    )

    BglibTheme {
        DetectedCards(2, 2, cards)
    }
}
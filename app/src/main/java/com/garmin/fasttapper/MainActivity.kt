package com.garmin.fasttapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garmin.fasttapper.ui.theme.FastTapperTheme

class MainActivity : ComponentActivity() {
    private val tapperModel: TapperModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastTapperTheme {
                val state = tapperModel.gameState.collectAsState()
                GameScreen(state.value,
                    onStartClick = {
                        tapperModel.startGame()
                    },
                    onTapClick = {
                        tapperModel.onTap(it)
                    },
                    onRestartClick = {
                        tapperModel.restart()
                    })
            }
        }
    }
}

@Composable
fun GameScreen(
    state: GameState,
    onStartClick: () -> Unit,
    onTapClick: (Player) -> Unit,
    onRestartClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (state) {
            is GameState.Starting ->
                StartingScreen(
                    onStartClick = onStartClick
                )

            is GameState.Playing ->
                PlayingScreen(
                    tapsState = state.tapsState,
                    onTapClick = onTapClick
                )

            is GameState.Finished ->
                FinishedScreen(
                    winner = state.winner,
                    winnerScore = state.winnerScore,
                    loserScore = state.loserScore,
                    onRestartClick = onRestartClick
                )
        }
    }
}

@Composable
fun StartingScreen(onStartClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Button(
            onClick = onStartClick,
        ) {
            Text(
                text = "Incepe joc!",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun FinishedScreen(
    winner: Player?,
    winnerScore: Int? = 0,
    loserScore: Int? = 0,
    onRestartClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Player.color(winner)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = winner?.let { "$it a castigat cu $winnerScore la $loserScore" } ?: "Remiza",
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
            Button(
                onClick = onRestartClick,
            ) {
                Text(
                    text = "Reincepe!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
fun PlayingScreen(
    tapsState: Map<Player, Progress>,
    onTapClick: (Player) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        tapsState.forEach { entry ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(entry.value.fraction)
                    .background(Player.color(entry.key))
                    .clickable { onTapClick(entry.key) }
            ) {
                Text(
                    text = "${entry.key}\n\n${entry.value.taps}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .matchParentSize(),
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartingPreview() {
    FastTapperTheme {
        StartingScreen(onStartClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FinishedPreview() {
    FastTapperTheme {
        FinishedScreen(
            winner = null,
            onRestartClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TapsScreenPreview() {
    FastTapperTheme {
        PlayingScreen(
            tapsState = mutableMapOf(Player.BOY to Progress(), Player.GIRL to Progress()),
            onTapClick = {})
    }
}
package com.garmin.fasttapper

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Player {
    BOY, GIRL;

    companion object {
        fun color(tap: Player?) = when (tap) {
            BOY -> Color.Blue
            GIRL -> Color.Magenta
            else -> Color.Gray
        }
    }
}

data class Progress(val taps: Int = 0, val fraction: Float = .5f)

sealed class GameState {
    object Starting : GameState()
    data class Playing(
        val tapsState: Map<Player, Progress> = mapOf(Player.BOY to Progress(), Player.GIRL to Progress()),
    ) : GameState()

    data class Finished(val winner: Player? = null, val winnerScore: Int = 0, val loserScore: Int = 0) : GameState()
}

class TapperModel : ViewModel() {
    private val _tapsCount: MutableStateFlow<Map<Player, Int>> = MutableStateFlow(mapOf())
    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Starting)
    val gameState = _gameState.asStateFlow()

    init {
        viewModelScope.launch {
            _tapsCount.collect { tapsState ->
                _gameState.value =
                    if (tapsState.values.isEmpty()) {
                        GameState.Starting
                    } else {
                        val totalTaps = tapsState.values.sum()
                        if (totalTaps == 0) {
                            GameState.Playing()
                        } else {
                            val winnerEntry = tapsState.entries.firstOrNull { it.value >= REQUIRED_TAPS_TO_WIN }
                            val loserEntry = tapsState.entries.firstOrNull { it.key != winnerEntry?.key }
                            if (winnerEntry != null) {
                                GameState.Finished(winnerEntry.key, winnerEntry.value, loserEntry?.value ?: 0)
                            } else {
                                val ahead = tapsState.entries.maxByOrNull { it.value }
                                val total = 2 * REQUIRED_TAPS_TO_WIN + totalTaps
                                val aheadFraction = (REQUIRED_TAPS_TO_WIN + (ahead?.value ?: 0)).toFloat() / total
                                val behindFraction: Float = 1 - aheadFraction
                                GameState.Playing(
                                    tapsState = tapsState.mapValues {
                                        Progress(
                                            taps = it.value,
                                            fraction = if (ahead != null && ahead.key == it.key) {
                                                aheadFraction
                                            } else {
                                                behindFraction
                                            }
                                        )
                                    })
                            }
                        }
                    }
            }
        }
    }

    fun onTap(tap: Player) = viewModelScope.launch(Dispatchers.IO) {
        val state = _tapsCount.value
        val newState = state.mapValues {
            if (it.key == tap) it.value + 1 else it.value
        }
        _tapsCount.value = newState
    }

    fun startGame() = restart()

    fun restart() {
        _tapsCount.value = mutableMapOf(Player.BOY to 0, Player.GIRL to 0)
    }

    companion object {
        const val REQUIRED_TAPS_TO_WIN = 25
    }
}
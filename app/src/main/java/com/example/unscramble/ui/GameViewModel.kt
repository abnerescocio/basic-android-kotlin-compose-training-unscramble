package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    var userGuess: String by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord: String

    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        reset()
    }

    private fun pick(): String {
        currentWord = allWords.random()

        return if (usedWords.contains(currentWord)) {
            pick()
        } else {
            usedWords.add(currentWord)
            shuffle(currentWord)
        }
    }

    private fun shuffle(word: String): String {
        val temp = word.toCharArray()
        temp.shuffle()
        while (String(temp) == word) {
            temp.shuffle()
        }
        return String(temp)
    }

    fun reset() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pick())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        updateUserGuess(guessedWord = "")
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pick(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore,
                )
            }
        }
    }

    fun skip() {
        updateGameState(_uiState.value.score)
        updateUserGuess(guessedWord = "")
    }
}

package com.davidbadin.kanaread.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.davidbadin.kanaread.data.KanaDatabase
import com.davidbadin.kanaread.data.WordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State exposed to the UI for the practice session.
 */
data class PracticeState(
    val selectedMode: String = "hiragana",
    val currentWord: WordEntity? = null,
    val userInput: String = "",
    val isAnswerChecked: Boolean = false,
    val isCorrect: Boolean = false,
    val sessionWordCount: Int = 0,
    val sessionCorrectCount: Int = 0,
    val sessionSuccessRate: Float = 0f,
    val averageTimePerCorrectWord: Float = 0f,
    val isLoading: Boolean = true
)

/**
 * Owns the practice session: loads words, tracks input, checks answers,
 * updates session statistics, and times correct answers.
 */
class PracticeViewModel(
    private val database: KanaDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state.asStateFlow()

    // Pool of words for the current session.
    private var pool: List<WordEntity> = emptyList()

    // Timing — when the current word was first shown.
    private var currentWordStartMs: Long = 0L

    // Running total of seconds spent on correct answers.
    private var totalCorrectTimeSeconds: Float = 0f

    /**
     * Begin a fresh session in the given mode.
     * mode: "hiragana", "katakana", or "both".
     */
    fun startNewSession(mode: String) {
        _state.update {
            PracticeState(
                selectedMode = mode,
                isLoading = true
            )
        }
        totalCorrectTimeSeconds = 0f

        viewModelScope.launch {
            val dao = database.wordDao()
            pool = when (mode) {
                "hiragana" -> dao.getWordsByType("hiragana")
                "katakana" -> dao.getWordsByType("katakana")
                else -> dao.getAllWords()
            }
            loadNextWord()
        }
    }

    /**
     * Pick a random word from the pool and present it.
     */
    fun loadNextWord() {
        if (pool.isEmpty()) {
            _state.update { it.copy(isLoading = false, currentWord = null) }
            return
        }

        val nextWord = pool.random()
        currentWordStartMs = System.currentTimeMillis()

        _state.update {
            it.copy(
                currentWord = nextWord,
                userInput = "",
                isAnswerChecked = false,
                isCorrect = false,
                isLoading = false
            )
        }
    }

    /**
     * Update the user's typed input.
     */
    fun onInputChange(input: String) {
        if (_state.value.isAnswerChecked) return
        _state.update { it.copy(userInput = input) }
    }

    /**
     * Compare trimmed lowercase user input to the romaji of the current word,
     * update session stats, and (for correct answers) include the elapsed
     * seconds in the running average.
     */
    fun checkAnswer() {
        val current = _state.value
        val word = current.currentWord ?: return
        if (current.isAnswerChecked) return

        val expected = word.romaji.trim().lowercase()
        val actual = current.userInput.trim().lowercase()
        val isCorrect = expected == actual

        val newWordCount = current.sessionWordCount + 1
        val newCorrectCount = if (isCorrect) {
            current.sessionCorrectCount + 1
        } else {
            current.sessionCorrectCount
        }

        if (isCorrect) {
            val elapsedSec = (System.currentTimeMillis() - currentWordStartMs) / 1000f
            totalCorrectTimeSeconds += elapsedSec
        }

        val successRate = if (newWordCount > 0) {
            (newCorrectCount.toFloat() / newWordCount.toFloat()) * 100f
        } else 0f

        val avgTime = if (newCorrectCount > 0) {
            totalCorrectTimeSeconds / newCorrectCount.toFloat()
        } else 0f

        _state.update {
            it.copy(
                isAnswerChecked = true,
                isCorrect = isCorrect,
                sessionWordCount = newWordCount,
                sessionCorrectCount = newCorrectCount,
                sessionSuccessRate = successRate,
                averageTimePerCorrectWord = avgTime
            )
        }
    }

    /**
     * Factory that wires the database into the ViewModel.
     */
    class Factory(private val database: KanaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PracticeViewModel(database) as T
        }
    }
}

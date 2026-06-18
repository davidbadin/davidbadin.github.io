package sk.punkacidetom.pd2026.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.NewsletterRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

data class CountdownState(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
)

data class HomeUiState(
    val phase: FestivalInfo.Phase = FestivalInfo.Phase.BEFORE,
    val countdown: CountdownState = CountdownState(),
    val thankyouText: String = "",
    val language: String = "sk",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    private val userPrefs: UserPreferencesRepository,
    private val newsletterRepository: NewsletterRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _ticker = MutableStateFlow(LocalDateTime.now())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _ticker.value = LocalDateTime.now()
            }
        }
    }

    val isNewsletterAvailable: StateFlow<Boolean> =
        newsletterRepository.observeAnyPublished()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val uiState: StateFlow<HomeUiState> = combine(
        bandRepository.observeFestivalInfo(),
        _ticker,
        userPrefs.language,
    ) { info, now, lang ->
        val phase = info?.phase(now) ?: FestivalInfo.Phase.BEFORE

        val countdown = if (phase == FestivalInfo.Phase.BEFORE && info != null) {
            val start = info.start
            if (!now.isBefore(start)) CountdownState()
            else {
                val dur = Duration.between(now, start)
                val s = dur.seconds
                CountdownState(
                    days = s / 86400,
                    hours = (s % 86400) / 3600,
                    minutes = (s % 3600) / 60,
                    seconds = s % 60,
                )
            }
        } else CountdownState()

        val thankyouText = if (phase == FestivalInfo.Phase.AFTER) {
            val fileName = if (lang.startsWith("en")) "thankyou_en.txt" else "thankyou_sk.txt"
            try { context.assets.open(fileName).bufferedReader().readText() } catch (_: Exception) { "" }
        } else ""

        HomeUiState(phase = phase, countdown = countdown, thankyouText = thankyouText, language = lang)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

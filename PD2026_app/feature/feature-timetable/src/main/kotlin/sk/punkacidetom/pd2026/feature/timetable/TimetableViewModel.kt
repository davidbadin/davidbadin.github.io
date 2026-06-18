package sk.punkacidetom.pd2026.feature.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.FestivalDay
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import java.time.LocalDateTime
import javax.inject.Inject

data class TimetableUiState(
    val days: List<FestivalDay> = emptyList(),
    val selectedDayIndex: Int = 0,
    val stageABands: List<Band> = emptyList(),
    val stageBBands: List<Band> = emptyList(),
    val favouriteIds: Set<Int> = emptySet(),
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedDayIndex = MutableStateFlow(0)

    init {
        // On first data load, default to the current festival day when DURING
        viewModelScope.launch {
            bandRepository.observeFestivalInfo()
                .filterNotNull()
                .take(1)
                .collect { info ->
                    val now = LocalDateTime.now()
                    if (info.phase(now) == FestivalInfo.Phase.DURING) {
                        val today = now.toLocalDate()
                        val idx = info.days.indexOfFirst { it.date == today }
                        if (idx >= 0) _selectedDayIndex.value = idx
                    }
                }
        }
    }

    val uiState: StateFlow<TimetableUiState> = combine(
        bandRepository.observeFestivalInfo(),
        _selectedDayIndex,
        userPrefs.favouriteIds,
    ) { festivalInfo, selectedIndex, favouriteIds ->
        val days = festivalInfo?.days ?: emptyList()
        val effectiveIndex = selectedIndex.coerceIn(0, (days.size - 1).coerceAtLeast(0))
        val selectedDay = days.getOrNull(effectiveIndex)

        TimetableUiState(
            days = days,
            selectedDayIndex = effectiveIndex,
            stageABands = selectedDay?.bands?.filter { it.stageCode == "A" }
                ?.sortedWith(compareBy({ it.startDate }, { it.startTime })) ?: emptyList(),
            stageBBands = selectedDay?.bands?.filter { it.stageCode == "B" }
                ?.sortedWith(compareBy({ it.startDate }, { it.startTime })) ?: emptyList(),
            favouriteIds = favouriteIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimetableUiState())

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
    }

    fun toggleFavourite(bandId: Int) {
        viewModelScope.launch { userPrefs.toggleFavourite(bandId) }
    }
}

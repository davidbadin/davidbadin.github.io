package sk.punkacidetom.pd2026.feature.bands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.model.Band
import javax.inject.Inject

data class BandsUiState(
    val bands: List<Band> = emptyList(),
    val favouriteIds: Set<Int> = emptySet(),
)

@HiltViewModel
class BandsViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<BandsUiState> = combine(
        bandRepository.observeSortedBands(),
        userPrefs.favouriteIds,
    ) { bands, favouriteIds ->
        BandsUiState(bands = bands, favouriteIds = favouriteIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BandsUiState())

    fun toggleFavourite(bandId: Int) {
        viewModelScope.launch { userPrefs.toggleFavourite(bandId) }
    }
}

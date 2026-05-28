package sk.punkacidetom.pd2026.feature.bands

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.navigation.BandDetailRoute
import javax.inject.Inject

data class BandDetailUiState(
    val band: Band? = null,
    val isFavourite: Boolean = false,
    val language: String = "sk",
)

@HiltViewModel
class BandDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bandRepository: BandRepository,
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    private val bandId: Int = savedStateHandle.toRoute<BandDetailRoute>().bandId

    val uiState: StateFlow<BandDetailUiState> = combine(
        bandRepository.observeBands(),
        userPrefs.favouriteIds,
        userPrefs.language,
    ) { bands, favouriteIds, lang ->
        BandDetailUiState(
            band = bands.find { it.id == bandId },
            isFavourite = favouriteIds.contains(bandId),
            language = lang,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BandDetailUiState())

    fun toggleFavourite() {
        viewModelScope.launch { userPrefs.toggleFavourite(bandId) }
    }
}

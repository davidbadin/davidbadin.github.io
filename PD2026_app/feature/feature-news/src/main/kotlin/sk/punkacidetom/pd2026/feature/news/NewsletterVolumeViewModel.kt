package sk.punkacidetom.pd2026.feature.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.NewsletterRepository
import javax.inject.Inject

data class VolumeUiState(
    val volumeId: String = "",
    val pagePaths: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class NewsletterVolumeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val newsletterRepository: NewsletterRepository,
) : ViewModel() {

    private val volumeId: String = checkNotNull(savedStateHandle["volumeId"])

    private val _uiState = MutableStateFlow(VolumeUiState(volumeId = volumeId))
    val uiState: StateFlow<VolumeUiState> = _uiState

    init { loadVolume() }

    private fun loadVolume() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = newsletterRepository.downloadVolume(volumeId)
            if (result.isSuccess) {
                val paths = newsletterRepository.cachedPagePaths(volumeId)
                _uiState.value = _uiState.value.copy(pagePaths = paths, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error",
                )
            }
        }
    }
}

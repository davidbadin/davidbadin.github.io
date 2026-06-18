package sk.punkacidetom.pd2026.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.NewsletterRepository
import sk.punkacidetom.pd2026.core.model.NewsletterVolume
import java.time.LocalDateTime
import javax.inject.Inject

data class NewsUiState(val volumes: List<NewsletterVolume> = emptyList())

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsletterRepository: NewsletterRepository,
) : ViewModel() {

    val uiState: StateFlow<NewsUiState> = newsletterRepository.observeVolumes()
        .map { all -> NewsUiState(volumes = all.filter { it.isPublished(LocalDateTime.now()) }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewsUiState())

    init {
        viewModelScope.launch { newsletterRepository.refreshManifest() }
    }
}

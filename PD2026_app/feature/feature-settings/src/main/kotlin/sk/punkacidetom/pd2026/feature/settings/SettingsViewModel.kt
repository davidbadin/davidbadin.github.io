package sk.punkacidetom.pd2026.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepositoryImpl
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.i18n.LocaleHelper
import javax.inject.Inject

enum class UpdateState { IDLE, UPDATING, SUCCESS, ERROR }

data class SettingsUiState(
    val language: String = "sk",
    val isFontLarge: Boolean = false,
    val updateState: UpdateState = UpdateState.IDLE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefs: UserPreferencesRepository,
    private val bandRepository: BandRepositoryImpl,
    private val localeHelper: LocaleHelper,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPrefs.language,
        userPrefs.isFontLarge,
    ) { lang, fontLarge ->
        SettingsUiState(language = lang, isFontLarge = fontLarge)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _updateState = MutableStateFlow(UpdateState.IDLE)
    val updateState: StateFlow<UpdateState> = _updateState

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            userPrefs.setLanguage(lang)
            localeHelper.applyLocale(lang)
        }
    }

    fun setFontLarge(large: Boolean) {
        viewModelScope.launch {
            userPrefs.setFontLarge(large)
        }
    }

    fun triggerDataUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateState.UPDATING
            val result = bandRepository.forceRefresh()
            _updateState.value = if (result.isSuccess) UpdateState.SUCCESS else UpdateState.ERROR
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.IDLE
    }
}

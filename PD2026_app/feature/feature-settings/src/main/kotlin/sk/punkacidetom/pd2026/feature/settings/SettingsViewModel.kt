package sk.punkacidetom.pd2026.feature.settings

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepositoryImpl
import sk.punkacidetom.pd2026.core.data.repository.NewsletterRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.i18n.LocaleHelper
import sk.punkacidetom.pd2026.core.model.BandNotificationScheduler
import javax.inject.Inject

enum class UpdateState { IDLE, UPDATING, SUCCESS, ERROR }

data class SettingsUiState(
    val language: String = "sk",
    val isFontLarge: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val isExactAlarmPermissionMissing: Boolean = false,
    val updateState: UpdateState = UpdateState.IDLE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPrefs: UserPreferencesRepository,
    private val bandRepository: BandRepositoryImpl,
    private val newsletterRepository: NewsletterRepository,
    private val localeHelper: LocaleHelper,
    private val bandNotificationScheduler: BandNotificationScheduler,
) : ViewModel() {

    // -------------------------------------------------------------------------
    // Exact-alarm permission — refreshed on every ON_RESUME so the button
    // disappears immediately after the user grants the permission and returns.
    // -------------------------------------------------------------------------

    private fun checkExactAlarmPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        else true // permission concept does not exist below API 31

    private val _canScheduleExactAlarms = MutableStateFlow(checkExactAlarmPermission())
    val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    fun refreshExactAlarmPermission() {
        _canScheduleExactAlarms.value = checkExactAlarmPermission()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        userPrefs.language,
        userPrefs.isFontLarge,
        userPrefs.notificationsEnabled,
        userPrefs.exactAlarmPermissionMissing,
    ) { lang, fontLarge, notifEnabled, exactAlarmMissing ->
        SettingsUiState(
            language = lang,
            isFontLarge = fontLarge,
            isNotificationsEnabled = notifEnabled,
            isExactAlarmPermissionMissing = exactAlarmMissing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _updateState = MutableStateFlow(UpdateState.IDLE)
    val updateState: StateFlow<UpdateState> = _updateState

    /**
     * Emits [Unit] after a language change so the screen can call
     * [Activity.recreate()] to apply the new locale immediately on devices
     * where [AppCompatDelegate.setApplicationLocales] alone doesn't trigger a
     * configuration change in the same process lifecycle.
     */
    private val _recreateActivity = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val recreateActivity: SharedFlow<Unit> = _recreateActivity

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            userPrefs.setLanguage(lang)
            localeHelper.applyLocale(lang)
            _recreateActivity.tryEmit(Unit)
            // Reschedule all alarms so new language is baked into the pending intents
            if (userPrefs.notificationsEnabled.first()) {
                bandNotificationScheduler.rescheduleAll()
            }
        }
    }

    fun setFontLarge(large: Boolean) {
        viewModelScope.launch {
            userPrefs.setFontLarge(large)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setNotificationsEnabled(enabled)
            if (enabled) {
                val favouriteIds = userPrefs.favouriteIds.first()
                val allBands     = bandRepository.observeBands().first()
                favouriteIds.forEach { id ->
                    val band = allBands.find { it.id == id } ?: return@forEach
                    bandNotificationScheduler.scheduleNotification(band)
                }
            } else {
                bandNotificationScheduler.cancelAllNotifications()
            }
        }
    }

    fun triggerDataUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateState.UPDATING
            val bandResult       = bandRepository.forceRefresh()
            val newsletterResult = newsletterRepository.refreshManifest()
            _updateState.value =
                if (bandResult.isSuccess && newsletterResult.isSuccess) UpdateState.SUCCESS
                else UpdateState.ERROR
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.IDLE
    }
}

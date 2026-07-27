package sk.punkacidetom.pd2026

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private val _showExactAlarmDialog = MutableStateFlow(false)
    val showExactAlarmDialog: StateFlow<Boolean> = _showExactAlarmDialog.asStateFlow()

    init {
        // Evaluate as early as possible so the dialog is ready before the first frame.
        viewModelScope.launch {
            val permissionMissing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                !alarmManager.canScheduleExactAlarms() else false
            val notShownBefore = !userPrefs.exactAlarmDialogShown.first()
            _showExactAlarmDialog.value = permissionMissing && notShownBefore
        }
    }

    fun onExactAlarmDialogConfirmed() {
        _showExactAlarmDialog.value = false
        viewModelScope.launch { userPrefs.setExactAlarmDialogShown() }
    }

    fun onExactAlarmDialogDismissed() {
        _showExactAlarmDialog.value = false
        viewModelScope.launch { userPrefs.setExactAlarmDialogShown() }
    }
}

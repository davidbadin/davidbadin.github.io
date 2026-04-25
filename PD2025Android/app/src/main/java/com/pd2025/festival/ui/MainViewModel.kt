package com.pd2025.festival.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pd2025.festival.model.Event
import com.pd2025.festival.network.EventRepository
import com.pd2025.festival.utils.PrefsHelper
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentDay = MutableLiveData(1)
    val currentDay: LiveData<Int> = _currentDay

    private lateinit var repository: EventRepository

    fun init(context: Context) {
        repository = EventRepository(context)

        // Load cached data first
        val cached = repository.loadCachedEvents()
        if (cached != null) {
            _events.value = cached
        }

        // Then refresh from network
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchEvents()
            _isLoading.value = false

            result.onSuccess { events ->
                _events.value = events
                _error.value = null
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    fun setDay(day: Int) {
        _currentDay.value = day
    }

    fun toggleFavorite(context: Context, eventId: String) {
        val currentEvents = _events.value?.toMutableList() ?: return
        val idx = currentEvents.indexOfFirst { it.id == eventId }
        if (idx < 0) return

        currentEvents[idx] = currentEvents[idx].copy(
            favorite = !currentEvents[idx].favorite
        )
        _events.value = currentEvents

        // Persist favorites
        val favIds = currentEvents.filter { it.favorite }.map { it.id }.toSet()
        PrefsHelper.saveFavorites(context, favIds)
    }

    fun getEventsForDay(day: Int): List<Event> {
        val all = _events.value ?: return emptyList()
        val dayStart = com.pd2025.festival.utils.DateUtils.getDayStart(day)
        val dayEnd = com.pd2025.festival.utils.DateUtils.getDayEnd(day)
        return all.filter { it.start >= dayStart && it.end <= dayEnd }
    }
}

package com.pd2025.festival.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pd2025.festival.R
import com.pd2025.festival.model.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var dayButtonsContainer: LinearLayout
    private val dayButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerSchedule)
        progressBar = findViewById(R.id.progressBar)
        dayButtonsContainer = findViewById(R.id.dayButtonsContainer)

        setupAdapter()
        setupDayButtons()
        setupSpotifyButton()
        observeViewModel()

        viewModel.init(this)
    }

    private fun setupAdapter() {
        scheduleAdapter = ScheduleAdapter { event ->
            EventDetailBottomSheet.newInstance(
                event = event,
                onFavToggle = { eventId ->
                    viewModel.toggleFavorite(this, eventId)
                },
                onDismissed = {}
            ).show(supportFragmentManager, "event_detail")
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = scheduleAdapter
    }

    private fun setupDayButtons() {
        dayButtonsContainer.removeAllViews()
        dayButtons.clear()

        Constants.DAYS.forEach { day ->
            val btn = Button(this).apply {
                text = day.name
                tag = day.number
                isAllCaps = false
                textSize = 14f
                setPadding(32, 16, 32, 16)
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.setMargins(4, 0, 4, 0) }
                setOnClickListener {
                    viewModel.setDay(day.number)
                }
            }
            dayButtons.add(btn)
            dayButtonsContainer.addView(btn)
        }
    }

    private fun setupSpotifyButton() {
        val btnSpotify = findViewById<Button>(R.id.btnSpotifyPlaylist)
        btnSpotify.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SPOTIFY_PLAYLIST))
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.currentDay.observe(this) { day ->
            updateDayButtonStyles(day)
            refreshSchedule(day)
        }

        viewModel.events.observe(this) { _ ->
            val day = viewModel.currentDay.value ?: 1
            refreshSchedule(day)
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun refreshSchedule(day: Int) {
        val events = viewModel.getEventsForDay(day)
        scheduleAdapter.submitData(events, day)

        // Scroll to current hour if today's day is selected
        val dayStart = com.pd2025.festival.utils.DateUtils.getDayStart(day)
        val dayEnd = com.pd2025.festival.utils.DateUtils.getDayEnd(day)
        val now = java.util.Date()
        if (now >= dayStart && now <= dayEnd) {
            val elapsedMinutes = ((now.time - dayStart.time) / 60000).toInt()
            val density = resources.displayMetrics.density
            val minuteHeightPx = (2f * density).toInt()
            recyclerView.post {
                recyclerView.smoothScrollBy(0, elapsedMinutes * minuteHeightPx)
            }
        }
    }

    private fun updateDayButtonStyles(activeDay: Int) {
        dayButtons.forEach { btn ->
            val dayNum = btn.tag as Int
            if (dayNum == activeDay) {
                btn.setBackgroundResource(R.drawable.button_yellow)
                btn.setTextColor(ContextCompat.getColor(this, R.color.day_button_active_text))
            } else {
                btn.setBackgroundResource(R.drawable.button_grey)
                btn.setTextColor(ContextCompat.getColor(this, R.color.day_button_text))
            }
        }
    }
}

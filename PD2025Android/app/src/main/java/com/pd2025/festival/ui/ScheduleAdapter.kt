package com.pd2025.festival.ui

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pd2025.festival.R
import com.pd2025.festival.model.Constants
import com.pd2025.festival.model.Event
import com.pd2025.festival.utils.DateUtils
import java.util.Calendar
import java.util.Date

class ScheduleAdapter(
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.RowViewHolder>() {

    // Each row = one hour slot, columns = stages
    private var events: List<Event> = emptyList()
    private var dayStart: Date = Date()
    private var dayEnd: Date = Date()

    // px per minute
    private val minuteHeightDp = 2f

    fun submitData(events: List<Event>, dayNumber: Int) {
        this.events = events
        this.dayStart = DateUtils.getDayStart(dayNumber)
        this.dayEnd = DateUtils.getDayEnd(dayNumber)
        notifyDataSetChanged()
    }

    // We use a single ViewHolder that draws the whole schedule as a custom view
    override fun getItemCount() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(events, dayStart, dayEnd, minuteHeightDp, onEventClick)
    }

    class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val containerStageA: LinearLayout = view.findViewById(R.id.columnStageA)
        private val containerStageB: LinearLayout = view.findViewById(R.id.columnStageB)
        private val hoursColumn: LinearLayout = view.findViewById(R.id.columnHours)

        fun bind(
            events: List<Event>,
            dayStart: Date,
            dayEnd: Date,
            minuteHeightDp: Float,
            onEventClick: (Event) -> Unit
        ) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val minuteHeightPx = (minuteHeightDp * density).toInt()

            // Calculate total minutes
            val totalMinutes = ((dayEnd.time - dayStart.time) / 60000).toInt()

            // Clear all columns
            hoursColumn.removeAllViews()
            containerStageA.removeAllViews()
            containerStageB.removeAllViews()

            // Build hours column
            val cal = Calendar.getInstance()
            cal.time = dayStart
            var elapsed = 0
            while (elapsed <= totalMinutes) {
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val tv = TextView(context).apply {
                    text = hour.toString()
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    textSize = 10f
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        60 * minuteHeightPx
                    )
                    gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                    setPadding(0, 4, 0, 0)
                }
                hoursColumn.addView(tv)
                cal.add(Calendar.HOUR_OF_DAY, 1)
                elapsed += 60
            }

            // Build stage columns
            buildStageColumn(
                containerStageA,
                events.filter { it.stage == "A" },
                dayStart,
                dayEnd,
                minuteHeightPx,
                isStageA = true,
                onEventClick
            )
            buildStageColumn(
                containerStageB,
                events.filter { it.stage == "B" },
                dayStart,
                dayEnd,
                minuteHeightPx,
                isStageA = false,
                onEventClick
            )
        }

        private fun buildStageColumn(
            container: LinearLayout,
            stageEvents: List<Event>,
            dayStart: Date,
            dayEnd: Date,
            minuteHeightPx: Int,
            isStageA: Boolean,
            onEventClick: (Event) -> Unit
        ) {
            val context = container.context
            val totalMinutes = ((dayEnd.time - dayStart.time) / 60000).toInt()

            // Add spacer at top
            container.addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 4
                )
            })

            var prevEnd = dayStart

            for (event in stageEvents.sortedBy { it.start }) {
                // Gap before event
                val gapMinutes = ((event.start.time - prevEnd.time) / 60000).toInt()
                if (gapMinutes > 0) {
                    container.addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            gapMinutes * minuteHeightPx
                        )
                    })
                }

                // Event block
                val eventMinutes = ((event.end.time - event.start.time) / 60000).toInt()
                val eventHeightPx = eventMinutes * minuteHeightPx

                val eventView = LayoutInflater.from(context)
                    .inflate(R.layout.item_event, container, false)

                val tvBand = eventView.findViewById<TextView>(R.id.tvBandName)
                val tvDescr = eventView.findViewById<TextView>(R.id.tvEventDescr)
                val tvFav = eventView.findViewById<TextView>(R.id.tvFavorite)
                val topBar = eventView.findViewById<View>(R.id.viewTopBar)
                val bottomBar = eventView.findViewById<View>(R.id.viewBottomBar)
                val contentArea = eventView.findViewById<View>(R.id.viewContent)

                tvBand.text = event.band
                val descrText = if (!event.genre.isNullOrBlank()) {
                    event.shortDescription + "\n" + event.genre
                } else {
                    event.shortDescription
                }
                tvDescr.text = descrText
                tvFav.text = if (event.favorite) "♥" else ""

                // Apply stage colors/drawables
                if (isStageA) {
                    topBar.setBackgroundResource(R.drawable.event_yellow_top)
                    bottomBar.setBackgroundResource(R.drawable.event_yellow_bot)
                    contentArea.setBackgroundResource(R.drawable.event_yellow_mid)
                    tvBand.setTextColor(ContextCompat.getColor(context, R.color.stage_a_text))
                    tvDescr.setTextColor(ContextCompat.getColor(context, R.color.stage_a_text))
                    tvFav.setTextColor(ContextCompat.getColor(context, R.color.stage_a_text))
                } else {
                    topBar.setBackgroundResource(R.drawable.event_grey_top)
                    bottomBar.setBackgroundResource(R.drawable.event_grey_bot)
                    contentArea.setBackgroundResource(R.drawable.event_grey_mid)
                    tvBand.setTextColor(ContextCompat.getColor(context, R.color.stage_b_text))
                    tvDescr.setTextColor(ContextCompat.getColor(context, R.color.stage_b_text))
                    tvFav.setTextColor(ContextCompat.getColor(context, R.color.stage_b_text))
                }

                // Highlight currently playing
                val now = Date()
                if (now >= event.start && now <= event.end) {
                    eventView.alpha = 1.0f
                    eventView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.highlight_current)
                    )
                }

                eventView.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    eventHeightPx
                )
                eventView.setOnClickListener { onEventClick(event) }

                container.addView(eventView)
                prevEnd = event.end
            }

            // Fill remaining time
            val remainingMinutes = ((dayEnd.time - prevEnd.time) / 60000).toInt()
            if (remainingMinutes > 0) {
                container.addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        remainingMinutes * minuteHeightPx
                    )
                })
            }
        }
    }
}

/**
 * Copyright (c) 2018 Microsoft Inc. All rights reserved.
 */

package com.microsoft.officeuifabric.datetimepicker

import android.content.Context
import android.support.design.widget.TabLayout
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.managers.PreferencesManager
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabric.util.DateTimeUtils
import com.microsoft.officeuifabric.view.NumberPicker
import kotlinx.android.synthetic.main.view_date_time_picker.view.*

import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

// TODO support date only mode for accessibility
/**
 * [DateTimePicker] houses [NumberPicker]s that allow users to pick dates, times and periods(12 hour clocks)
 */
internal class DateTimePicker : LinearLayout, NumberPicker.OnValueChangeListener {
    companion object {
        // The number of months the calendar goes back from the current date
        private const val MONTH_BACK = 3L
        // The number of months the calendar goes forward from the current date
        private const val MONTH_AHEAD = 12L
        private const val MAX_HOURS_24_CLOCK = 23
        private const val MAX_HOURS_12_CLOCK = 12
        private const val MIN_HOURS_24_CLOCK = 0
        private const val MIN_HOURS_12_CLOCK = 1
        private const val MAX_MINUTES = 59
        private const val MIN_MINUTES = 0
        private const val MIN_PERIOD = 0
        private const val MAX_PERIOD = 1
    }

    enum class Tab {
        START_TIME, END_TIME, NO_DURATION
    }

    var timeSlot: TimeSlot? = null
        get() {
            val updatedTime = computeTime()

            if (selectedTab == Tab.START_TIME)
                dateTime = updatedTime
            else
                duration = if (updatedTime.isBefore(dateTime)) Duration.ZERO else Duration.between(dateTime, updatedTime)

            return TimeSlot(dateTime, duration)
        }
        set(value) {
            field = value
            value?.let {
                dateTime = value.start
                duration = value.duration
                displayTime(selectedTab == Tab.START_TIME, false)
            }
        }

    val selectedTab: Tab
        get() = Tab.values()[start_end_time_tab.selectedTabPosition]

    var onDateTimeSelectedListener: OnDateTimeSelectedListener? = null

    private var dateTime: ZonedDateTime = ZonedDateTime.now()
    private var duration: Duration = Duration.ZERO
    private val is24Hour: Boolean
    private var daysBack: Int = 0
    private var daysForward: Int = 0

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        // TODO revisit when implementing duration
        override fun onTabSelected(tab: TabLayout.Tab) {
            // Adjust start time and duration when switching tabs
            if (tab.tag === Tab.START_TIME) {
                date_picker.maxValue = daysBack + daysForward
                displayTime(true, true)
            } else {
                // Since simple time picker shows 28 hours, end time of alt time picker should be extended to the next day
                date_picker.maxValue = daysBack + daysForward + 1
                displayTime(false, true)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) { }

        override fun onTabReselected(tab: TabLayout.Tab) { }
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_date_time_picker, this, true)
        is24Hour = DateFormat.is24HourFormat(context)
    }

    /**
     * Selects either start or end tab, or in the case of NO_DURATION hides the tabs
     */
    fun selectTab(tab: Tab) {
        if (tab == Tab.NO_DURATION)
            start_end_time_tab.visibility = View.GONE
        else
            with(start_end_time_tab) {
                removeOnTabSelectedListener(onTabSelectedListener)
                getTabAt(tab.ordinal)?.select()
                addOnTabSelectedListener(onTabSelectedListener)
                visibility = View.VISIBLE
            }
    }

    /**
     * Sets time depending on start/end mode as well as determines whether animation is to be applied
     * to the [NumberPicker]s
     */
    fun displayTime(showStartTime: Boolean, animate: Boolean) {
        val time = if (showStartTime) dateTime else dateTime.plus(duration)
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(today, time).toInt()
        val dateValue = daysBack + daysBetween
        val hourValue = if (is24Hour)
            time.hour
        else if (time.hour == MAX_HOURS_12_CLOCK)
            time.hour
        else
            time.hour % MAX_HOURS_12_CLOCK
        val minuteValue = time.minute
        val ampmValue = if (time.hour < 12) 0 else 1

        if (animate) {
            date_picker.quicklyAnimateValueTo(dateValue)
            hour_picker.animateValueTo(hourValue)
            minute_picker.quicklyAnimateValueTo(minuteValue)
            if (!is24Hour)
                period_picker.animateValueTo(ampmValue)
        } else {
            date_picker.value = dateValue
            hour_picker.value = hourValue
            minute_picker.value = minuteValue
            if (!is24Hour)
                period_picker.value = ampmValue
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        with(start_end_time_tab) {
            addTab(start_end_time_tab.newTab().setText(R.string.date_time_picker_start_time).setTag(Tab.START_TIME))
            addTab(start_end_time_tab.newTab().setText(R.string.date_time_picker_end_time).setTag(Tab.END_TIME))
            addOnTabSelectedListener(onTabSelectedListener)
            tabGravity = TabLayout.GRAVITY_FILL
        }

        initDatePicker()
        initHourPicker()
        initMinutePicker()
        initPeriodPicker()
    }

    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        timeSlot?.let { onDateTimeSelectedListener?.onDateTimeSelected(it.start) }
    }

    private fun computeTime(): ZonedDateTime {
        val now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)

        val dayDiff = date_picker.value - daysBack
        var hour = hour_picker.value
        val minute = minute_picker.value

        if (!is24Hour) {
            val isMorning = period_picker.value == 0
            if (hour == MAX_HOURS_12_CLOCK) {
                hour = if (isMorning) 0 else MAX_HOURS_12_CLOCK
            } else {
                hour += if (isMorning) 0 else MAX_HOURS_12_CLOCK
            }
        }

        return now.plusDays(dayDiff.toLong()).withHour(hour).withMinute(minute)
    }

    private fun initDatePicker() {
        val firstDayOfWeek = PreferencesManager.getWeekStart(context)
        val today = LocalDate.now()
        var minWindowRange = today.minusMonths(MONTH_BACK)
        minWindowRange = DateTimeUtils.roundToLastWeekend(minWindowRange, firstDayOfWeek)
        var maxWindowRange = today.plusMonths(MONTH_AHEAD)
        maxWindowRange = DateTimeUtils.roundToNextWeekend(maxWindowRange, firstDayOfWeek)

        daysBack = ChronoUnit.DAYS.between(minWindowRange, today).toInt()
        daysForward = ChronoUnit.DAYS.between(today, maxWindowRange).toInt()

        with(date_picker) {
            minValue = 0
            maxValue = daysBack + daysForward
            value = daysBack
            setFormatter(DateFormatter(context, today, daysBack))
            setOnValueChangedListener(this@DateTimePicker)
        }

    }

    private fun initHourPicker() {
        if (is24Hour) {
            hour_picker.minValue = MIN_HOURS_24_CLOCK
            hour_picker.maxValue = MAX_HOURS_24_CLOCK
        } else {
            hour_picker.minValue = MIN_HOURS_12_CLOCK
            hour_picker.maxValue = MAX_HOURS_12_CLOCK
        }

        hour_picker.setOnValueChangedListener(this)
    }

    private fun initMinutePicker() {
        with(minute_picker) {
            minValue = MIN_MINUTES
            maxValue = MAX_MINUTES
            setFormatter(NumberPicker.twoDigitFormatter)
            setOnValueChangedListener(this@DateTimePicker)
        }
    }

    private fun initPeriodPicker() {
        with(period_picker) {
            minValue = MIN_PERIOD
            maxValue = MAX_PERIOD
            displayedValues = DateStringUtils.amPmStrings
            visibility = if (is24Hour) View.GONE else View.VISIBLE
            setOnValueChangedListener(this@DateTimePicker)
        }
    }

    private class DateFormatter(
        private val context: Context,
        private val today: LocalDate,
        private val todayIndex: Int
    ) : android.widget.NumberPicker.Formatter {
        private val todayString = context.getString(R.string.today)
        private val tomorrowString = context.getString(R.string.tomorrow)
        private val yesterdayString = context.getString(R.string.yesterday)

        override fun format(value: Int): String =
            when (value) {
                todayIndex -> todayString
                todayIndex + 1 -> tomorrowString
                todayIndex - 1 -> yesterdayString
                else -> {
                    val today = LocalDate.now()
                    val date = this.today.plusDays((value - todayIndex).toLong())
                    if (DateTimeUtils.isSameYear(today, date)) {
                        DateStringUtils.formatDateAbbrevAll(context, date)
                    } else {
                        DateStringUtils.formatWeekdayDateYearAbbrev(context, date)
                    }
                }
            }
    }
}

internal interface OnDateTimeSelectedListener {
    /**
     * Method called when a user changes a date and time
     * @param [dateTime] the selected date and time
     */
    fun onDateTimeSelected(dateTime: ZonedDateTime)
}
/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
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
import org.threeten.bp.YearMonth
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.text.DateFormatSymbols

/**
 * [DateTimePicker] houses [NumberPicker]s that allow users to pick dates, times and periods (12 hour clocks).
 * When [PickerMode.DATE] is the pickerMode months, days and years are shown.
 */
internal class DateTimePicker : LinearLayout, NumberPicker.OnValueChangeListener {
    companion object {
        private const val MONTH_LIMIT = 1200L
        private const val MIN_DAYS = 1
        private const val MAX_HOURS_24_CLOCK = 23
        private const val MAX_HOURS_12_CLOCK = 12
        private const val MIN_HOURS_24_CLOCK = 0
        private const val MIN_HOURS_12_CLOCK = 1
        private const val MAX_MINUTES = 59
        private const val MIN_MINUTES = 0
        private const val MIN_MONTHS = 1
        private const val MIN_PERIOD = 0
        private const val MAX_PERIOD = 1
    }

    enum class Tab {
        START, END, NONE
    }

    enum class PickerMode {
        DATE, DATE_TIME
    }

    val selectedTab: Tab
        get() = Tab.values()[start_end_tabs.selectedTabPosition]

    var timeSlot: TimeSlot? = null
        get() {
            val updatedTime = pickerValue

            if (selectedTab == Tab.START)
                dateTime = updatedTime
            else
                duration = if (updatedTime.isBefore(dateTime)) Duration.ZERO else Duration.between(dateTime, updatedTime)

            return TimeSlot(dateTime, duration)
        }
        set(value) {
            field = value
            value?.let {
                dateTime = value.start.truncatedTo(ChronoUnit.MINUTES)
                duration = value.duration
                setPickerValues(selectedTab == Tab.END, false)
            }
        }

    /**
     * Determines whether date and time are shown or just date.
     */
    var pickerMode: PickerMode = PickerMode.DATE_TIME
        set(value) {
            field = value
            when (pickerMode) {
                PickerMode.DATE -> initDateNumberPickers()
                PickerMode.DATE_TIME -> initDateTimeNumberPickers()
            }
        }

    var onDateTimeSelectedListener: OnDateTimeSelectedListener? = null

    private val pickerValue: ZonedDateTime
        get() = when (pickerMode) {
            PickerMode.DATE -> datePickerValue
            PickerMode.DATE_TIME -> dateTimePickerValue
        }

    private var dateTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    private var duration: Duration = Duration.ZERO
    private val is24Hour: Boolean
    private var daysBack: Int = 0
    private var daysForward: Int = 0

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            // Adjust start time and duration when switching tabs
            setPickerValues(tab.tag === Tab.END, true)
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
     * Selects either START or END tab, or in the case of NONE hides the tabs.
     */
    fun selectTab(tab: Tab) {
        if (tab == Tab.NONE) {
            start_end_tabs.visibility = View.GONE
        } else {
            with(start_end_tabs) {
                removeOnTabSelectedListener(onTabSelectedListener)
                getTabAt(tab.ordinal)?.select()
                addOnTabSelectedListener(onTabSelectedListener)
                visibility = View.VISIBLE
            }
        }
    }

    /**
     * Sets the pickers values
     * @param showEndTime is a flag that sets the [NumberPicker]s' end date / time, start date / time or,
     * in the case of no duration, a selected date / time.
     * @param animate is a flag that sets whether the [NumberPicker]s animate to their set values.
     */
    fun setPickerValues(showEndTime: Boolean, animate: Boolean) {
        when (pickerMode) {
            PickerMode.DATE -> setDatePickerValues(showEndTime, animate)
            PickerMode.DATE_TIME -> setDateTimePickerValues(showEndTime, animate)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        with(start_end_tabs) {
            addTab(newTab())
            addTab(newTab())
            addOnTabSelectedListener(onTabSelectedListener)
        }

        getTab(Tab.START)?.tag = Tab.START
        getTab(Tab.END)?.tag = Tab.END
    }

    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        if (pickerMode == PickerMode.DATE)
            updateDayPicker()

        timeSlot?.let { onDateTimeSelectedListener?.onDateTimeSelected(it.start, it.duration) }
    }

    private fun getTab(tab: Tab): TabLayout.Tab? = start_end_tabs.getTabAt(tab.ordinal)

    // Date Time NumberPickers

    private val dateTimePickerValue: ZonedDateTime
        get() {
            val now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val dayDiff = date_picker.value - daysBack
            var hour = hour_picker.value
            val minute = minute_picker.value

            if (!is24Hour) {
                val isMorning = period_picker.value == 0
                val periodStartHour = if (isMorning) 0 else MAX_HOURS_12_CLOCK
                if (hour == MAX_HOURS_12_CLOCK)
                    hour = periodStartHour
                else
                    hour += periodStartHour
            }

            return now.plusDays(dayDiff.toLong()).withHour(hour).withMinute(minute)
        }

    private fun initDateTimeNumberPickers() {
        getTab(Tab.START)?.setText(R.string.date_time_picker_start_time)
        getTab(Tab.END)?.setText(R.string.date_time_picker_end_time)

        date_time_pickers.visibility = View.VISIBLE

        initDatePicker()
        initHourPicker()
        initMinutePicker()
        initPeriodPicker()
    }

    private fun initDatePicker() {
        val firstDayOfWeek = PreferencesManager.getWeekStart(context)
        val today = LocalDate.now()
        var minWindowRange = today.minusMonths(MONTH_LIMIT)
        minWindowRange = DateTimeUtils.roundToLastWeekend(minWindowRange, firstDayOfWeek)
        var maxWindowRange = today.plusMonths(MONTH_LIMIT)
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

    private fun setDateTimePickerValues(showEndTime: Boolean, animate: Boolean) {
        val time = if (showEndTime) dateTime.plus(duration) else dateTime
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(today, time).toInt()
        val dateValue = daysBack + daysBetween
        val hourValue = when {
            is24Hour || time.hour == MAX_HOURS_12_CLOCK -> time.hour
            else -> time.hour % MAX_HOURS_12_CLOCK
        }
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

    // Date NumberPickers

    private val datePickerValue: ZonedDateTime
        get() = ZonedDateTime.now().withYear(year_picker.value).withMonth(month_picker.value).withDayOfMonth(day_picker.value)

    private fun initDateNumberPickers() {
        getTab(Tab.START)?.setText(R.string.date_time_picker_start_date)
        getTab(Tab.END)?.setText(R.string.date_time_picker_end_date)

        date_pickers.visibility = View.VISIBLE

        initMonthPicker()
        initDayPicker()
        initYearPicker()
    }

    private fun initMonthPicker() {
        val months = DateFormatSymbols().months
        with(month_picker) {
            minValue = MIN_MONTHS
            maxValue = months.size
            displayedValues = months
            setOnValueChangedListener(this@DateTimePicker)
        }
    }

    private fun initDayPicker() {
        updateDayPicker()
        day_picker.setOnValueChangedListener(this@DateTimePicker)
    }

    private fun initYearPicker() {
        val today = LocalDate.now()
        with(year_picker) {
            minValue = today.minusMonths(MONTH_LIMIT).year
            maxValue = today.plusMonths(MONTH_LIMIT).year
            wrapSelectorWheel = false
            setOnValueChangedListener(this@DateTimePicker)
        }
    }

    private fun setDatePickerValues(showEndTime: Boolean, animate: Boolean) {
        val time = if (showEndTime) dateTime.plus(duration) else dateTime

        if (animate) {
            month_picker.quicklyAnimateValueTo(time.monthValue)
            day_picker.quicklyAnimateValueTo(time.dayOfMonth)
            year_picker.animateValueTo(time.year)
        } else {
            month_picker.value = time.monthValue
            day_picker.value = time.dayOfMonth
            year_picker.value = time.year
        }
    }

    private fun updateDayPicker() {
        val yearMonth = YearMonth.of(year_picker.value, month_picker.value)
        with(day_picker) {
            minValue = MIN_DAYS
            maxValue = yearMonth.lengthOfMonth()
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
                    if (DateTimeUtils.isSameYear(today, date))
                        DateStringUtils.formatDateAbbrevAll(context, date)
                    else
                        DateStringUtils.formatWeekdayDateYearAbbrev(context, date)
                }
            }
    }
}

internal interface OnDateTimeSelectedListener {
    /**
     * Method called when a user selects a date time range
     * @param [dateTime] the selected date and time
     * @param [duration] the duration of a date range
     */
    fun onDateTimeSelected(dateTime: ZonedDateTime, duration: Duration)
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datetimepicker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.calendar.OnDateSelectedListener
import com.microsoft.officeuifabric.calendar.CalendarView
import com.microsoft.officeuifabric.util.DateStringUtils
import kotlinx.android.synthetic.main.fragment_date_picker.*

import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * [DatePickerFragment] houses a [CalendarView] instance and configures aspects of the view
 */
internal class DatePickerFragment : Fragment(), OnDateSelectedListener {
    val fullModeHeight: Int
        get() = calendar_view.fullModeHeight

    var onDateSelectedListener: OnDateSelectedListener? = null

    private lateinit var date: ZonedDateTime
    private lateinit var duration: Duration
    private lateinit var datePickMode: DatePickMode

    fun setTimeSlot(timeSlot: TimeSlot) {
        date = timeSlot.start
        duration = timeSlot.duration
        calendar_view.setSelectedDateRange(date.toLocalDate(), duration, datePickMode == DatePickMode.RANGE_END)
    }

    fun expandCalendarView() {
        calendar_view.setDisplayMode(CalendarView.DisplayMode.LENGTHY_MODE, true)
    }

    fun collapseCalendarView() {
        calendar_view.leaveLengthyMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = savedInstanceState ?: arguments ?: return
        date = bundle.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
        duration = bundle.getSerializable(DateTimePickerExtras.DURATION) as Duration
        datePickMode = bundle.getSerializable(DateTimePickerExtras.DATE_PICK_MODE) as DatePickMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar_view.setSelectedDateRange(date.toLocalDate(), duration, datePickMode == DatePickMode.SINGLE)
        calendar_view.onDateSelectedListener = this
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putSerializable(DateTimePickerExtras.DATE_TIME, date)
        bundle.putSerializable(DateTimePickerExtras.DURATION, duration)
        bundle.putSerializable(DateTimePickerExtras.DATE_PICK_MODE, datePickMode)
    }

    override fun onDateSelected(date: ZonedDateTime) {
        when (datePickMode) {
            DatePickMode.SINGLE -> {
                this.date = date
                duration = Duration.ZERO
            }
            DatePickMode.RANGE_START -> {
                this.date = date
            }
            DatePickMode.RANGE_END -> {
                if (date.isBefore(this.date)) {
                    this.date = date.minus(duration)
                } else {
                    duration = Duration.between(this.date, date)
                }
            }
        }

        onDateSelectedListener?.onDateSelected(this.date)

        if (title.visibility == View.VISIBLE)
            context?.let { title.text = DateStringUtils.formatDateAbbrevAll(it, this.date) }
    }
}

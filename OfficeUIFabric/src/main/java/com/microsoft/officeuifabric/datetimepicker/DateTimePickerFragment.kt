/**
 * Copyright (c) 2018 Microsoft Inc. All rights reserved.
 */

package com.microsoft.officeuifabric.datetimepicker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.util.DateTimeUtils
import kotlinx.android.synthetic.main.fragment_date_time_picker.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * [DateTimePickerFragment] houses a [DateTimePicker] instance and configures aspects of the view
 */
internal class DateTimePickerFragment : Fragment(), OnDateTimeSelectedListener {
    var onDateTimeSelectedListener: OnDateTimeSelectedListener? = null

    private var datePickMode: DatePickMode? = null

    fun setDate(date: ZonedDateTime) {
        val range = date_time_picker.timeSlot ?: return
        if (DateTimeUtils.isSameDay(date, range.start))
            return
        date_time_picker.timeSlot = TimeSlot(range.start.with(date.toLocalDate()), range.duration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = savedInstanceState ?: arguments ?: return
        datePickMode = bundle.getSerializable(DateTimePickerExtras.DATE_PICK_MODE) as DatePickMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_time_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = savedInstanceState ?: arguments
        if (bundle != null) {
            val dateTime = bundle.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
            val duration = bundle.getSerializable(DateTimePickerExtras.DURATION) as Duration
            date_time_picker.timeSlot = TimeSlot(dateTime, duration)
        }
        date_time_picker.onDateTimeSelectedListener = this

        initUI()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)

        if (datePickMode != DatePickMode.SINGLE) {
            datePickMode = if (date_time_picker.selectedTab == DateTimePicker.Tab.START_TIME)
                DatePickMode.RANGE_START
            else
                DatePickMode.RANGE_END
        }

        bundle.putSerializable(DateTimePickerExtras.DATE_PICK_MODE, datePickMode)
        bundle.putSerializable(DateTimePickerExtras.DATE_TIME, date_time_picker.timeSlot?.start)
        bundle.putSerializable(DateTimePickerExtras.DURATION, date_time_picker.timeSlot?.duration)
    }

    override fun onDateTimeSelected(dateTime: ZonedDateTime) {
        onDateTimeSelectedListener?.onDateTimeSelected(dateTime)
    }

    private fun initUI() {
        when (datePickMode) {
            DatePickMode.RANGE_START -> {
                date_time_picker.selectTab(DateTimePicker.Tab.START_TIME)
                date_time_picker.displayTime(true, false)
            }
            DatePickMode.RANGE_END -> {
                date_time_picker.selectTab(DateTimePicker.Tab.END_TIME)
                date_time_picker.displayTime(false, false)
            }
            DatePickMode.SINGLE -> {
                date_time_picker.selectTab(DateTimePicker.Tab.NO_DURATION)
                date_time_picker.displayTime(true, false)
            }
        }
    }
}

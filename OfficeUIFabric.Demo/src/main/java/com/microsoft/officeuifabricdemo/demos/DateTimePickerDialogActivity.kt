/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.widget.Button
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog.Mode
import com.microsoft.officeuifabric.datetimepicker.DatePickMode
import com.microsoft.officeuifabric.datetimepicker.OnDateTimePickedListener
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_date_time_picker_dialog.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class DateTimePickerDialogActivity : DemoActivity(), OnDateTimePickedListener {
    companion object {
        private const val TAG_DATE_TIME_PICKER = "dateTimePicker"
        private const val TAG_DATE_PICKER = "datePicker"
        private const val TAG_END_DATE_PICKER = "endDatePicker"
        private const val TAG_START_DATE_PICKER = "startDatePicker"

        private const val DURATION = "duration"
        private const val START_DATE = "startDate"
        private const val SINGLE_DATE_TIME = "singleDateTime"

        private const val SINGLE_MODE_TAG = "singleModeDialogTag"
    }

    enum class DatePickerType(val buttonId: Int, val tag: String, val mode: Mode, val datePickMode: DatePickMode) {
        DATE(R.id.date_picker_button, TAG_DATE_PICKER, Mode.DATE, DatePickMode.SINGLE),
        DATE_TIME(R.id.date_time_picker_date_selected_button, TAG_DATE_TIME_PICKER, Mode.DATE_TIME, DatePickMode.SINGLE),
        TIME_DATE(R.id.date_time_picker_time_selected_button, TAG_DATE_TIME_PICKER, Mode.TIME_DATE, DatePickMode.SINGLE),
        START_DATE(R.id.date_range_start_button, TAG_START_DATE_PICKER, Mode.DATE, DatePickMode.RANGE_START),
        END_DATE(R.id.date_range_end_button, TAG_END_DATE_PICKER, Mode.DATE, DatePickMode.RANGE_END)
    }

    override val contentLayoutId: Int
        get() = R.layout.activity_date_time_picker_dialog

    private var singleDateTime: ZonedDateTime? = null
    private var startDate: ZonedDateTime? = null
    private var duration: Duration = Duration.ZERO
    private var datePickMode: DatePickMode = DatePickMode.SINGLE
    private var singleModeDialogTag: String? = null
        set(value) {
            if (datePickMode == DatePickMode.SINGLE) {
                field = value
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            singleDateTime = it.getSerializable(SINGLE_DATE_TIME) as? ZonedDateTime
            startDate = it.getSerializable(START_DATE) as? ZonedDateTime
            duration = it.getSerializable(DURATION) as Duration
            singleModeDialogTag = it.getString(SINGLE_MODE_TAG)

            updateSingleDateTimeText()
            updateDateRangeText()
        }

        DatePickerType.values().forEach { picker ->
            findViewById<Button>(picker.buttonId).setOnClickListener {
                datePickMode = picker.datePickMode
                singleModeDialogTag = picker.tag
                val dialog = DateTimePickerDialog.newInstance(
                    getDateTime(),
                    getDuration(),
                    picker.mode,
                    picker.datePickMode
                )
                dialog.show(supportFragmentManager, picker.tag)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SINGLE_MODE_TAG, singleModeDialogTag)
        outState.putSerializable(SINGLE_DATE_TIME, singleDateTime)
        outState.putSerializable(START_DATE, startDate)
        outState.putSerializable(DURATION, duration)
    }

    override fun onDateTimePicked(dateTime: ZonedDateTime, duration: Duration) {
        if (datePickMode == DatePickMode.SINGLE) {
            singleDateTime = dateTime
            updateSingleDateTimeText()
        } else {
            startDate = dateTime
            this.duration = duration
            updateDateRangeText()
        }
    }

    private fun updateSingleDateTimeText() {
        val dialogTag = singleModeDialogTag ?: return
        val singleDateTime = singleDateTime ?: return
        if (dialogTag == TAG_DATE_PICKER)
            single_date_text_view.text = DateStringUtils.formatDateWithWeekDay(this, singleDateTime)
        else
            single_date_text_view.text = DateStringUtils.formatFullDateTime(this, singleDateTime)
    }

    private fun updateDateRangeText() {
        val rangeStartDate = startDate ?: return
        date_start_text_view.text = DateStringUtils.formatDateWithWeekDay(this, rangeStartDate)

        val endDate = rangeStartDate.plus(duration)
        date_end_text_view.text = DateStringUtils.formatDateWithWeekDay(this, endDate)
    }

    private fun getDuration(): Duration = if (datePickMode == DatePickMode.SINGLE) Duration.ZERO else duration

    private fun getDateTime(): ZonedDateTime = if (datePickMode == DatePickMode.SINGLE) getSingleDateTime() else getRangeStartDate()

    private fun getSingleDateTime(): ZonedDateTime = singleDateTime ?: ZonedDateTime.now()

    private fun getRangeStartDate(): ZonedDateTime = startDate ?: ZonedDateTime.now()

}
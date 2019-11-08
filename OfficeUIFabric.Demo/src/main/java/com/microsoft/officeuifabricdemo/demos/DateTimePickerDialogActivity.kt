/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.widget.Button
import com.jakewharton.threetenabp.AndroidThreeTen
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog.DateRangeMode
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog.Mode
import com.microsoft.officeuifabric.datetimepicker.DateTimeRangeTab
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabric.util.accessibilityManager
import com.microsoft.officeuifabric.util.isAccessibilityEnabled
import com.microsoft.officeuifabric.util.isVisible
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_date_time_picker_dialog.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class DateTimePickerDialogActivity : DemoActivity(), DateTimePickerDialog.OnDateTimeSelectedListener, DateTimePickerDialog.OnDateTimePickedListener {
    companion object {
        private const val TAG_DATE_PICKER = "datePicker"
        private const val TAG_DATE_TIME_PICKER = "dateTimePicker"
        private const val TAG_TIME_DATE_PICKER = "timeDatePicker"
        private const val TAG_DATE_TIME_RANGE_PICKER = "dateTimeRangePicker"
        private const val TAG_END_DATE_PICKER = "endDatePicker"
        private const val TAG_START_DATE_PICKER = "startDatePicker"

        private const val PICKED_DATE_TIME = "pickedDateTime"
        private const val PICKED_DATE_DURATION = "pickedDateDuration"
        private const val PICKED_DATE_TIME_DURATION = "pickedDateTimeDuration"
        private const val PICKED_START_DATE = "pickedStartDate"
        private const val PICKED_START_DATE_TIME = "pickedStartDateTime"

        private const val SELECTED_DATE_TIME = "selectedDateTime"
        private const val SELECTED_DATE_DURATION = "selectedDateDuration"
        private const val SELECTED_DATE_TIME_DURATION = "selectedDateTimeDuration"
        private const val SELECTED_START_DATE = "selectedStartDate"
        private const val SELECTED_START_DATE_TIME = "selectedStartDateTime"

        private const val SINGLE_MODE_TAG = "singleModeDialogTag"
        private const val DIALOG_TAG = "dialogTag"
        private const val DIALOG_IS_SHOWING = "dialogIsShowing"
        private const val DISPLAY_MODE = "displayMode"
        private const val DATE_RANGE_MODE = "dateRangeMode"
    }

    enum class DatePickerType(val buttonId: Int, val tag: String, val mode: Mode, val dateRangeMode: DateRangeMode) {
        DATE(R.id.date_picker_button, TAG_DATE_PICKER, Mode.DATE, DateRangeMode.NONE),
        DATE_TIME(R.id.date_time_picker_date_selected_button, TAG_DATE_TIME_PICKER, Mode.DATE_TIME, DateRangeMode.NONE),
        TIME_DATE(R.id.date_time_picker_time_selected_button, TAG_TIME_DATE_PICKER, Mode.TIME_DATE, DateRangeMode.NONE),
        START_DATE(R.id.date_range_start_button, TAG_START_DATE_PICKER, Mode.DATE, DateRangeMode.START),
        END_DATE(R.id.date_range_end_button, TAG_END_DATE_PICKER, Mode.DATE, DateRangeMode.END),
        START_DATE_TIME(R.id.date_time_range_start_button, TAG_DATE_TIME_RANGE_PICKER, Mode.DATE_TIME, DateRangeMode.START)
    }

    override val contentLayoutId: Int
        get() = R.layout.activity_date_time_picker_dialog

    private var pickedDateTime: ZonedDateTime? = null
    private var pickedDateDuration: Duration = Duration.ZERO
    private var pickedDateTimeDuration: Duration = Duration.ZERO
    private var pickedStartDate: ZonedDateTime? = null
    private var pickedStartDateTime: ZonedDateTime? = null

    private var selectedDateTime: ZonedDateTime? = null
    private var selectedDateDuration: Duration? = Duration.ZERO
    private var selectedDateTimeDuration: Duration? = Duration.ZERO
    private var selectedStartDate: ZonedDateTime? = null
    private var selectedStartDateTime: ZonedDateTime? = null

    private var currentDialog: DateTimePickerDialog? = null
    private var displayMode: Mode? = null
    private var dateRangeMode: DateRangeMode? = null
    private var singleModeDialogTag: String? = null
    private var dialogTag: String? = null
        set(value) {
            if (dateRangeMode == DateRangeMode.NONE || dateRangeMode == null)
                singleModeDialogTag = value
            field = value
        }

    init {
        AndroidThreeTen.init(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            pickedDateTime = it.getSerializable(PICKED_DATE_TIME) as? ZonedDateTime
            pickedDateDuration = it.getSerializable(PICKED_DATE_DURATION) as Duration
            pickedDateTimeDuration = it.getSerializable(PICKED_DATE_TIME_DURATION) as Duration
            pickedStartDate = it.getSerializable(PICKED_START_DATE) as? ZonedDateTime
            pickedStartDateTime = it.getSerializable(PICKED_START_DATE_TIME) as? ZonedDateTime

            selectedDateTime = it.getSerializable(SELECTED_DATE_TIME) as? ZonedDateTime
            selectedDateDuration = it.getSerializable(SELECTED_DATE_DURATION) as? Duration
            selectedDateTimeDuration = it.getSerializable(SELECTED_DATE_TIME_DURATION) as? Duration
            selectedStartDate = it.getSerializable(SELECTED_START_DATE) as? ZonedDateTime
            selectedStartDateTime = it.getSerializable(SELECTED_START_DATE_TIME) as? ZonedDateTime

            singleModeDialogTag = it.getString(SINGLE_MODE_TAG)
            dialogTag = it.getString(DIALOG_TAG)
            displayMode = it.getSerializable(DISPLAY_MODE) as? Mode
            dateRangeMode = it.getSerializable(DATE_RANGE_MODE) as? DateRangeMode

            updateDateTimeText()
            updateDateRangeText()
            updateDateTimeRangeText()

            if (it.getBoolean(DIALOG_IS_SHOWING))
                openDateTimePickerDialogByTag()
        }

        DatePickerType.values().forEach { picker ->
            findViewById<Button>(picker.buttonId).setOnClickListener {
                showPicker(picker)
            }
        }

        updateButtonsForAccessibility(isAccessibilityEnabled)

        accessibilityManager.addAccessibilityStateChangeListener { enabled ->
            updateButtonsForAccessibility(enabled)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(PICKED_DATE_TIME, pickedDateTime)
        outState.putSerializable(PICKED_DATE_DURATION, pickedDateDuration)
        outState.putSerializable(PICKED_DATE_TIME_DURATION, pickedDateTimeDuration)
        outState.putSerializable(PICKED_START_DATE, pickedStartDate)
        outState.putSerializable(PICKED_START_DATE_TIME, pickedStartDateTime)

        outState.putSerializable(SELECTED_DATE_TIME, selectedDateTime)
        outState.putSerializable(SELECTED_DATE_DURATION, selectedDateDuration)
        outState.putSerializable(SELECTED_DATE_TIME_DURATION, selectedDateTimeDuration)
        outState.putSerializable(SELECTED_START_DATE, selectedStartDate)
        outState.putSerializable(SELECTED_START_DATE_TIME, selectedStartDateTime)

        outState.putString(SINGLE_MODE_TAG, singleModeDialogTag)
        outState.putString(DIALOG_TAG, dialogTag)
        outState.putBoolean(DIALOG_IS_SHOWING, currentDialog != null)

        if (displayMode == Mode.DATE_TIME || displayMode == Mode.TIME_DATE)
            displayMode = if (currentDialog?.pickerTab == DateTimePickerDialog.PickerTab.CALENDAR_VIEW)
                Mode.DATE_TIME
            else
                Mode.TIME_DATE

        outState.putSerializable(DISPLAY_MODE, displayMode)

        if (dateRangeMode == DateRangeMode.START || dateRangeMode == DateRangeMode.END)
            dateRangeMode = if (currentDialog?.dateTimeRangeTab == DateTimeRangeTab.START)
                DateRangeMode.START
            else
                DateRangeMode.END

        outState.putSerializable(DATE_RANGE_MODE, dateRangeMode)
    }

    override fun onDateTimePicked(dateTime: ZonedDateTime, duration: Duration) {
        when (dialogTag) {
            TAG_DATE_PICKER, TAG_DATE_TIME_PICKER, TAG_TIME_DATE_PICKER -> {
                pickedDateTime = dateTime
                updateDateTimeText()
            }
            TAG_START_DATE_PICKER, TAG_END_DATE_PICKER -> {
                pickedStartDate = dateTime
                pickedDateDuration = duration
                updateDateRangeText()
            }
            TAG_DATE_TIME_RANGE_PICKER -> {
                pickedStartDateTime = dateTime
                pickedDateTimeDuration = duration
                updateDateTimeRangeText()
            }
        }

        reset()
    }

    override fun onDateTimeSelected(dateTime: ZonedDateTime, duration: Duration) {
        when (dialogTag) {
            TAG_DATE_PICKER, TAG_DATE_TIME_PICKER, TAG_TIME_DATE_PICKER -> {
                selectedDateTime = dateTime
            }
            TAG_START_DATE_PICKER, TAG_END_DATE_PICKER -> {
                selectedStartDate = dateTime
                selectedDateDuration = duration
            }
            TAG_DATE_TIME_RANGE_PICKER -> {
                selectedStartDateTime = dateTime
                selectedDateTimeDuration = duration
            }
        }
    }

    private fun reset() {
        selectedDateTime = null
        selectedDateDuration = null
        selectedDateTimeDuration = null
        selectedStartDate = null
        selectedStartDateTime  = null

        currentDialog = null
        displayMode = null
        dateRangeMode = null
    }

    private fun openDateTimePickerDialogByTag() {
        DatePickerType.values().forEach { picker ->
            if (dialogTag == picker.tag) {
                showPicker(picker)
                return
            }
        }
    }

    private fun showPicker(picker: DatePickerType) {
        dialogTag = picker.tag
        val dialog = DateTimePickerDialog(
            this,
            displayMode ?: picker.mode,
            dateRangeMode ?: picker.dateRangeMode,
            getDateTime(),
            getDuration()
        )
        dialog.onDateTimeSelectedListener = this
        dialog.onDateTimePickedListener = this
        dateRangeMode = picker.dateRangeMode
        displayMode = picker.mode
        dialog.setOnCancelListener { reset() }
        dialog.show()
        currentDialog = dialog
    }

    private fun updateButtonsForAccessibility(accessibilityEnabled: Boolean) {
        date_time_picker_time_selected_button.isVisible = !accessibilityEnabled

        val dateSelectedButtonText = if (accessibilityEnabled)
            R.string.date_time_picker_dialog_date_time_button
        else
            R.string.date_time_picker_dialog_calendar_date_time_button
        date_time_picker_date_selected_button.setText(dateSelectedButtonText)
    }

    private fun updateDateTimeText() {
        val tag = singleModeDialogTag ?: return
        val dateTime = pickedDateTime ?: return
        if (tag == TAG_DATE_PICKER)
            date_text_view.text = DateStringUtils.formatDateWithWeekDay(this, dateTime)
        else
            date_text_view.text = DateStringUtils.formatFullDateTime(this, dateTime)
    }

    private fun updateDateRangeText() {
        val startDate = pickedStartDate ?: return
        start_date_text_view.text = DateStringUtils.formatDateWithWeekDay(this, startDate)

        val endDate = startDate.plus(pickedDateDuration)
        end_date_text_view.text = DateStringUtils.formatDateWithWeekDay(this, endDate)
    }

    private fun updateDateTimeRangeText() {
        val startDateTime = pickedStartDateTime ?: return
        start_date_time_text_view.text = DateStringUtils.formatFullDateTime(this, startDateTime)

        val endDateTime = startDateTime.plus(pickedDateTimeDuration)
        end_date_time_text_view.text = DateStringUtils.formatFullDateTime(this, endDateTime)
    }

    private fun getDateTime(): ZonedDateTime =
        when (dialogTag) {
            TAG_DATE_PICKER, TAG_DATE_TIME_PICKER, TAG_TIME_DATE_PICKER -> selectedDateTime ?: pickedDateTime ?: ZonedDateTime.now()
            TAG_START_DATE_PICKER, TAG_END_DATE_PICKER -> selectedStartDate ?: pickedStartDate ?: ZonedDateTime.now()
            TAG_DATE_TIME_RANGE_PICKER -> selectedStartDateTime ?: pickedStartDateTime ?: ZonedDateTime.now()
            else -> throw IllegalStateException("dialogTag expected")
        }

    private fun getDuration(): Duration =
        when (dialogTag) {
            TAG_START_DATE_PICKER, TAG_END_DATE_PICKER -> selectedDateDuration ?: pickedDateDuration
            TAG_DATE_TIME_RANGE_PICKER -> selectedDateTimeDuration ?: pickedDateTimeDuration
            else -> Duration.ZERO
        }
}
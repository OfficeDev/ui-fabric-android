/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datepicker

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabric.view.PositionableDialog
import kotlinx.android.synthetic.main.dialog_day_picker.*

import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * [DatePickerDialog] houses a [DatePickerView] instances within a positionable dialog as well as
 * configures aspects of the view
 */
class DatePickerDialog : PositionableDialog(), DateTimePickerListener {
    companion object {
        private const val ELEVATION = 0f

        @JvmStatic
        fun newInstance(
            time: ZonedDateTime,
            duration: Duration,
            mode: PickMode,
            customPosition: IntArray? = null,
            customSize: IntArray? = null,
            showTitlebar: Boolean = false,
            popupMode: Boolean = false,
            showBusyDays: Boolean = false,
            dismissOnDayPicked: Boolean = false
        ): DatePickerDialog {
            val args = Bundle()
            args.putSerializable(DateTimePickerExtras.DATE_TIME, time)
            args.putSerializable(DateTimePickerExtras.DURATION, duration)
            args.putSerializable(DateTimePickerExtras.PICKING_MODE, mode)
            args.putIntArray(PositionableDialog.EXTRA_CUSTOM_SCREEN_POSITION, customPosition)
            args.putIntArray(PositionableDialog.EXTRA_CUSTOM_SCREEN_SIZE, customSize)
            args.putBoolean(DateTimePickerExtras.POPUP_MODE, popupMode)
            args.putBoolean(DateTimePickerExtras.SHOW_BUSY_DAYS, showBusyDays)
            args.putBoolean(DateTimePickerExtras.DISMISS_ON_DAY_PICKED, dismissOnDayPicked)
            args.putBoolean(DateTimePickerExtras.SHOW_TITLEBAR, showTitlebar)

            val dialog = DatePickerDialog()
            dialog.arguments = args
            return dialog
        }
    }

    enum class PickMode {
        SINGLE, RANGE_START, RANGE_END
    }

    private lateinit var displayDate: ZonedDateTime
    private lateinit var duration: Duration
    private lateinit var pickMode: PickMode
    private var accentColor: Int = 0
        get() {
            return context?.let { ContextCompat.getColor(it, R.color.uifabric_primary)} ?: 0
        }

    val fullModeHeight: Int
        get() = date_picker_view.fullModeHeight

    fun setTimeSlot(startTime: ZonedDateTime, duration: Duration) {
        displayDate = startTime
        val endTime = startTime.plus(duration).with(startTime.toLocalTime())
        this.duration = Duration.between(startTime, endTime)
        date_picker_view.setSelectedDateRange(displayDate.toLocalDate(), this.duration, pickMode == PickMode.RANGE_END)
    }

    fun expandCalendarView() {
        date_picker_view.displayMode = DatePickerView.DisplayMode.LENGTHY_MODE
    }

    fun collapseCalendarView() {
        date_picker_view.leaveLengthyMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = savedInstanceState ?: arguments ?: return
        displayDate = bundle.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
        duration = bundle.getSerializable(DateTimePickerExtras.DURATION) as Duration
        accentColor = bundle.getInt(DateTimePickerExtras.ACCENT_COLOR)
        pickMode = bundle.getSerializable(DateTimePickerExtras.PICKING_MODE) as PickMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_day_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        date_picker_view.setSelectedDateRange(displayDate.toLocalDate(), duration, pickMode == PickMode.SINGLE)
        date_picker_view.listener = this

        val arguments = arguments ?: return
        if (arguments.getBoolean(DateTimePickerExtras.SHOW_TITLEBAR)) {
            title.visibility = View.VISIBLE
            title.setTextColor(accentColor)
            context?.let { title.text = DateStringUtils.formatDateAbbrevAll(it, displayDate) }

            if (arguments.getBoolean(DateTimePickerExtras.POPUP_MODE, false))
                view.setBackgroundResource(R.drawable.dialog_background_light_no_insets)
        }
    }

    override fun onCreateDialog(bundle: Bundle?): Dialog {
        val dialog = super.onCreateDialog(bundle)
        val arguments = arguments ?: return dialog
        if (arguments.getBoolean(DateTimePickerExtras.POPUP_MODE, false)) {
            dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.window.setBackgroundDrawable(ContextCompat.getDrawable(dialog.context, android.R.drawable.dialog_holo_light_frame))
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                dialog.window.setElevation(ELEVATION)
        }
        return dialog
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putSerializable(DateTimePickerExtras.DATE_TIME, displayDate)
        bundle.putSerializable(DateTimePickerExtras.DURATION, duration)
        bundle.putInt(DateTimePickerExtras.ACCENT_COLOR, accentColor)
        bundle.putSerializable(DateTimePickerExtras.PICKING_MODE, pickMode)
    }

    override fun onDateSelected(date: ZonedDateTime) {
        when (pickMode) {
            PickMode.SINGLE -> {
                displayDate = date
                duration = Duration.ZERO
            }
            PickMode.RANGE_START -> {
                displayDate = date
            }
            PickMode.RANGE_END -> {
                if (date.isBefore(displayDate)) {
                    displayDate = date.minus(duration)
                } else {
                    duration = Duration.between(displayDate, date)
                }
            }
        }

        (parentFragment as? DateTimePickerListener)?.onDateSelected(date)

        if (title.visibility == View.VISIBLE)
            context?.let { title.text = DateStringUtils.formatDateAbbrevAll(it, displayDate) }
    }

    override fun adjustCustomPosition(customPosition: IntArray) {
        super.adjustCustomPosition(customPosition)

        val arguments = arguments ?: return
        if (arguments.getBoolean(DateTimePickerExtras.POPUP_MODE, false)) {
            var isRTL = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            }

            // Since the shadow is baked into our background asset, we should offset to compensate.
            val offsetX = Math.round(resources.getDimension(R.dimen.uifabric_dialog_background_light_shadow_width))
            val offsetY = Math.round(resources.getDimension(R.dimen.uifabric_dialog_background_light_shadow_height))
            if (isRTL) {
                customPosition[0] += offsetX
            } else {
                customPosition[0] -= offsetX
            }
            customPosition[1] -= offsetY
        }
    }
}

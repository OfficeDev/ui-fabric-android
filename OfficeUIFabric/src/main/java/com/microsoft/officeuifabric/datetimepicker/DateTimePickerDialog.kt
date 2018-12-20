/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datetimepicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.calendar.OnDateSelectedListener
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabric.view.ResizableDialog
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * [DateTimePickerDialog] provides a dialog view housing both a [DatePickerFragment] and [DateTimePickerFragment] in a [WrapContentViewPager]
 * as well as includes toolbar UI and menu buttons to dismiss the dialog and accept a date/ time
 */
class DateTimePickerDialog : ResizableDialog(), Toolbar.OnMenuItemClickListener, OnDateTimeSelectedListener, OnDateSelectedListener {
    companion object {
        @JvmStatic
        fun newInstance(
            dateTime: ZonedDateTime,
            duration: Duration,
            mode: Mode,
            datePickMode: DatePickMode
        ): DateTimePickerDialog {
            val args = Bundle()
            args.putSerializable(DateTimePickerExtras.DATE_TIME, dateTime)
            args.putSerializable(DateTimePickerExtras.DURATION, duration)
            args.putSerializable(DateTimePickerExtras.MODE, mode)
            args.putSerializable(DateTimePickerExtras.DATE_PICK_MODE, datePickMode)

            val dialog = DateTimePickerDialog()
            dialog.arguments = args
            return dialog
        }
    }

    enum class Mode(val defaultMode: DisplayMode, val accessibleMode: DisplayMode) {
        DATE(DisplayMode.DATE, DisplayMode.ACCESSIBLE_DATE),
        DATE_TIME(DisplayMode.DATE_TIME, DisplayMode.ACCESSIBLE_DATE_TIME),
        TIME_DATE(DisplayMode.TIME_DATE, DisplayMode.ACCESSIBLE_DATE_TIME),
        DATE_TIME_RANGE(DisplayMode.DATE_TIME_RANGE, DisplayMode.ACCESSIBLE_DATE_TIME_RANGE)
    }

    private enum class DisplayMode(
        val showDatePicker: Boolean,
        val showDateTimePicker: Boolean,
        val showTime: Boolean,
        val showDateTimeRange: Boolean,
        val dateTabIndex: Int,
        val dateTimeTabIndex: Int,
        val initialIndex: Int
    ) {
        DATE(true, false, false, false, 0, -1, 0),
        DATE_TIME(true, true, true, false, 0, 1, 0),
        TIME_DATE(true, true, true, false, 0, 1, 1),
        DATE_TIME_RANGE(true, true, true, true, 0, 1, 1),
        ACCESSIBLE_DATE(false, true, false, false, -1, 0, 0),
        ACCESSIBLE_DATE_TIME(false, true, true, false, -1, 0, 0),
        ACCESSIBLE_DATE_TIME_RANGE(false, true, true, true, -1, 0, 0),
    }

    var onDateTimePickedListener: OnDateTimePickedListener? = null

    private lateinit var dateTime: ZonedDateTime
    private lateinit var duration: Duration
    private lateinit var mode: Mode
    private lateinit var displayMode: DisplayMode
    private lateinit var datePickMode: DatePickMode
    private lateinit var pagerAdapter: DateTimePagerAdapter

    private val animatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            pagerAdapter.datePicker?.collapseCalendarView()
        }

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            pagerAdapter.datePicker?.collapseCalendarView()
        }
    }

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateTitles()
            val datePicker = pagerAdapter.datePicker
            val dateTimePicker = pagerAdapter.dateTimePicker
            if (position == displayMode.dateTabIndex && datePicker != null) {
                view_pager.currentObject = datePicker

                // We're switching from the tall time picker to the short date picker. Layout transition
                // leaves blank white area below date picker. So manual animation is used here instead to avoid this.
                enableLayoutTransition(false)
                view_pager.smoothlyResize(datePicker.fullModeHeight, animatorListener)
            } else if (position == displayMode.dateTimeTabIndex && dateTimePicker != null) {
                view_pager.currentObject = dateTimePicker
                datePicker?.expandCalendarView()
                enableLayoutTransition(true)
                view_pager.shouldWrapContent = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = savedInstanceState ?: arguments ?: return
        dateTime = bundle.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
        duration = bundle.getSerializable(DateTimePickerExtras.DURATION) as Duration
        mode = bundle.getSerializable(DateTimePickerExtras.MODE) as Mode
        datePickMode = bundle.getSerializable(DateTimePickerExtras.DATE_PICK_MODE) as DatePickMode

        // TODO implement accessible modes
        displayMode = mode.defaultMode
    }

    override fun createContentView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_date_time_picker, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        val accentColor = ContextCompat.getColor(context, R.color.uifabric_primary)
        toolbar.setTitleTextColor(accentColor)
        toolbar.inflateMenu(R.menu.menu_time_picker)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationIcon(R.drawable.ms_ic_close_grey)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.menu.findItem(R.id.action_done).setIcon(R.drawable.ms_ic_done)

        pagerAdapter = DateTimePagerAdapter(childFragmentManager)
        view_pager.adapter = pagerAdapter
        view_pager.addOnPageChangeListener(pageChangeListener)

        if (displayMode == DisplayMode.TIME_DATE)
            view_pager.currentItem = displayMode.dateTimeTabIndex

        if (pagerAdapter.count < 2) {
            tab_container.visibility = View.GONE
        } else {
            tabs.setupWithViewPager(view_pager)
            tabs.setTabTextColors(ContextCompat.getColor(context, R.color.uifabric_gray), accentColor)
            tabs.setSelectedTabIndicatorColor(accentColor)
        }

        updateTitles()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putSerializable(DateTimePickerExtras.DATE_TIME, dateTime)
        bundle.putSerializable(DateTimePickerExtras.DURATION, duration)
        bundle.putSerializable(DateTimePickerExtras.MODE, mode)
        bundle.putSerializable(DateTimePickerExtras.DATE_PICK_MODE, datePickMode)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        callOnDateTimePicked()
        dismiss()
        return false
    }

    override fun onDateSelected(dateTime: ZonedDateTime) {
        if (datePickMode === DatePickMode.RANGE_END)
            if (dateTime.isBefore(this.dateTime))
                this.dateTime = dateTime.minus(duration)
            else
                duration = Duration.between(this.dateTime, dateTime)
        else
            this.dateTime = this.dateTime.with(dateTime.toLocalDate())

        updateTitles()

        pagerAdapter.dateTimePicker?.setDate(this.dateTime)
    }

    override fun onDateTimeSelected(dateTime: ZonedDateTime) {
        this.dateTime = dateTime
        updateTitles()
        pagerAdapter.datePicker?.setTimeSlot(TimeSlot(this.dateTime, duration))
    }

    private fun updateTitles() {
        val context = context ?: return
        when (displayMode) {
            DisplayMode.DATE -> {
                toolbar.title = if (datePickMode == DatePickMode.RANGE_START)
                    DateStringUtils.formatDateAbbrevAll(context, dateTime)
                else
                    DateStringUtils.formatDateAbbrevAll(context, dateTime.plus(duration))
            }
            DisplayMode.ACCESSIBLE_DATE_TIME -> {
                toolbar.title = if (datePickMode == DatePickMode.RANGE_START)
                    DateStringUtils.formatAbbrevDateAtTime(context, dateTime)
                else
                    DateStringUtils.formatAbbrevDateTime(context, dateTime.plus(duration))
            }
            else -> {
                val currentTab = view_pager.currentItem
                toolbar.setTitle(if (currentTab == displayMode.dateTabIndex) R.string.date_time_picker_choose_date else R.string.date_time_picker_choose_time)

                // Set tab titles
                if (displayMode.dateTabIndex != -1)
                    tabs.getTabAt(displayMode.dateTabIndex)?.text = DateStringUtils.formatDateWithWeekDay(context, dateTime)

                if (displayMode.dateTimeTabIndex != -1)
                    tabs.getTabAt(displayMode.dateTimeTabIndex)?.text = DateStringUtils.formatAbbrevTime(context, dateTime)
            }
        }
    }

    private fun callOnDateTimePicked() {
        (activity as? OnDateTimePickedListener)?.onDateTimePicked(dateTime)
        onDateTimePickedListener?.onDateTimePicked(dateTime)
    }

    private inner class DateTimePagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        var datePicker: DatePickerFragment? = null
        var dateTimePicker: DateTimePickerFragment? = null

        override fun getItem(position: Int): Fragment {
            val fragment: Fragment = if (position == displayMode.dateTabIndex) DatePickerFragment() else DateTimePickerFragment()
            fragment.arguments = arguments
            return fragment
        }

        override fun getCount(): Int = if (displayMode == DisplayMode.DATE_TIME || displayMode == DisplayMode.TIME_DATE) 2 else 1

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            val currentItem = view_pager.currentItem

            if (position == displayMode.dateTabIndex) {
                datePicker = fragment as DatePickerFragment
                datePicker?.arguments?.putBoolean(DateTimePickerExtras.EXPAND_ON_START, position != currentItem)
                datePicker?.onDateSelectedListener = this@DateTimePickerDialog
            } else if (position == displayMode.dateTimeTabIndex) {
                dateTimePicker = fragment as DateTimePickerFragment
                dateTimePicker?.arguments?.putSerializable(DateTimePickerExtras.DATE_PICK_MODE, DatePickMode.SINGLE)
                dateTimePicker?.onDateTimeSelectedListener = this@DateTimePickerDialog
            }

            if (position == currentItem)
                view_pager.currentObject = fragment

            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            if (position == displayMode.dateTabIndex) {
                datePicker = null
            } else if (position == displayMode.dateTimeTabIndex) {
                dateTimePicker = null
            }

            super.destroyItem(container, position, `object`)
        }
    }
}

interface OnDateTimePickedListener {
    /**
     * Method called when a user picks a date or date and time.
     * @param [dateTime] the picked date or date and time
     */
    fun onDateTimePicked(dateTime: ZonedDateTime)
}

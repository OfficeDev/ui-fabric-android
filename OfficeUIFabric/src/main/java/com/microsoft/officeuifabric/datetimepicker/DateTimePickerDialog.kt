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
import com.microsoft.officeuifabric.core.DateTimeSelectionListener
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabric.view.ResizableDialog
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

// TODO create TimePicker
/**
 * [DateTimePickerDialog] provides a dialog view housing both a [DatePickerDialog] and Time Picker in a [ViewPager]
 * as well as includes toolbar UI and menu buttons to dismiss the dialog and accept a date/ time
 */
class DateTimePickerDialog : ResizableDialog(), Toolbar.OnMenuItemClickListener, DateTimeSelectionListener {
    companion object {
        @JvmStatic
        fun newInstance(
            time: ZonedDateTime,
            duration: Duration,
            pickMode: DatePickerDialog.PickMode
        ): DateTimePickerDialog {
            val args = Bundle()
            args.putSerializable(DateTimePickerExtras.DATE_TIME, time)
            args.putSerializable(DateTimePickerExtras.DURATION, duration)
            args.putSerializable(DateTimePickerExtras.MODE, Mode.DATE_ONLY)
            args.putSerializable(DateTimePickerExtras.PICKING_MODE, pickMode)

            val dialog = DateTimePickerDialog()
            dialog.arguments = args
            return dialog
        }
    }

    private enum class Mode(val showDatePicker: Boolean, val showSimpleTimePicker: Boolean, val showAltTimePicker: Boolean, val dateTabIndex: Int, val timeTabIndex: Int, val initialIndex: Int) {
        DATE_ONLY(true, false, false, 0, -1, 0), // used for all-day listener
        ALT_TIME_ONLY(false, false, true, -1, 0, 0), // used for cross-day listener
        NORMAL_DATE(true, true, true, 0, 1, 0), // used for same-day event, open in date tab
        NORMAL_TIME(true, true, true, 0, 1, 1)
    }

    var listener: DateTimeSelectionListener? = null

    private lateinit var displayDate: ZonedDateTime
    private lateinit var duration: Duration
    private lateinit var mode: Mode
    private lateinit var pickMode: DatePickerDialog.PickMode
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
            val datePicker = pagerAdapter.datePicker ?: return
            if (position == mode.dateTabIndex) {
                datePicker.setTimeSlot(displayDate, duration)
                viewPager.currentObject = datePicker

                // We're switching from the tall time picker to the short date picker. Layout transition
                // leaves blank white area below date picker. So manual animation is used here instead to avoid this.
                enableLayoutTransition(false)
                viewPager.smoothlyResize(datePicker.fullModeHeight, animatorListener)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            displayDate = arguments?.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
            duration = arguments?.getSerializable(DateTimePickerExtras.DURATION) as Duration
        } else {
            displayDate = savedInstanceState.getSerializable(DateTimePickerExtras.DATE_TIME) as ZonedDateTime
            duration = savedInstanceState.getSerializable(DateTimePickerExtras.DURATION) as Duration
        }

        val args = arguments ?: return
        mode = args.getSerializable(DateTimePickerExtras.MODE) as Mode
        pickMode = args.getSerializable(DateTimePickerExtras.PICKING_MODE) as DatePickerDialog.PickMode
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
        toolbar.setNavigationIcon(R.drawable.ic_close_grey)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.menu.findItem(R.id.action_done).setIcon(R.drawable.ic_done)

        pagerAdapter = DateTimePagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter
        viewPager.addOnPageChangeListener(pageChangeListener)

        if (mode == Mode.NORMAL_TIME)
            viewPager.currentItem = mode.timeTabIndex

        if (pagerAdapter.count == 1) {
            tab_row.visibility = View.GONE
        } else {
            tabs.setupWithViewPager(viewPager)
            tabs.setTabTextColors(ContextCompat.getColor(context, R.color.uifabric_gray), accentColor)
            tabs.setSelectedTabIndicatorColor(accentColor)
        }

        updateTitles()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putSerializable(DateTimePickerExtras.DATE_TIME, displayDate)
        bundle.putSerializable(DateTimePickerExtras.DURATION, duration)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (viewPager.currentItem == mode.dateTabIndex) {
            callTimeListener()
            callDateListener()
        } else {
            callDateListener()
            callTimeListener()
        }

        dismiss()
        return false
    }

    override fun onDateSelected(date: ZonedDateTime) {
        if (pickMode === DatePickerDialog.PickMode.RANGE_END) {
            if (date.isBefore(displayDate)) {
                displayDate = date.minus(duration)
            } else {
                duration = Duration.between(displayDate, date)
            }
        } else {
            displayDate = displayDate.with(date.toLocalDate())
        }

        updateTitles()
    }

    private fun updateTitles() {
        val context = context ?: return
        toolbar.title = if (pickMode == DatePickerDialog.PickMode.RANGE_START) {
            DateStringUtils.formatDateAbbrevAll(context, displayDate)
        } else {
            DateStringUtils.formatDateAbbrevAll(context, displayDate.plus(duration))
        }
    }

    private fun callDateListener() {
        // Routing communication through Activities rather than Fragment to Fragment is preferred in Android
        (activity as? DateTimeSelectionListener)?.onDatePicked(displayDate)
        listener?.onDatePicked(displayDate)
    }

    private fun callTimeListener() {
        // TODO implement for time picking
    }

    private inner class DateTimePagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        var datePicker: DatePickerDialog? = null

        override fun getItem(position: Int): Fragment {
            val dialog = DatePickerDialog()
            dialog.arguments = arguments
            return dialog
        }

        override fun getCount(): Int = if (mode == Mode.NORMAL_DATE || mode == Mode.NORMAL_TIME) 2 else 1

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            val currentItem = viewPager.currentItem

            if (position == mode.dateTabIndex) {
                val datePicker = fragment as DatePickerDialog
                datePicker.arguments?.putBoolean(DateTimePickerExtras.EXPAND_ON_START, position != currentItem)
                this.datePicker = datePicker
            }

            if (position == currentItem)
                viewPager.currentObject = fragment

            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            if (position == mode.dateTabIndex)
                datePicker = null

            super.destroyItem(container, position, `object`)
        }
    }
}

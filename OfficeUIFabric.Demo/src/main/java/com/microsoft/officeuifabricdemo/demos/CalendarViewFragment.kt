/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.core.DateTimeSelectionListener
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import org.threeten.bp.ZonedDateTime

class CalendarViewFragment : DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        val context = context ?: return view
        view.calendar_view.listener = object : DateTimeSelectionListener {
            override fun onDateSelected(date: ZonedDateTime) {
                view.example_date_title.text = DateStringUtils.formatDateWithWeekDay(context, date)
            }
        }
        return view
    }
}
/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import com.microsoft.officeuifabric.calendar.OnDateSelectedListener
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_calendar_view.*
import org.threeten.bp.ZonedDateTime

class CalendarViewActivity : DemoActivity() {
    override val contentLayoutId: Int
        get() = R.layout.activity_calendar_view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calendar_view.onDateSelectedListener = object : OnDateSelectedListener {
            override fun onDateSelected(date: ZonedDateTime) {
                example_date_title.text = DateStringUtils.formatDateWithWeekDay(this@CalendarViewActivity, date)
            }
        }
    }
}
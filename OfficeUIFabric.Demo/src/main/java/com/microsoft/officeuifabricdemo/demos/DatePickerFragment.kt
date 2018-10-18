/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.datepicker.IDatePickerEvents
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_date_picker.view.*
import org.threeten.bp.LocalDate

class DatePickerFragment : DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_date_picker, container, false)
        val context = context ?: return view
        view.date_picker_view.events = object : IDatePickerEvents {
            override fun onChange(date: LocalDate) {
                view.example_date_title.text = DateStringUtils.formatDateWithWeekDay(context, date)
            }
        }
        return view
    }
}
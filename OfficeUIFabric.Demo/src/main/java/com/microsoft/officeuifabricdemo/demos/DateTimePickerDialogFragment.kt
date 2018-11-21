/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.datetimepicker.DatePickerDialog
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_date_time_picker_dialog.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class DateTimePickerDialogFragment : DemoFragment() {
    companion object {
        const val TAG_DATETIME_PICKER = "date_time_picker"
        private const val SAVE_DATE = "saveDate"
    }

    var date: ZonedDateTime = ZonedDateTime.now()
        set(value) {
            field = value
            context?.let { date_text_vew.text = DateStringUtils.formatDateWithWeekDay(it, value) }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_time_picker_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null)
            date = savedInstanceState.getSerializable(SAVE_DATE) as ZonedDateTime

        date_time_picker_button.setOnClickListener {
            val dialog = DateTimePickerDialog.newInstance(
                date,
                Duration.ZERO,
                DatePickerDialog.PickMode.SINGLE
            )
            dialog.show(fragmentManager, TAG_DATETIME_PICKER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SAVE_DATE, date)
    }
}
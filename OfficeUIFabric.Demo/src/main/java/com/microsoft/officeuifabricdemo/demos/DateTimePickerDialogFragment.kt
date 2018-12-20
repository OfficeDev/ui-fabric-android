/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.datetimepicker.DateTimePickerDialog
import com.microsoft.officeuifabric.datetimepicker.DatePickMode
import com.microsoft.officeuifabric.util.DateStringUtils
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_date_time_picker_dialog.*
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

class DateTimePickerDialogFragment : DemoFragment() {
    companion object {
        private const val TAG_DATE_TIME_PICKER = "dateTimePicker"
        private const val TAG_DATE_PICKER = "datePicker"
        private const val SELECTED_DATE_TIME = "saveDateTime"
        private const val DIALOG_TAG = "dialogTag"
    }

    private var selectedDateTime: ZonedDateTime? = null
    private var dialogTag: String? = null

    fun setDateTime(dateTime: ZonedDateTime) {
        selectedDateTime = dateTime
        updateText()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_time_picker_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            selectedDateTime = it.getSerializable(SELECTED_DATE_TIME) as? ZonedDateTime
            dialogTag = it.getString(DIALOG_TAG)
        }

        date_picker_button.setOnClickListener {
            dialogTag = TAG_DATE_PICKER
            val dialog = DateTimePickerDialog.newInstance(
                getLastDateTime(),
                Duration.ZERO,
                DateTimePickerDialog.Mode.DATE,
                DatePickMode.SINGLE
            )
            dialog.show(fragmentManager, dialogTag)
        }

        date_time_picker_date_selected_button.setOnClickListener {
            dialogTag = TAG_DATE_TIME_PICKER
            val dialog = DateTimePickerDialog.newInstance(
                getLastDateTime(),
                Duration.ZERO,
                DateTimePickerDialog.Mode.DATE_TIME,
                DatePickMode.SINGLE
            )
            dialog.show(fragmentManager, dialogTag)
        }

        date_time_picker_time_selected_button.setOnClickListener {
            dialogTag = TAG_DATE_TIME_PICKER
            val dialog = DateTimePickerDialog.newInstance(
                getLastDateTime(),
                Duration.ZERO,
                DateTimePickerDialog.Mode.TIME_DATE,
                DatePickMode.SINGLE
            )
            dialog.show(fragmentManager, dialogTag)
        }

        updateText()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SELECTED_DATE_TIME, selectedDateTime)
        outState.putString(DIALOG_TAG, dialogTag)
    }

    private fun getLastDateTime() = selectedDateTime ?: ZonedDateTime.now()

    private fun updateText() {
        val context = context ?: return
        val selectDateTime = selectedDateTime ?: return
        val dialogTag = dialogTag ?: return
        if (dialogTag == TAG_DATE_PICKER)
            date_text_view.text = DateStringUtils.formatDateWithWeekDay(context, selectDateTime)
        else
            date_text_view.text = DateStringUtils.formatFullDateTime(context, selectDateTime)
    }
}
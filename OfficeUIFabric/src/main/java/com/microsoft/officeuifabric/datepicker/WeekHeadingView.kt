/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datepicker

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.managers.PreferencesManager
import org.threeten.bp.DayOfWeek

/**
 * [WeekHeadingView] is a LinearLayout holding the DatePicker header with views for
 * the week day letters, S, M, T, W, T, F, S
 */
class WeekHeadingView : LinearLayout {
    private lateinit var config: DatePickerView.Config

    constructor(context: Context, datePickerConfig: DatePickerView.Config) : super(context) {
        config = datePickerConfig
        setBackgroundColor(config.weekHeadingBackgroundColor)

        var dayOfWeek = PreferencesManager.getWeekStart(context)

        val headingTextAppearance = R.style.UIFabric_SubHeading2

        val weekDayHeadingColor = config.weekdayHeadingTextColor
        val weekendHeadingColor = config.weekendHeadingTextColor

        val strDayOfWeek = resources.getStringArray(R.array.weekday_initial)
        for (i in 1..DatePickerView.DAYS_IN_WEEK) {
            val textView = TextView(context)
            TextViewCompat.setTextAppearance(textView, headingTextAppearance)
            textView.text = strDayOfWeek[dayOfWeek.value - 1]

            if (DayOfWeek.SATURDAY == dayOfWeek || DayOfWeek.SUNDAY == dayOfWeek) {
                textView.setTextColor(weekendHeadingColor)
            } else {
                textView.setTextColor(weekDayHeadingColor)
            }

            textView.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, config.weekHeadingTextSize.toFloat())
            addView(textView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f))

            dayOfWeek = dayOfWeek.plus(1)
        }

        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            View.MeasureSpec.makeMeasureSpec(config.weekHeadingHeight, View.MeasureSpec.EXACTLY)
        )
    }
}

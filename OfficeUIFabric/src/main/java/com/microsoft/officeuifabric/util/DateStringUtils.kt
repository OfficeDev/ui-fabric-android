/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.util

import android.content.Context
import android.text.format.DateUtils

import com.microsoft.officeuifabric.R

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAccessor

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

import android.text.format.DateUtils.FORMAT_ABBREV_ALL
import android.text.format.DateUtils.FORMAT_ABBREV_MONTH
import android.text.format.DateUtils.FORMAT_ABBREV_TIME
import android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY
import android.text.format.DateUtils.FORMAT_NO_MONTH_DAY
import android.text.format.DateUtils.FORMAT_NO_YEAR
import android.text.format.DateUtils.FORMAT_NUMERIC_DATE
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.text.format.DateUtils.FORMAT_SHOW_YEAR

/**
 * [DateStringUtils] is a helper object for formatting and dealing with time Strings
 */
object DateStringUtils {
    /**
     * @return an array of week days. Monday being at index 0, Tuesday being at index 1 and so on.
     */
    @JvmStatic
    val weekDayStrings: Array<String> =
        DayOfWeek.values().map { it.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) }.toTypedArray()

    /**
     * @return an array of strings depending on 12 hour period
     */
    @JvmStatic
    val amPmStrings: Array<String>
        get() {
            val format = SimpleDateFormat("a")
            val calendar = GregorianCalendar.getInstance()

            calendar.set(Calendar.AM_PM, Calendar.AM)
            val am = format.format(calendar.time)
            calendar.set(Calendar.AM_PM, Calendar.PM)
            val pm = format.format(calendar.time)

            return arrayOf(am, pm)
        }

    /**
     * Formats a time in a 12 or 24 hours format.
     */
    @JvmStatic
    fun formatTime(context: Context, dateTime: TemporalAccessor): String = formatTime(context, dateTime.epochMillis)

    /**
     * @see .formatTime
     */
    @JvmStatic
    fun formatTime(context: Context, time: Long): String = DateUtils.formatDateTime(context, time, FORMAT_SHOW_TIME)

    /**
     * Formats a date with the weekday. It will auto-append the year if the date is from a different
     * year than now.
     *
     * Example:
     * - Sunday, January 3
     * - Sunday, January 3, 1982
     */
    @JvmStatic
    fun formatDateWithWeekDay(context: Context, date: TemporalAccessor): String = formatDateWithWeekDay(context, date.epochMillis)

    /**
     * @see .formatDateWithWeekDay
     */
    @JvmStatic
    fun formatDateWithWeekDay(context: Context, date: Long): String =
        DateUtils.formatDateTime(context, date,FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)

    /**
     * Formats a date to numeric format with the abbreviated weekday. It will auto-append the year
     * if the date is from a different year than now.
     *
     * Example:
     * - Sun, 1/3
     * - Sun, 1/3/1982
     */
    @JvmStatic
    fun formatDateWithWeekDayAbbrev(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(
            context,
            date.epochMillis,
            FORMAT_ABBREV_WEEKDAY or FORMAT_NUMERIC_DATE or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY
        )

    /**
     * Formats a date with the abbreviated weekday + month + day
     *
     * Example:
     * - Mon, Mar 9
     *
     * @param date Time to format
     */
    @JvmStatic
    fun formatDateAbbrevAll(context: Context, date: TemporalAccessor): String =
        formatDateAbbrevAll(context, date.epochMillis)

    /**
     * @see .formatDateAbbrevAll
     */
    @JvmStatic
    fun formatDateAbbrevAll(context: Context, time: Long): String =
        DateUtils.formatDateTime(context, time, FORMAT_ABBREV_ALL or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)

    /**
     * Formats the month from a date. If the date is not in the same year as today then the current
     * year is also appended to it.
     */
    @JvmStatic
    fun formatMonth(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(context, date.epochMillis, FORMAT_SHOW_DATE or FORMAT_NO_MONTH_DAY)

    /**
     * Formats the month in abbreviated format from a date.
     * Example:
     * input => date - 17th July 2018
     * output => Jul
     */
    @JvmStatic
    fun formatAbbrevMonth(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(context, date.epochMillis, FORMAT_ABBREV_ALL or FORMAT_SHOW_DATE or FORMAT_NO_MONTH_DAY or FORMAT_NO_YEAR)

    /**
     * Formats the month + year from a date (even if the month is in the current year)
     */
    @JvmStatic
    fun formatMonthWithYear(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(context, date.epochMillis, FORMAT_SHOW_DATE or FORMAT_NO_MONTH_DAY or FORMAT_SHOW_YEAR)

    /**
     * @return a formatted week day + time like: Tue 8:00 AM
     */
    @JvmStatic
    fun formatWeekDayWithTime(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(context, date.epochMillis, FORMAT_ABBREV_ALL or FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_TIME)

    /**
     * Formats a date with the weekday + month + day + Time.  The year is optionally formatted if it
     * is not the current year.
     *
     * Example:
     * - Tuesday, November 25, 2014, 3:55AM
     * - Tuesday, March 3, 16:22
     *
     * @param time   Time to format (in millis since the epoch in UTC)
     */
    @JvmStatic
    fun formatFullDateTime(context: Context, time: Long): String =
        DateUtils.formatDateTime(context, time, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_TIME)

    /**
     * @see .formatFullDateTime
     */
    @JvmStatic
    fun formatFullDateTime(context: Context, date: TemporalAccessor?): String =
        if (date == null) "" else formatFullDateTime(context, date.epochMillis)

    /**
     * Formats a date with the abbreviated weekday + month + day + year + 'at' + time.
     *
     * Example:
     * - Tue, Nov 25, 2014 at 3:55AM
     * - Tue, Mar 3, 2016 at 16:22
     *
     * @param date Time to format
     */
    @JvmStatic
    fun formatAbbrevDateAtTime(context: Context, date: TemporalAccessor?): String =
        if (date == null) "" else formatAbbrevDateTime(context, date.epochMillis, R.string.date_at_time)

    /**
     * Formats a date with the abbreviated weekday + month + day + year + ',' + time.
     *
     * Example:
     * - Tue, Nov 25, 2014, 3:55AM
     * - Tue, Mar 3, 2016, 16:22
     *
     * @param date Time to format
     */
    @JvmStatic
    fun formatAbbrevDateTime(context: Context, date: TemporalAccessor?): String =
        if (date == null) "" else formatAbbrevDateTime(context, date.epochMillis, R.string.date_time)

    /**
     * Formats a time.
     *
     * Example:
     * - 3:55AM
     * - 3PM
     * - 16:22
     *
     * @param dateTime Time to format
     */
    @JvmStatic
    fun formatAbbrevTime(context: Context, dateTime: TemporalAccessor): String =
        DateUtils.formatDateTime(context, dateTime.epochMillis, FORMAT_SHOW_TIME or FORMAT_ABBREV_TIME)

    /**
     * Formats a date with Weekday + Date + Year
     *
     * Example:
     * - Friday, March 20, 2015
     *
     * @param  time Time to format (in millis since the epoch in UTC)
     */
    @JvmStatic
    fun formatWeekdayDateYear(context: Context, time: Long): String =
        DateUtils.formatDateTime(context, time, FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR)

    /**
     * Formats a date with abbreviated Weekday + Date + Year
     *
     * Example:
     * - Fri, Mar 20, 2015
     *
     * @param  date Time to format
     */
    @JvmStatic
    fun formatWeekdayDateYearAbbrev(context: Context, date: TemporalAccessor): String =
        DateUtils.formatDateTime(
            context,
            date.epochMillis,
            FORMAT_ABBREV_WEEKDAY or FORMAT_ABBREV_MONTH or FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR
        )

    private fun formatAbbrevDateTime(context: Context, timestamp: Long, stringResource: Int): String {
        var flags = FORMAT_ABBREV_MONTH or FORMAT_ABBREV_WEEKDAY or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY

        // Only show Year when it's not current year
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = timestamp
        if (calendar.get(Calendar.YEAR) != currentYear)
            flags = flags or FORMAT_SHOW_YEAR

        val date = DateUtils.formatDateTime(context, timestamp, flags)
        val time = DateUtils.formatDateTime(context, timestamp, FORMAT_SHOW_TIME)

        return context.getString(stringResource, date, time)
    }

    /**
     * Converts date to the number of milliseconds from the epoch of 1970-01-01T00:00:00Z.
     */
    private val TemporalAccessor.epochMillis: Long
        get() = when (this) {
            is ZonedDateTime -> this.toInstant().toEpochMilli()
            is LocalDate -> this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            is LocalDateTime -> this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            else -> {
                throw Exception("Invalid date")
            }
        }
}

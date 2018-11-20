/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datepicker

import org.threeten.bp.ZonedDateTime

/**
 * [DateTimePickerListener] is an interface for Date and Time picker callbacks
 */
interface DateTimePickerListener {
    /**
     * Method called when a user selects a date
     * @param [date] the selected date
     */
    fun onDateSelected(date: ZonedDateTime) {
        // no op default implementation
    }

    /**
     * Method called when a user picks a date. This would be used in a scenario where
     * date picking is completed and the DatePicker is dismissed
     * @param [date] the picked date
     */
    fun onDatePicked(date: ZonedDateTime) {
        // no op default implementation
    }
}

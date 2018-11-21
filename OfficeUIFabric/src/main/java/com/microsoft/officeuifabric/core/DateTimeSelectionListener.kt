/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.core

import org.threeten.bp.ZonedDateTime

/**
 * [DateTimeSelectionListener] is an interface for Date and Time picker callbacks
 */
interface DateTimeSelectionListener {
    /**
     * Method called when a user selects a date
     * @param [date] the selected date
     */
    fun onDateSelected(date: ZonedDateTime) {
        // no op default implementation
    }

    /**
     * Method called when a user picks a date. This would be used in a scenario where
     * date picking is completed and the DatePickerDialog is dismissed
     * @param [date] the picked date
     */
    fun onDatePicked(date: ZonedDateTime) {
        // no op default implementation
    }
}

/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.datepicker

import org.threeten.bp.LocalDate

/**
 * [IDatePickerEvents] is an interface for DatePicker callbacks
 */
interface IDatePickerEvents {
    /**
     * Method called when a user selects a date
     * @param [date] LocalDate, the selected date
     */
    fun onChange(date: LocalDate)
}

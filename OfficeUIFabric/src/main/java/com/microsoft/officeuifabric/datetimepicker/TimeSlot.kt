/**
 * Copyright (c) 2018 Microsoft Inc. All rights reserved.
 */

package com.microsoft.officeuifabric.datetimepicker

import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import java.io.Serializable

// TODO PBI #668220 investigate whether it's feasible to replace dateTime + duration with this data class
data class TimeSlot(val start: ZonedDateTime, val duration: Duration) : Serializable

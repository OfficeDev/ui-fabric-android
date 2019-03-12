/**
 * Copyright © 2019 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.util

import android.content.Context
import android.support.annotation.StyleRes

fun Context.getTextSize(@StyleRes textAppearanceResourceId: Int): Float {
    val textAttributes = obtainStyledAttributes(textAppearanceResourceId, intArrayOf(android.R.attr.textSize))
    val textSize = textAttributes.getDimension(0, -1f)
    textAttributes.recycle()
    return textSize
}
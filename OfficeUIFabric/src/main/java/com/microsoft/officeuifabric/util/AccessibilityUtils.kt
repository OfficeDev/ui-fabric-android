/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.util

import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * [AccessibilityUtils] utilities for accessibility
 */
object AccessibilityUtils {
    /**
     * Gets whether accessibility is enabled
     * @return Boolean
     */
    @JvmStatic
    fun isAccessibilityEnabled(context: Context): Boolean {
        return getAccessibilityManager(context).isTouchExplorationEnabled
    }

    @JvmStatic
    private fun getAccessibilityManager(context: Context): AccessibilityManager {
        return context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }
}

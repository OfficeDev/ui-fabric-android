/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.util

import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Utilities for accessibility
 */
val Context.isAccessibilityEnabled: Boolean
    get() = accessibilityManager.isTouchExplorationEnabled

private val Context.accessibilityManager: AccessibilityManager
    get() = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

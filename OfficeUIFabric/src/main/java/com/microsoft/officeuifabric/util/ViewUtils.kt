/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.util

import android.view.View
import android.view.ViewGroup

/**
 * Adds a given [view] to a [ViewGroup]. Especially useful when you need a custom view in a control.
 * Use [updateLayout] to update any part of the view's layout before adding it to the [ViewGroup].
 */
fun ViewGroup.setContentAndUpdateVisibility(view: View?, updateLayout: (() -> Unit)? = null) {
    // We need to remove the view each time so that RecyclerViews can properly recycle the view.
    removeAllViews()

    if (view == null) {
        visibility = View.GONE
        return
    }

    // Make sure the custom view isn't already in a ViewGroup.
    // With RecyclerView reusing ViewHolders, it could have a different parent than the current container.
    (view.parent as? ViewGroup)?.removeView(view)

    updateLayout?.invoke()
    addView(view)
    visibility = View.VISIBLE
}
/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.appbarlayout

import android.animation.AnimatorInflater
import android.content.Context
import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.design.widget.AppBarLayout.LayoutParams.*
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.appbarlayout.AppBarLayout.ScrollBehavior
import com.microsoft.officeuifabric.search.Searchbar
import com.microsoft.officeuifabric.theming.UIFabricContextThemeWrapper
import com.microsoft.officeuifabric.toolbar.Toolbar
import com.microsoft.officeuifabric.util.activity
import kotlin.math.abs

/**
 * [AppBarLayout] comes with a [Toolbar] and an optional [accessoryView] that appears below the [Toolbar].
 * [ScrollBehavior] provides control over [Toolbar] and [accessoryView] motion during scroll
 * changes and focus changes when using [Searchbar].
 *
 * TODO
 * - Use Fluent PopupMenu
 * - Add xml attributes
 */
class AppBarLayout : AppBarLayout {
    enum class ScrollBehavior {
        NONE, COLLAPSE_TOOLBAR
    }

    /**
     * This [toolbar] is used as the support action bar.
     */
    lateinit var toolbar: Toolbar
        private set
    /**
     * This view appears below the [toolbar].
     */
    var accessoryView: View? = null
        set(value) {
            if (field == value)
                return

            if (accessoryView != null)
                removeView(accessoryView)

            field = value

            if (field != null)
                addView(field)

            updateAnimationState()
        }
    /**
     * Defines the [ScrollBehavior] applied to the [toolbar] and [accessoryView] on scroll and focus changes.
     */
    var scrollBehavior: ScrollBehavior = ScrollBehavior.COLLAPSE_TOOLBAR
        set(value) {
            if (field == value)
                return
            field = value
            updateAnimationState()
        }

    private val offsetChangedListener = OnOffsetChangedListener { appBarLayout, verticalOffset ->
        toolbar.alpha = 1f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() / 3))

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            stateListAnimator = if (verticalOffset == 0)
                AnimatorInflater.loadStateListAnimator(context, R.animator.app_bar_layout_elevation)
            else
                AnimatorInflater.loadStateListAnimator(context, R.animator.app_bar_layout_elevation_scroll)
        else
            elevation = resources.getDimension(R.dimen.uifabric_app_bar_layout_elevation)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(UIFabricContextThemeWrapper(context), attrs) {
        setupToolbar(context)
        updateAnimationState()
    }

    internal fun updateExpanded(expanded: Boolean) {
        if (scrollBehavior == ScrollBehavior.COLLAPSE_TOOLBAR)
            setExpanded(expanded, true)
    }

    private fun setupToolbar(context: Context) {
        // This theme ensures the proper overflow icon color.
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.ThemeOverlay_AppCompat_Dark_ActionBar)
        toolbar = Toolbar(contextThemeWrapper)
        addView(toolbar)
        context.activity?.setSupportActionBar(toolbar)
    }

    private fun updateAnimationState() {
        val toolbarLayoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        when (scrollBehavior) {
            ScrollBehavior.NONE -> {
                toolbarLayoutParams.scrollFlags = 0
                removeOnOffsetChangedListener(offsetChangedListener)
                toolbar.alpha = 1.0f
            }
            ScrollBehavior.COLLAPSE_TOOLBAR -> {
                toolbarLayoutParams.scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_SNAP or SCROLL_FLAG_ENTER_ALWAYS

                val accessoryViewLayoutParams = accessoryView?.layoutParams as? LayoutParams
                accessoryViewLayoutParams?.scrollFlags = 0
                accessoryView?.layoutParams = accessoryViewLayoutParams

                addOnOffsetChangedListener(offsetChangedListener)
            }
        }

        toolbar.layoutParams = toolbarLayoutParams
    }
}
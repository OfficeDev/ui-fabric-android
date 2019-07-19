/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.drawer

import android.content.Context
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.R
import kotlinx.android.synthetic.main.dialog_drawer.*
import kotlinx.android.synthetic.main.dialog_drawer.view.*

/**
 * [DrawerDialog] is used for displaying a modal dialog in the form of an expanding and collapsing bottom sheet
 * to which content is added.
 */
open class DrawerDialog(context: Context) : AppCompatDialog(context, R.style.Drawer_UIFabric) {
    companion object {
        private const val DISMISS_THRESHOLD = 0.005f
    }

    var onDrawerContentCreatedListener: OnDrawerContentCreatedListener? = null

    private val bottomSheetBehavior: BottomSheetBehavior<View>
        get() = BottomSheetBehavior.from(drawer as View)

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // No op
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (isExpanded && slideOffset < DISMISS_THRESHOLD) {
                isExpanded = false
                dismiss()
            }
        }
    }

    private var isExpanded: Boolean = false
    private val container: View = createContainer()

    override fun setContentView(layoutResID: Int) {
        val content = layoutInflater.inflate(layoutResID, container.drawer_content, false)
        setContentView(content)
        onDrawerContentCreatedListener?.onDrawerContentCreated(content)
    }

    override fun setContentView(view: View) {
        container.drawer_content.addView(view)
        super.setContentView(container)
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun show() {
        super.show()
        val expandDelayMilliseconds = context.resources.getInteger(R.integer.uifabric_drawer_fade_in_milliseconds).toLong()
        Handler().postDelayed(::expand, expandDelayMilliseconds)
    }

    override fun onBackPressed() {
        collapse()
    }

    private fun expand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        isExpanded = true
    }

    protected fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun createContainer(): View {
        val container = layoutInflater.inflate(R.layout.dialog_drawer, null)
        container.setOnClickListener {
            collapse()
        }

        return container
    }
}

interface OnDrawerContentCreatedListener {
    fun onDrawerContentCreated(drawerContents: View)
}

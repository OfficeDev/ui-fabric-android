/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.drawer

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.R
import kotlinx.android.synthetic.main.fragment_drawer.*

// TODO investigate why over scroll "bow" animation is not showing and fix

/**
 * [Drawer] is used for displaying a modal dialog in the form of an expanding and collapsing bottom sheet
 * to which contents are added.
 */
open class Drawer : AppCompatDialogFragment() {
    companion object {
        private const val CONTENT_LAYOUT_ID = "contentLayoutId"
        private const val BOTTOM_SHEET_STATE = "bottomSheetState"

        /**
         * @param contentLayoutId the layout id of the drawer contents.
         */
        fun newInstance(@LayoutRes contentLayoutId: Int): Drawer {
            val bundle = Bundle()
            bundle.putInt(CONTENT_LAYOUT_ID, contentLayoutId)

            val drawer = Drawer()
            drawer.arguments = bundle
            return drawer
        }
    }

    private var contentLayoutId: Int = -1
    private var bottomSheetState: Int = BottomSheetBehavior.STATE_HIDDEN

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contentLayoutId = savedInstanceState?.getInt(CONTENT_LAYOUT_ID) ?: arguments?.getInt(CONTENT_LAYOUT_ID) ?: contentLayoutId
        bottomSheetState = savedInstanceState?.getInt(BOTTOM_SHEET_STATE) ?: arguments?.getInt(BOTTOM_SHEET_STATE) ?: bottomSheetState

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Drawer_UIFabric)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(drawer)
        bottomSheetBehavior.state = bottomSheetState

        drawer_container.setOnClickListener {
            collapse()
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // No op
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset == 0f)
                    dismiss()
            }
        })

        if (contentLayoutId > -1) {
            val drawerContents = layoutInflater.inflate(contentLayoutId, drawer, true)
            (parentFragment as? OnDrawerContentCreatedListener)?.onDrawerContentCreated(drawerContents)
            (activity as? OnDrawerContentCreatedListener)?.onDrawerContentCreated(drawerContents)
        }

        val expandDelayMilliseconds = resources.getInteger(R.integer.uifabric_drawer_fade_in_milliseconds).toLong()
        Handler().postDelayed(::expand, expandDelayMilliseconds)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(context!!, theme) {
            override fun onBackPressed() {
                collapse()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CONTENT_LAYOUT_ID, contentLayoutId)
        outState.putInt(BOTTOM_SHEET_STATE, bottomSheetBehavior.state)
    }

    private fun expand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}

interface OnDrawerContentCreatedListener {
    fun onDrawerContentCreated(drawerContents: View)
}
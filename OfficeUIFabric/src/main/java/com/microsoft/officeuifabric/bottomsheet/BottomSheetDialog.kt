/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.bottomsheet

import android.content.Context
import android.support.annotation.StyleRes
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.bottomsheet.BottomSheetItem.OnClickListener
import com.microsoft.officeuifabric.drawer.DrawerDialog

/**
 * [BottomSheetDialog] is used to display a list of menu items in a modal dialog.
 */
class BottomSheetDialog : DrawerDialog, OnClickListener {
    var onItemClickListener: OnClickListener? = null

    private var clickedItem: BottomSheetItem? = null

    @JvmOverloads
    constructor(context: Context, items: ArrayList<BottomSheetItem>, @StyleRes theme: Int = 0) : super(context, theme) {
        supportRequestWindowFeature(DialogFragment.STYLE_NO_TITLE)
        val adapter = BottomSheetAdapter(context, items, theme)
        adapter.onBottomSheetItemClickListener = this

        val recyclerView = createRecyclerView(context)
        recyclerView.adapter = adapter
        setContentView(recyclerView)
    }

    private fun createRecyclerView(context: Context): RecyclerView {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val bottomPadding = context.resources.getDimension(R.dimen.uifabric_bottom_sheet_bottom_padding).toInt()
        recyclerView.setPadding(0, 0, 0, bottomPadding)

        return recyclerView
    }

    override fun onBottomSheetItemClick(item: BottomSheetItem) {
        clickedItem = item
        collapse()
    }

    override fun dismiss() {
        clickedItem?.let {
            onItemClickListener?.onBottomSheetItemClick(it)
            clickedItem = null
        }

        super.dismiss()
    }
}
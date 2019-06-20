/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.bottomsheet

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.drawer.Drawer

/**
 * [BottomSheet] is used to display a list of menu items in a [Drawer].
 */
class BottomSheet : Drawer(), OnBottomSheetItemClickListener {
    companion object {
        private const val ITEMS = "items"

        /**
         * @param items is an ArrayList of [BottomSheetItem]s.
         */
        @JvmStatic
        fun newInstance(items: ArrayList<BottomSheetItem>): BottomSheet {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ITEMS, items)

            val bottomSheet = BottomSheet()
            bottomSheet.arguments = bundle
            return bottomSheet
        }
    }

    private lateinit var items: ArrayList<BottomSheetItem>
    private var clickedItemId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val bundle = savedInstanceState ?: arguments ?: return
        items = bundle.getParcelableArrayList<BottomSheetItem>(ITEMS) ?: return

        val adapter = BottomSheetAdapter(context, items)
        adapter.onBottomSheetItemClickListener = this

        val recyclerView = createRecyclerView(context)
        contentView = recyclerView
        recyclerView.adapter = adapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ITEMS, items)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (clickedItemId > -1) {
            (parentFragment as? OnBottomSheetItemClickListener)?.onBottomSheetItemClick(clickedItemId)
            (activity as? OnBottomSheetItemClickListener)?.onBottomSheetItemClick(clickedItemId)
        }
    }

    override fun onBottomSheetItemClick(id: Int) {
        clickedItemId = id
        collapse()
    }

    private fun createRecyclerView(context: Context): RecyclerView {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val verticalPadding = resources.getDimension(R.dimen.uifabric_bottom_sheet_vertical_padding).toInt()
        recyclerView.setPadding(0, verticalPadding, 0, verticalPadding)

        return recyclerView
    }
}

interface OnBottomSheetItemClickListener {
    fun onBottomSheetItemClick(id: Int)
}
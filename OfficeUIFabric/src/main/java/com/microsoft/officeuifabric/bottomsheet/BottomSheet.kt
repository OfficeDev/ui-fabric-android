/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment

/**
 * [BottomSheet] is used to display a list of menu items in a modal dialog inside of a Fragment that retains state.
 */
class BottomSheet : AppCompatDialogFragment(), OnBottomSheetItemClickListener {
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

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var items: ArrayList<BottomSheetItem>
    private var clickedItemId: Int = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = savedInstanceState ?: arguments
        items = bundle?.getParcelableArrayList<BottomSheetItem>(ITEMS) ?: arrayListOf()
        bottomSheetDialog = BottomSheetDialog(context!!, items)
        bottomSheetDialog.onItemClickListener = this
        return bottomSheetDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ITEMS, items)
    }

    override fun onBottomSheetItemClick(id: Int) {
        clickedItemId = id
    }

    // According to Android documentation, DialogFragment owns the Dialog setOnDismissListener callback so this
    // can't be set on the Dialog. Instead onDismiss(android.content.DialogInterface) must be overridden.
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (clickedItemId > -1) {
            (parentFragment as? OnBottomSheetItemClickListener)?.onBottomSheetItemClick(clickedItemId)
            (activity as? OnBottomSheetItemClickListener)?.onBottomSheetItemClick(clickedItemId)
            clickedItemId = -1
        }
    }
}
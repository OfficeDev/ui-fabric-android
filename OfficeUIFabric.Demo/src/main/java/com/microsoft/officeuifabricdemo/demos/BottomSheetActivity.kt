/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import com.microsoft.officeuifabric.bottomsheet.BottomSheet
import com.microsoft.officeuifabric.bottomsheet.BottomSheetItem
import com.microsoft.officeuifabric.bottomsheet.OnBottomSheetItemClickListener
import com.microsoft.officeuifabric.snackbar.Snackbar
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_bottom_sheet.*
import kotlinx.android.synthetic.main.activity_demo_detail.*

class BottomSheetActivity : DemoActivity(), OnBottomSheetItemClickListener {
    override val contentLayoutId: Int
        get() = R.layout.activity_bottom_sheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        open_bottom_sheet_button.setOnClickListener {
            val bottomSheet = BottomSheet.newInstance(
                arrayListOf(
                    BottomSheetItem(
                        R.id.bottom_sheet_item_flag,
                        R.drawable.ic_flag,
                        resources.getString(R.string.bottom_sheet_item_flag_label)),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_reply,
                        R.drawable.ic_reply,
                        resources.getString(R.string.bottom_sheet_item_reply_label)),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_forward,
                        R.drawable.ic_forward,
                        resources.getString(R.string.bottom_sheet_item_forward_label)),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_delete,
                        R.drawable.ic_trash_can,
                        resources.getString(R.string.bottom_sheet_item_delete_label))
                )
            )
            bottomSheet.show(supportFragmentManager, null)
        }
    }

    override fun onBottomSheetItemClick(id: Int) {
        when(id) {
            R.id.bottom_sheet_item_flag -> showSnackbar(resources.getString(R.string.bottom_sheet_item_flag_toast))
            R.id.bottom_sheet_item_reply -> showSnackbar(resources.getString(R.string.bottom_sheet_item_reply_toast))
            R.id.bottom_sheet_item_forward -> showSnackbar(resources.getString(R.string.bottom_sheet_item_forward_toast))
            R.id.bottom_sheet_item_delete -> showSnackbar(resources.getString(R.string.bottom_sheet_item_delete_toast))
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(root_view, message).show()
    }
}
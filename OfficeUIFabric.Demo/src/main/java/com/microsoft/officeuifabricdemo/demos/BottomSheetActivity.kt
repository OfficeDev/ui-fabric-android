/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import com.microsoft.officeuifabric.bottomsheet.BottomSheet
import com.microsoft.officeuifabric.bottomsheet.BottomSheetDialog
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

    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        show_with_single_line_items_button.setOnClickListener {
            val bottomSheet = BottomSheet.newInstance(
                arrayListOf(
                    BottomSheetItem(
                        R.id.bottom_sheet_item_flag,
                        R.drawable.ic_flag,
                        getString(R.string.bottom_sheet_item_flag_title)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_reply,
                        R.drawable.ic_reply,
                        getString(R.string.bottom_sheet_item_reply_title)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_forward,
                        R.drawable.ic_forward,
                        getString(R.string.bottom_sheet_item_forward_title)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_delete,
                        R.drawable.ic_trash_can,
                        getString(R.string.bottom_sheet_item_delete_title)
                    )
                )
            )
            bottomSheet.show(supportFragmentManager, null)
        }

        show_with_double_line_items_button.setOnClickListener {
            val bottomSheet = BottomSheet.newInstance(
                arrayListOf(
                    BottomSheetItem(
                        R.id.bottom_sheet_item_camera,
                        R.drawable.ic_camera,
                        getString(R.string.bottom_sheet_item_camera_title),
                        getString(R.string.bottom_sheet_item_camera_subtitle)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_gallery,
                        R.drawable.ic_gallery,
                        getString(R.string.bottom_sheet_item_gallery_title),
                        getString(R.string.bottom_sheet_item_gallery_subtitle)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_videos,
                        R.drawable.ic_videos,
                        getString(R.string.bottom_sheet_item_videos_title),
                        getString(R.string.bottom_sheet_item_videos_subtitle)
                    ),
                    BottomSheetItem(
                        R.id.bottom_sheet_item_manage,
                        R.drawable.ic_wrench,
                        getString(R.string.bottom_sheet_item_manage_title),
                        getString(R.string.bottom_sheet_item_manage_subtitle)
                    )
                )
            )
            bottomSheet.show(supportFragmentManager, null)
        }

        show_bottom_sheet_dialog_button.setOnClickListener {
            if (bottomSheetDialog == null) {
                bottomSheetDialog = BottomSheetDialog(
                    this,
                    arrayListOf(
                        BottomSheetItem(
                            R.id.bottom_sheet_item_flag,
                            R.drawable.ic_flag,
                            getString(R.string.bottom_sheet_item_flag_title)
                        ),
                        BottomSheetItem(
                            R.id.bottom_sheet_item_reply,
                            R.drawable.ic_reply,
                            getString(R.string.bottom_sheet_item_reply_title)
                        ),
                        BottomSheetItem(
                            R.id.bottom_sheet_item_forward,
                            R.drawable.ic_forward,
                            getString(R.string.bottom_sheet_item_forward_title)
                        ),
                        BottomSheetItem(
                            R.id.bottom_sheet_item_delete,
                            R.drawable.ic_trash_can,
                            getString(R.string.bottom_sheet_item_delete_title)
                        )
                    )
                )
                bottomSheetDialog?.onItemClickListener = this
            }

            bottomSheetDialog?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetDialog?.dismiss()
    }

    override fun onBottomSheetItemClick(id: Int) {
        when(id) {
            // single line items
            R.id.bottom_sheet_item_flag -> showSnackbar(resources.getString(R.string.bottom_sheet_item_flag_toast))
            R.id.bottom_sheet_item_reply -> showSnackbar(resources.getString(R.string.bottom_sheet_item_reply_toast))
            R.id.bottom_sheet_item_forward -> showSnackbar(resources.getString(R.string.bottom_sheet_item_forward_toast))
            R.id.bottom_sheet_item_delete -> showSnackbar(resources.getString(R.string.bottom_sheet_item_delete_toast))

            // double line items
            R.id.bottom_sheet_item_camera -> showSnackbar(resources.getString(R.string.bottom_sheet_item_camera_toast))
            R.id.bottom_sheet_item_gallery -> showSnackbar(resources.getString(R.string.bottom_sheet_item_gallery_toast))
            R.id.bottom_sheet_item_videos -> showSnackbar(resources.getString(R.string.bottom_sheet_item_videos_toast))
            R.id.bottom_sheet_item_manage -> showSnackbar(resources.getString(R.string.bottom_sheet_item_manage_toast))

            // dialog
            R.id.bottom_sheet_item_clock -> showSnackbar(resources.getString(R.string.bottom_sheet_item_clock_toast))
            R.id.bottom_sheet_item_alarm -> showSnackbar(resources.getString(R.string.bottom_sheet_item_alarm_toast))
            R.id.bottom_sheet_item_stop_watch -> showSnackbar(resources.getString(R.string.bottom_sheet_item_stop_watch_toast))
            R.id.bottom_sheet_item_time_zone -> showSnackbar(resources.getString(R.string.bottom_sheet_item_time_zone_toast))
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(root_view, message).show()
    }
}
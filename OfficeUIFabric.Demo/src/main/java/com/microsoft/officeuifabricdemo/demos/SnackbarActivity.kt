/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.View
import com.microsoft.officeuifabric.snackbar.Snackbar
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_demo_detail.*
import kotlinx.android.synthetic.main.activity_snackbar.*

class SnackbarActivity : DemoActivity(), View.OnClickListener {
    override val contentLayoutId: Int
        get() = R.layout.activity_snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btn_snackbar_single_line.setOnClickListener(this)
        btn_snackbar_single_line_icon.setOnClickListener(this)
        btn_snackbar_single_line_action.setOnClickListener(this)
        btn_snackbar_single_line_action_icon.setOnClickListener(this)
        btn_snackbar_multi_line.setOnClickListener(this)
        btn_snackbar_multi_line_icon.setOnClickListener(this)
        btn_snackbar_multi_line_action.setOnClickListener(this)
        btn_snackbar_multi_line_action_icon.setOnClickListener(this)
        btn_snackbar_multi_line_long_action.setOnClickListener(this)
        btn_snackbar_announcement.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_snackbar_single_line ->
                Snackbar.make(root_view, getString(R.string.snackbar_single_line)).show()

            R.id.btn_snackbar_single_line_icon ->
                Snackbar.make(root_view, getString(R.string.snackbar_single_line))
                    .setIcon(R.drawable.ic_done_white)
                    .show()

            R.id.btn_snackbar_single_line_action ->
                Snackbar.make(root_view, getString(R.string.snackbar_single_line))
                    .setAction(getString(R.string.snackbar_action), View.OnClickListener {
                        // handle click here
                    })
                    .show()

            R.id.btn_snackbar_single_line_action_icon ->
                Snackbar.make(root_view, getString(R.string.snackbar_single_line))
                    .setIcon(R.drawable.ic_done_white)
                    .setAction(getString(R.string.snackbar_action), View.OnClickListener {
                        // handle click here
                    })
                    .show()

            R.id.btn_snackbar_multi_line ->
                Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_LONG).show()

            R.id.btn_snackbar_multi_line_icon ->
                Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_LONG)
                    .setIcon(R.drawable.ic_done_white)
                    .show()

            R.id.btn_snackbar_multi_line_action ->
                Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.snackbar_action), View.OnClickListener {
                        // handle click here
                    })
                    .show()

            R.id.btn_snackbar_multi_line_action_icon ->
                Snackbar.make(root_view, getString(R.string.snackbar_multi_line))
                    .setIcon(R.drawable.ic_done_white)
                    .setAction(getString(R.string.snackbar_action), View.OnClickListener {
                        // handle click here
                    })
                    .show()

            R.id.btn_snackbar_multi_line_long_action ->
                Snackbar.make(root_view, getString(R.string.snackbar_multi_line))
                    .setAction(getString(R.string.snackbar_action_long), View.OnClickListener {
                        // handle click here
                    })
                    .show()

            R.id.btn_snackbar_announcement ->
                Snackbar.make(root_view, getString(R.string.snackbar_announcement), style = Snackbar.Style.ANNOUNCEMENT)
                    .setIcon(R.drawable.ic_birthday)
                    .setAction(getString(R.string.snackbar_action), View.OnClickListener {
                        // handle click here
                    })
                    .show()
        }
    }
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
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
        btn_snackbar_multi_line.setOnClickListener(this)
        btn_snackbar_announcement.setOnClickListener(this)
        btn_snackbar_multi_line_action.setOnClickListener(this)
        btn_snackbar_multi_line_long_action.setOnClickListener(this)
        btn_snackbar_single_line_action.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_snackbar_single_line -> Snackbar.make(root_view, getString(R.string.snackbar_single_line), Snackbar.LENGTH_SHORT).show()

            R.id.btn_snackbar_multi_line -> Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_LONG).show()

            R.id.btn_snackbar_single_line_action -> {
                val snackbar = Snackbar.make(root_view, getString(R.string.snackbar_single_line), Snackbar.LENGTH_SHORT)
                snackbar.setAction(getString(R.string.snackbar_action), View.OnClickListener {
                    // handle click here
                })
                snackbar.show()
            }

            R.id.btn_snackbar_multi_line_action -> {
                val snackbar = Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(getString(R.string.snackbar_action), View.OnClickListener {
                    // handle click here
                })
                snackbar.show()
            }

            R.id.btn_snackbar_multi_line_long_action -> {
                val snackbar = Snackbar.make(root_view, getString(R.string.snackbar_multi_line), Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(getString(R.string.snackbar_action_long), View.OnClickListener {
                    // handle click here
                })
                snackbar.show()
            }

            R.id.btn_snackbar_announcement -> {
                val snackbar = Snackbar.make(
                    root_view,
                    getString(R.string.snackbar_announcement),
                    Snackbar.LENGTH_INDEFINITE,
                    Snackbar.Style.ANNOUNCEMENT
                )
                snackbar.setIcon(R.drawable.ms_ic_birthday)
                snackbar.setAction(getString(R.string.snackbar_action), View.OnClickListener {
                    // handle click here
                })
                snackbar.show()
            }
        }
    }
}

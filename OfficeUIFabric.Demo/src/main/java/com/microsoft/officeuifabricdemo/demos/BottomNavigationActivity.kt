/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.support.design.bottomnavigation.LabelVisibilityMode
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

// TODO Replace outlined icons with fill icons when selected.
class BottomNavigationActivity : DemoActivity() {
    override val contentLayoutId: Int
        get() = R.layout.activity_bottom_navigation

    override val contentNeedsScrollableContainer: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toggle_label_button.setOnClickListener {
            // You can also achieve unlabeled items via @style/Widget.UIFabric.BottomNavigation.Unlabeled
            bottom_navigation.labelVisibilityMode =
                if (bottom_navigation.labelVisibilityMode == LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED)
                    LabelVisibilityMode.LABEL_VISIBILITY_LABELED
                else
                    LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
        }

        three_menu_items_button.setOnClickListener {
            bottom_navigation.menu.removeItem(R.id.action_share)
            bottom_navigation.menu.removeItem(R.id.action_settings)
        }

        four_menu_items_button.setOnClickListener {
            bottom_navigation.menu.removeItem(R.id.action_share)
            bottom_navigation.menu.removeItem(R.id.action_settings)

            bottom_navigation.menu.add(
                R.id.bottom_navigation,
                R.id.action_share,
                3,
                resources.getString(R.string.bottom_navigation_menu_item_shared)
            ).setIcon(R.drawable.ic_share)
        }

        five_menu_items_button.setOnClickListener {
            bottom_navigation.menu.removeItem(R.id.action_share)
            bottom_navigation.menu.removeItem(R.id.action_settings)

            bottom_navigation.menu.add(
                R.id.bottom_navigation,
                R.id.action_share,
                3,
                resources.getString(R.string.bottom_navigation_menu_item_shared)
            ).setIcon(R.drawable.ic_share)
            bottom_navigation.menu.add(
                R.id.bottom_navigation,
                R.id.action_settings,
                4,
                resources.getString(R.string.bottom_navigation_menu_item_settings)
            ).setIcon(R.drawable.ic_wrench)
        }
    }
}
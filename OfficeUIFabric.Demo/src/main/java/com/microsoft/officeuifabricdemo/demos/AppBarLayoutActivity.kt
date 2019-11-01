/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.view.Menu
import android.view.View
import com.microsoft.officeuifabric.appbarlayout.AppBarLayout
import com.microsoft.officeuifabric.listitem.ListItemDivider
import com.microsoft.officeuifabric.listitem.ListSubHeaderView
import com.microsoft.officeuifabric.search.Searchbar
import com.microsoft.officeuifabric.snackbar.Snackbar
import com.microsoft.officeuifabric.util.ThemeUtil
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import com.microsoft.officeuifabricdemo.demos.list.*
import com.microsoft.officeuifabricdemo.util.Avatar
import kotlinx.android.synthetic.main.activity_app_bar_layout.*
import kotlinx.android.synthetic.main.activity_demo_detail.*
import kotlinx.android.synthetic.main.activity_demo_list.app_bar

class AppBarLayoutActivity : DemoActivity(), View.OnClickListener {
    companion object {
        private var themeId = R.style.AppTheme
        private const val SCROLL_BEHAVIOR = "scrollBehavior"
        private const val NAVIGATION_ICON_TYPE = "navigationIconType"
        private const val SHOW_ACCESSORY_VIEW = "showAccessoryView"
    }

    enum class NavigationIconType {
        NONE, AVATAR, BACK_ICON
    }

    override val contentLayoutId: Int
        get() = R.layout.activity_app_bar_layout

    override val contentNeedsScrollableContainer: Boolean
        get() = false

    private var scrollBehavior: AppBarLayout.ScrollBehavior = AppBarLayout.ScrollBehavior.COLLAPSE_TOOLBAR
        set(value) {
            field = value
            updateScrollBehavior()
        }
    private var showAccessoryView: Boolean = true
        set(value) {
            field = value
            updateAccessoryView()
        }
    private var navigationIconType: NavigationIconType = NavigationIconType.BACK_ICON
        set(value) {
            field = value
            updateNavigationIcon()
        }

    private val adapter = ListAdapter(this)
    private lateinit var scrollBehaviorSubHeader: ListSubHeader
    private lateinit var navigationIconButton: ButtonItem
    private lateinit var accessoryViewButton: ButtonItem

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(themeId)

        var scrollBehaviorOrdinal = scrollBehavior.ordinal
        var navigationIconTypeOrdinal = navigationIconType.ordinal
        savedInstanceState?.let {
            scrollBehaviorOrdinal = it.getInt(SCROLL_BEHAVIOR)
            navigationIconTypeOrdinal = it.getInt(NAVIGATION_ICON_TYPE)
            showAccessoryView = it.getBoolean(SHOW_ACCESSORY_VIEW)
        }
        scrollBehavior = AppBarLayout.ScrollBehavior.values()[scrollBehaviorOrdinal]
        navigationIconType = NavigationIconType.values()[navigationIconTypeOrdinal]

        super.onCreate(savedInstanceState)

        scrollBehaviorSubHeader = createListSubHeader(
            resources.getString(R.string.app_bar_layout_toggle_scroll_behavior_sub_header, scrollBehavior.toString())
        )
        navigationIconButton = ButtonItem(
            buttonText = resources.getString(R.string.app_bar_layout_hide_icon_button),
            id = R.id.app_bar_layout_toggle_navigation_icon_button,
            onClickListener = this
        )
        accessoryViewButton = ButtonItem(
            buttonText = resources.getString(R.string.app_bar_layout_hide_accessory_view_button),
            id = R.id.app_bar_layout_toggle_accessory_view_button,
            onClickListener = this
        )

        setupList()
        app_bar.scrollTargetViewId = R.id.app_bar_layout_list

        updateScrollBehavior()
        updateNavigationIcon()
        updateAccessoryView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt(SCROLL_BEHAVIOR, scrollBehavior.ordinal)
        outState?.putInt(NAVIGATION_ICON_TYPE, navigationIconType.ordinal)
        outState?.putBoolean(SHOW_ACCESSORY_VIEW, showAccessoryView)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.app_bar_layout_toggle_scroll_behavior_button -> {
                scrollBehavior = when (scrollBehavior) {
                    AppBarLayout.ScrollBehavior.NONE -> AppBarLayout.ScrollBehavior.COLLAPSE_TOOLBAR
                    AppBarLayout.ScrollBehavior.COLLAPSE_TOOLBAR -> AppBarLayout.ScrollBehavior.PIN
                    AppBarLayout.ScrollBehavior.PIN -> AppBarLayout.ScrollBehavior.NONE
                }
            }
            R.id.app_bar_layout_toggle_navigation_icon_button ->
                navigationIconType = when (navigationIconType) {
                    NavigationIconType.NONE -> NavigationIconType.AVATAR
                    NavigationIconType.AVATAR -> NavigationIconType.BACK_ICON
                    NavigationIconType.BACK_ICON -> NavigationIconType.NONE
                }
            R.id.app_bar_layout_toggle_accessory_view_button ->
                showAccessoryView = !showAccessoryView
            R.id.app_bar_layout_toggle_theme_button -> {
                themeId = if (themeId == R.style.AppTheme)
                    R.style.AppTheme_Orange
                else
                    R.style.AppTheme

                recreate()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app_bar_layout, menu)

        for (index in 0 until menu.size()) {
            val drawable = menu.getItem(index).icon
            drawable?.setColorFilter(
                ThemeUtil.getThemeAttrColor(this, R.attr.uifabricToolbarIconColor),
                PorterDuff.Mode.SRC_IN
            )
        }

        return true
    }

    private fun updateScrollBehavior() {
        if (app_bar == null)
            return

        app_bar.scrollBehavior = scrollBehavior

        scrollBehaviorSubHeader.title =
            resources.getString(
                R.string.app_bar_layout_toggle_scroll_behavior_sub_header,
                scrollBehavior.toString()
            )

        adapter.notifyDataSetChanged()
    }

    private fun updateNavigationIcon() {
        if (app_bar == null)
            return

        when (navigationIconType) {
            NavigationIconType.NONE -> {
                app_bar.toolbar.navigationIcon = null

                navigationIconButton.buttonText = resources.getString(R.string.app_bar_layout_show_avatar_button)
            }
            NavigationIconType.AVATAR -> {
                val avatar = Avatar(resources.getString(R.string.persona_name_mauricio_august))
                avatar.avatarImageResourceId = R.drawable.avatar_mauricio_august
                app_bar.toolbar.setNavigationOnClickListener {
                    Snackbar.make(root_view, getString(R.string.app_bar_layout_navigation_icon_clicked)).show()
                }
                app_bar.toolbar.avatar = avatar

                navigationIconButton.buttonText = resources.getString(R.string.app_bar_layout_show_back_icon_button)
            }
            NavigationIconType.BACK_ICON -> {
                val backArrow = ContextCompat.getDrawable(this, R.drawable.ms_ic_arrow_back)
                backArrow?.setTint(ThemeUtil.getThemeAttrColor(this, R.attr.uifabricToolbarIconColor))
                app_bar.toolbar.navigationIcon = backArrow
                app_bar.toolbar.setNavigationOnClickListener {
                    onBackPressed()
                }

                navigationIconButton.buttonText = resources.getString(R.string.app_bar_layout_hide_icon_button)
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun updateAccessoryView() {
        if (app_bar == null)
            return

        if (showAccessoryView) {
            app_bar.accessoryView = Searchbar(this)
            accessoryViewButton.buttonText = resources.getString(R.string.app_bar_layout_hide_accessory_view_button)
        } else {
            app_bar.accessoryView = null
            accessoryViewButton.buttonText = resources.getString(R.string.app_bar_layout_show_accessory_view_button)
        }

        adapter.notifyDataSetChanged()
    }

    private fun setupList() {
        adapter.listItems = createList()
        app_bar_layout_list.adapter = adapter
        app_bar_layout_list.addItemDecoration(ListItemDivider(this, DividerItemDecoration.VERTICAL))
    }

    private fun createList(): ArrayList<IBaseListItem> {
        val scrollBehaviorSection = createSection(
            scrollBehaviorSubHeader,
            arrayListOf(
                ButtonItem(
                    buttonText = resources.getString(R.string.app_bar_layout_toggle_scroll_behavior_button),
                    id = R.id.app_bar_layout_toggle_scroll_behavior_button,
                    onClickListener = this
                )
            )
        )

        val navigationIconSection = createSection(
            createListSubHeader(resources.getString(R.string.app_bar_layout_toggle_navigation_icon_sub_header)),
            arrayListOf(navigationIconButton)
        )

        val accessoryViewSection = createSection(
            createListSubHeader(resources.getString(R.string.app_bar_layout_toggle_accessory_view_sub_header)),
            arrayListOf(accessoryViewButton)
        )

        val themeSection = createSection(
            createListSubHeader(resources.getString(R.string.app_bar_layout_toggle_theme_sub_header)),
            arrayListOf(ButtonItem(
                buttonText = resources.getString(R.string.app_bar_layout_toggle_theme_button),
                id = R.id.app_bar_layout_toggle_theme_button,
                onClickListener = this
            ))
        )

        val extraListItems = ArrayList<IBaseListItem>()
        for (index in 0..35)
            extraListItems.add(ListItem("${getString(R.string.app_bar_layout_list_item)} $index"))

        val extraScrollableContextSection = createSection(
            createListSubHeader(getString(R.string.app_bar_layout_list_sub_header)),
            extraListItems
        )

        return (scrollBehaviorSection + navigationIconSection + accessoryViewSection + themeSection + extraScrollableContextSection) as ArrayList<IBaseListItem>
    }

    private fun createSection(subHeader: ListSubHeader, items: ArrayList<IBaseListItem>): ArrayList<IBaseListItem> {
        val itemArray = arrayListOf(subHeader) as ArrayList<IBaseListItem>
        itemArray.addAll(items)
        return itemArray
    }

    private fun createListSubHeader(text: String): ListSubHeader {
        val listSubHeader = ListSubHeader(text)
        listSubHeader.titleColor = ListSubHeaderView.TitleColor.SECONDARY
        return listSubHeader
    }
}
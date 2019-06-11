/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import com.microsoft.officeuifabric.listitem.ListItemView
import com.microsoft.officeuifabric.listitem.ListSubHeaderView
import com.microsoft.officeuifabric.snackbar.Snackbar
import com.microsoft.officeuifabricdemo.R
import java.util.*

class ListAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private enum class ViewType {
        SUB_HEADER, ITEM
    }

    var listItems = ArrayList<IBaseListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeOrdinal: Int): RecyclerView.ViewHolder {
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        return when (ViewType.values()[viewTypeOrdinal]) {
            ViewType.SUB_HEADER -> {
                val subHeaderView = ListSubHeaderView(context)
                subHeaderView.layoutParams = lp
                ListSubHeaderViewHolder(subHeaderView)
            }
            ViewType.ITEM -> {
                val listItemView = ListItemView(context)
                listItemView.layoutParams = lp
                ListItemViewHolder(listItemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = listItems[position]

        (holder as? ListSubHeaderViewHolder)?.setTitle(listItem.title)

        if (listItem is IListItem)
            (holder as? ListItemViewHolder)?.setListItem(listItem)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? ListItemViewHolder)?.clearCustomViews()
    }

    override fun getItemCount(): Int = listItems.size

    override fun getItemViewType(position: Int): Int {
        return if (listItems[position] is ListSubHeader)
            ViewType.SUB_HEADER.ordinal
        else
            ViewType.ITEM.ordinal
    }

    private inner class ListItemViewHolder : RecyclerView.ViewHolder {
        private val listItemView: ListItemView

        constructor(view: ListItemView) : super(view) {
            listItemView = view
            listItemView.setOnClickListener {
                Snackbar.make(listItemView, context.resources.getString(R.string.list_item_click), Snackbar.LENGTH_SHORT).show()
            }
        }

        fun setListItem(listItem: IListItem) {
            listItemView.setListItem(listItem)
        }

        fun clearCustomViews() {
            listItemView.customView = null
            listItemView.customAccessoryView = null
        }
    }

    private class ListSubHeaderViewHolder : RecyclerView.ViewHolder {
        private val listSubHeaderView: ListSubHeaderView

        constructor(view: ListSubHeaderView) : super(view) {
            listSubHeaderView = view
        }

        fun setTitle(title: String) {
            listSubHeaderView.title = title
        }
    }
}

fun ListItemView.setListItem(listItem: IListItem?) {
    title = listItem?.title ?: ""
    subtitle = listItem?.subtitle ?: ""
    footer = listItem?.footer ?: ""

    titleMaxLines = listItem?.titleMaxLines ?: ListItemView.DEFAULT_MAX_LINES
    subtitleMaxLines = listItem?.subtitleMaxLines ?: ListItemView.DEFAULT_MAX_LINES
    footerMaxLines = listItem?.footerMaxLines ?: ListItemView.DEFAULT_MAX_LINES

    titleTruncateAt = listItem?.titleTruncateAt ?: ListItemView.DEFAULT_TRUNCATION
    subtitleTruncateAt = listItem?.subtitleTruncateAt ?: ListItemView.DEFAULT_TRUNCATION
    footerTruncateAt = listItem?.footerTruncateAt ?: ListItemView.DEFAULT_TRUNCATION

    customView = listItem?.customView
    customViewSize = listItem?.customViewSize ?: ListItemView.DEFAULT_CUSTOM_VIEW_SIZE
    customAccessoryView = listItem?.customAccessoryView

    layoutDensity = listItem?.layoutDensity ?: ListItemView.DEFAULT_LAYOUT_DENSITY
}
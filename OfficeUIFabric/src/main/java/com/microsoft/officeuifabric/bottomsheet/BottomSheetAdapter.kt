/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.bottomsheet

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.microsoft.officeuifabric.listitem.ListItemView

internal class BottomSheetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    var onBottomSheetItemClickListener: OnBottomSheetItemClickListener? = null

    private val context: Context
    private val items: ArrayList<BottomSheetItem>

    constructor(context: Context, items: ArrayList<BottomSheetItem>) {
        this.context = context
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = ListItemView(context)
        itemView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        itemView.customViewSize = ListItemView.CustomViewSize.SMALL
        return BottomSheetItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? BottomSheetItemViewHolder)?.setBottomSheetItem(items[position])
    }

    override fun getItemCount(): Int = items.size

    private inner class BottomSheetItemViewHolder : RecyclerView.ViewHolder {
        private val listItemView: ListItemView

        constructor(itemView: ListItemView) : super(itemView) {
            listItemView = itemView
        }

        fun setBottomSheetItem(item: BottomSheetItem) {
            listItemView.customView = createImageView(item.imageId)
            listItemView.title = item.title
            listItemView.subtitle = item.subtitle
            listItemView.layoutDensity = ListItemView.LayoutDensity.COMPACT

            listItemView.setOnClickListener {
                onBottomSheetItemClickListener?.onBottomSheetItemClick(item.id)
            }
        }

        private fun createImageView(imageId: Int): ImageView {
            val drawable = ContextCompat.getDrawable(context, imageId)
            val imageView = ImageView(context)
            imageView.setImageDrawable(drawable)
            return imageView
        }
    }
}
/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.listitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.View
import com.microsoft.officeuifabric.R

/**
 * This [DividerItemDecoration] is designed to be used with [RecyclerView]s containing [ListItemView]s.
 * [ListItemDivider] will have an inset matching the text views' inset,
 * which increases at the start of the list item view when a [ListItemView.customView] is present.
 * Dividers after [ListSubHeaderView]s are removed.
 * If your view has multiple [ListSubHeaderView]s, this class draws a section divider with no inset
 * between the last list item view in the section and next section's sub header view.
 */
class ListItemDivider : DividerItemDecoration {
    private val context: Context

    private val dividerPaint: Paint = Paint()
    private val spacerPaint: Paint = Paint()

    private val dividerHeight: Float
    private val subHeaderDividerPadding: Float

    constructor(context: Context, orientation: Int) : super(context, orientation) {
        this.context = context

        dividerHeight = context.resources.getDimension(R.dimen.uifabric_divider_height)
        subHeaderDividerPadding = context.resources.getDimension(R.dimen.uifabric_list_sub_header_divider_padding)

        dividerPaint.style = Paint.Style.FILL
        dividerPaint.color = ContextCompat.getColor(context, R.color.uifabric_divider)

        spacerPaint.style = Paint.Style.FILL
        spacerPaint.color = ContextCompat.getColor(context, android.R.color.transparent)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val viewPosition = parent.getChildAdapterPosition(view)
        val previousView = parent.getChildAt(viewPosition - 1)

        outRect.top = when {
            viewPosition == 0  && view is ListSubHeaderView -> subHeaderDividerPadding.toInt()
            view is ListSubHeaderView -> ((subHeaderDividerPadding * 2) + dividerHeight).toInt()
            previousView is ListSubHeaderView -> 0
            else -> dividerHeight.toInt()
        }

        outRect.bottom = 0
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (listItemIndex in 1 until parent.childCount) {
            val itemView = parent.getChildAt(listItemIndex)
            val listItemView = itemView as? ListItemView
            val previousView = parent.getChildAt(listItemIndex - 1)

            // SubHeaders have no dividers.
            if (previousView is ListSubHeaderView)
                continue

            val useSectionDivider = itemView is ListSubHeaderView

            val topOfFirstSpacer = itemView.top.toFloat() - (subHeaderDividerPadding * 2) - dividerHeight
            val bottomOfFirstSpacer = topOfFirstSpacer + subHeaderDividerPadding

            val topOfDivider = if (useSectionDivider) bottomOfFirstSpacer else itemView.top.toFloat() - dividerHeight
            val bottomOfDivider = topOfDivider + dividerHeight

            val topOfSecondSpacer = bottomOfDivider
            val bottomOfSecondSpacer = topOfSecondSpacer + subHeaderDividerPadding

            val leftOfDivider = if (useSectionDivider) 0f else listItemView?.textAreaStartInset ?: 0f
            val rightOfDivider = itemView.right - if (useSectionDivider) 0f else listItemView?.textAreaEndInset ?: 0f

            val spacerLeft = itemView.left.toFloat()
            val spacerRight = itemView.right.toFloat()

            if (useSectionDivider)
                // Draw the spacer
                canvas.drawRect(spacerLeft, topOfFirstSpacer, spacerRight, bottomOfFirstSpacer, spacerPaint)

            // Draw the divider
            canvas.drawRect(leftOfDivider, topOfDivider, rightOfDivider, bottomOfDivider, dividerPaint)

            if (useSectionDivider)
                // Draw the spacer
                canvas.drawRect(spacerLeft, topOfSecondSpacer, spacerRight, bottomOfSecondSpacer, spacerPaint)
        }
    }
}
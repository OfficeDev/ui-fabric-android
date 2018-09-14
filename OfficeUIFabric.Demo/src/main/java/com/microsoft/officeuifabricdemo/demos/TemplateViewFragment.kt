//
// Copyright © 2018 Microsoft Corporation. All rights reserved.
//

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import com.microsoft.officeuifabricdemo.demos.views.Cell
import com.microsoft.officeuifabricdemo.demos.views.CellOrientation
import kotlinx.android.synthetic.main.fragment_templateview.*
import kotlinx.android.synthetic.main.template_cell_vertical.view.*

class TemplateViewFragment : DemoFragment() {
    companion object {
        const val LIST_ITEM_COUNT = 1000
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_templateview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        horizontal_cell.setOnClickListener { onCellClicked(it as Cell) }
        vertical_cell.setOnClickListener { onCellClicked(it as Cell) }

        template_list_view.adapter = TemplateListViewAdapter()
        template_list_view.layoutManager = LinearLayoutManager(context)
        template_list_view.setHasFixedSize(true)

        regular_list_view.adapter = RegularListViewAdapter()
        regular_list_view.layoutManager = LinearLayoutManager(context)
        regular_list_view.setHasFixedSize(true)

        calculate_cells_button.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val t = measureAndLayoutViews(createView = {
                val cell = Cell(context)
                cell.orientation = CellOrientation.VERTICAL
                return@measureAndLayoutViews cell
            })
            println("Cell.M&L: $t")
            calculate_cells_button.text = context.getString(R.string.calculate_cells) + " = $t ms"
        }

        calculate_layouts_button.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val t = measureAndLayoutViews(createView = {
                // Emulation of Cell code without extra ViewGroup (Cell itself)
                val cell = LayoutInflater.from(context).inflate(R.layout.template_cell_vertical, null)
                /*val titleView = */cell.findViewById(R.id.cell_title) as TextView
                /*val descriptionView = */cell.findViewById(R.id.cell_description) as TextView
                return@measureAndLayoutViews cell
            })
            println("Layout.M&L: $t")
            calculate_layouts_button.text = context.getString(R.string.calculate_layouts) + " = $t ms"
        }
    }

    private fun measureAndLayoutViews(createView: () -> View): Long  {
        val t1 = System.nanoTime()

        for (i in 1..100) {
            val cell = createView()
            cell.requestLayout()
            cell.measure(0, 0)
            cell.layout(0, 0, cell.measuredWidth, cell.measuredHeight)
        }

        val t2 = System.nanoTime()
        return (t2 - t1) / 1000000
    }

    private fun onCellClicked(cell: Cell) {
        cell.orientation = when (cell.orientation) {
            CellOrientation.HORIZONTAL -> CellOrientation.VERTICAL
            CellOrientation.VERTICAL -> CellOrientation.HORIZONTAL
        }
    }

    // Template list view adapter

    private class TemplateListViewAdapter : RecyclerView.Adapter<TemplateListViewAdapter.ViewHolder>() {
        override fun getItemCount(): Int = LIST_ITEM_COUNT

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val cell = Cell(parent.context)
            cell.orientation = CellOrientation.VERTICAL
            cell.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return ViewHolder(cell)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.cell.apply {
                title = "Title $position"
                description = "Description $position"
            }
        }

        class ViewHolder(val cell: Cell) : RecyclerView.ViewHolder(cell)
    }

    // Regular list view adapter

    private class RegularListViewAdapter : RecyclerView.Adapter<RegularListViewAdapter.ViewHolder>() {
        override fun getItemCount(): Int = LIST_ITEM_COUNT

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val cell = LayoutInflater.from(parent.context).inflate(R.layout.template_cell_vertical, parent, false)
            cell.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            return ViewHolder(cell)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                titleView.text = "Title $position"
                descriptionView.text = "Description $position"
            }
        }

        class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
            val titleView: TextView = cell.cell_title
            val descriptionView: TextView = cell.cell_description
        }
    }
}
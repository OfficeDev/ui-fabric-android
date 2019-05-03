/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo

import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_demo_list.*
import kotlinx.android.synthetic.main.demo_list.*
import kotlinx.android.synthetic.main.demo_list_content.view.*

/**
 * This activity presents a list of [Demo]s, which when touched,
 * lead to a subclass of [DemoActivity] representing demo details.
 */
class DemoListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Launch Screen: Setting the theme here removes the launch screen, which was added to this activity
        // by setting the theme to App.Launcher in the manifest.
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo_list)

        setSupportActionBar(toolbar)
        toolbar.title = title
        toolbar.subtitle = com.microsoft.officeuifabric.BuildConfig.VERSION_NAME

        demo_list.adapter = DemoListAdapter()
        addDemoListDivider()

        Initializer.init(application)
    }

    private fun addDemoListDivider() {
        val drawable = ContextCompat.getDrawable(this, com.microsoft.officeuifabric.R.drawable.ms_row_divider)
        val spacing = resources.getDimension(R.dimen.default_layout_margin).toInt()
        val insetDrawable = InsetDrawable(drawable, spacing, 0, spacing, 0)

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(insetDrawable)
        demo_list.addItemDecoration(dividerItemDecoration)
    }

    private class DemoListAdapter : RecyclerView.Adapter<DemoListAdapter.ViewHolder>() {
        private val onClickListener = View.OnClickListener { view ->
            val demo = view.tag as Demo
            val intent = Intent(view.context, demo.demoClass.java)
            intent.putExtra(DemoActivity.DEMO_ID, demo.id)
            view.context.startActivity(intent)
        }

        override fun getItemCount(): Int = DEMOS.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.demo_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val demo = DEMOS[position]
            holder.textView.text = demo.title
            with(holder.itemView) {
                tag = demo
                setOnClickListener(onClickListener)
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.id_text
        }
    }
}

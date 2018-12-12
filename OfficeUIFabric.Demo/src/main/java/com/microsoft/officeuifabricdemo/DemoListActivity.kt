package com.microsoft.officeuifabricdemo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_demo_list.*
import kotlinx.android.synthetic.main.demo_list.*
import kotlinx.android.synthetic.main.demo_list_content.view.*

/**
 * An activity representing a list of Demos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [DemoActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class DemoListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        setupRecyclerView(demo_list)

        Initializer.init(application)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = DemoRecyclerViewAdapter(this)
    }

    // DemoRecyclerViewAdapter

    private class DemoRecyclerViewAdapter(val parentActivity: DemoListActivity) :
        RecyclerView.Adapter<DemoRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val demo = v.tag as Demo
                val intent = Intent(v.context, DemoActivity::class.java)
                intent.putExtra(DemoFragment.DEMO_ID, demo.id)
                v.context.startActivity(intent)
            }
        }

        override fun getItemCount() = DEMOS.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.demo_list_content, parent, false)
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

        // ViewHolder

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.id_text
        }
    }
}

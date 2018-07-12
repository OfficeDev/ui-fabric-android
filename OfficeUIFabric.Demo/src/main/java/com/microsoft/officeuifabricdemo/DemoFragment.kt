package com.microsoft.officeuifabricdemo

import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_demo_detail.*
import java.util.*

open class DemoFragment : Fragment() {
    companion object {
        const val DEMO_ID = "demo_id"
    }

    private var demo: Demo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(DEMO_ID)) {
                val demoID = it.getSerializable(DEMO_ID) as UUID
                demo = DEMOS.find { it.id == demoID }
                activity?.toolbar_layout?.title = demo?.title
            }
        }
    }
}

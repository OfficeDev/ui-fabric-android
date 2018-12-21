/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.support.design.widget.Snackbar
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.PersonaListAdapter
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import com.microsoft.officeuifabricdemo.util.createPersonaList
import kotlinx.android.synthetic.main.activity_demo_detail.*
import kotlinx.android.synthetic.main.activity_persona_list_view.*

class PersonaListViewActivity : DemoActivity() {
    override val contentLayoutId: Int
        get() = R.layout.activity_persona_list_view
    override val contentNeedsScrollableContainer: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        persona_list_view_example.personas = createPersonaList(this)
        persona_list_view_example.onItemClicked = object : PersonaListAdapter.Callback {
            override fun onItemClicked(persona: IPersona) {
                Snackbar.make(
                    root_view,
                    "You clicked on the cell for ${persona.name}, ${persona.subtitle}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.PersonaListAdapter
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import com.microsoft.officeuifabricdemo.util.createPersonaList
import kotlinx.android.synthetic.main.fragment_persona_list_view.*

class PersonaListViewFragment : DemoFragment() {
    override fun needsScrollableContainer(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_persona_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        persona_list_view_example.personas = createPersonaList(context)
        persona_list_view_example.onItemClicked = object : PersonaListAdapter.Callback {
            override fun onItemClicked(persona: IPersona) {
                val snackbar = Snackbar.make(
                    view,
                    "You clicked on the cell for ${persona.name}, ${persona.subtitle}",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
            }
        }
    }
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.microsoft.officeuifabric.persona.PersonaChipView
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_persona_chip.*

class PersonaChipFragment : DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_persona_chip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context ?: return
        createDisabledPersonaChip(context)
        persona_chip_example_basic.listener = setOnClickExample(view)
        persona_chip_example_no_icon.listener = setOnClickExample(view)
        persona_chip_example_error.listener = setOnClickExample(view)
        persona_chip_example_error.hasError = true
    }

    private fun createDisabledPersonaChip(context: Context) {
        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.text = getString(R.string.persona_chip_example_disabled)
        TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_UIFabric_Body2)
        textView.setTextColor(ContextCompat.getColor(context, R.color.uifabric_black))
        persona_chip_layout.addView(textView)

        val personaChipView = PersonaChipView(context)
        personaChipView.isEnabled = false
        personaChipView.name = resources.getString(R.string.persona_name_kat_larsson)
        personaChipView.email = resources.getString(R.string.persona_email_kat_larsson)
        personaChipView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        persona_chip_layout.addView(personaChipView)
    }

    private fun setOnClickExample(view: View): PersonaChipView.Listener {
        return object : PersonaChipView.Listener {
            override fun onSelected(selected: Boolean) {
                // no op
            }

            override fun onClicked() {
                val snackbar = Snackbar.make(
                    view,
                    getString(R.string.persona_chip_example_click),
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
            }
        }
    }
}
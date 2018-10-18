/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.microsoft.officeuifabric.persona.AvatarSize
import com.microsoft.officeuifabric.persona.PersonaView
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_persona.*

class PersonaFragment: DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_persona, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add Persona programmatically
        createNewPersonaFromCode()
    }

    private fun createNewPersonaFromCode() {
        val context = context ?: return
        val personaView = PersonaView(context)
        personaView.avatarSize = AvatarSize.SMALL
        personaView.name = resources.getString(R.string.persona_name_mauricio_august)
        personaView.email = resources.getString(R.string.persona_email_mauricio_august)
        personaView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        persona_layout.addView(personaView)
    }
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.microsoft.officeuifabric.persona.*
import java.util.*

/**
 * This adapter controls data binding and ViewHolders for [PersonaListView].
 */
class PersonaListAdapter(private val context: Context) : RecyclerView.Adapter<PersonaListAdapter.ViewHolder>() {
    /**
     * [Callback] for when a list item is clicked
     */
    var clickCallback: Callback? = null
    /**
     * Collection of [Persona] objects that hold data to create the [PersonaView]s
     */
    var personaList = ArrayList<IPersona>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = PersonaView(context)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        view.avatarSize = AvatarSize.LARGE
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in 0 until personaList.size)
            holder.setPersona(personaList[position])
        else
            return
    }

    override fun getItemCount() = personaList.size

    private fun onItemClicked(persona: IPersona) {
        clickCallback?.onItemClicked(persona)
    }

    inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener {
        private val personaView: PersonaView
        private lateinit var persona: IPersona

        constructor(view: PersonaView) : super(view) {
            personaView = view
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClicked(persona)
        }

        fun setPersona(persona: IPersona) {
            this.persona = persona
            personaView.setPersona(persona)
        }
    }

    interface Callback {
        fun onItemClicked(persona: IPersona)
    }
}

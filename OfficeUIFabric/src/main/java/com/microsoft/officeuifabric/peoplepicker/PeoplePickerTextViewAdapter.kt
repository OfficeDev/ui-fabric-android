/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.persona.*
import java.util.*

/**
 * Provides views for the DropDownListView that shows the [personas].
 * The DropDown used by [MultiAutoCompleteTextView] (via [TokenCompleteTextView]) uses a ListView
 * so we use an [ArrayAdapter] to generate the views instead of a [RecyclerView.Adapter].
 */
class PeoplePickerTextViewAdapter : ArrayAdapter<IPersona>, Filterable {
    /**
     * Collection of [Persona] objects that hold data to create the [PersonaView]s
     */
    var personas: ArrayList<IPersona> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var filter: Filter
    private var viewParent: ListView? = null
        set(value) {
            if (value == null || field == value)
                return
            field = value
            value.divider = createDivider()
        }

    constructor(context: Context, resource: Int, objects: List<IPersona>, filter: Filter) : super(context, resource, objects) {
        personas.addAll(objects)
        this.filter = filter
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // TODO: Get the view type here to implement either PersonaListItem or SearchDirectoryItem
        val view = convertView as? PersonaView ?: PersonaView(context)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        view.avatarSize = AvatarSize.LARGE
        view.setPersona(personas[position])
        viewParent = parent as? ListView
        return view
    }

    override fun getItem(position: Int): IPersona? = personas[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = personas.size

    override fun getFilter(): Filter = filter

    private fun createDivider(): InsetDrawable {
        val spacing = PersonaView.getSpacing(context, AvatarSize.LARGE)
        val insetDrawable = InsetDrawable(
            ContextCompat.getDrawable(context, R.drawable.ms_row_divider),
            spacing.insetLeft,
            0,
            spacing.cellPadding,
            0
        )
        return insetDrawable
    }
}
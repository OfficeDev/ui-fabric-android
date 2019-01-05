/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.persona.*
import kotlinx.android.synthetic.main.people_picker_search_directory.view.*
import java.util.*

/**
 * Provides views for the DropDownListView that shows the [personas].
 * The DropDown used by [MultiAutoCompleteTextView] (via [TokenCompleteTextView]) uses a ListView
 * so we use an [ArrayAdapter] to generate the views instead of a [RecyclerView.Adapter].
 */
internal class PeoplePickerTextViewAdapter : ArrayAdapter<IPersona>, Filterable {
    /**
     * Collection of [Persona] objects that hold data to create the [PersonaView]s
     */
    var personas: ArrayList<IPersona> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var showSearchDirectoryButton: Boolean = false
    var isSearchingDirectory: Boolean = false
        set(value) {
            field = value
            searchDirectoryView?.isEnabled = !value
            updateSearchDirectoryText()
        }

    var onSearchDirectoryButtonClicked: View.OnClickListener? = null
        set(value) {
            field = value
            searchDirectoryView?.setOnClickListener(value)
        }

    private var filter: Filter
    private var listView: ListView? = null
        set(value) {
            if (value == null || field == value)
                return
            field = value
            value.divider = createDivider()
            // This hides the last divider
            value.overscrollFooter = ContextCompat.getDrawable(context, android.R.color.transparent)
        }
    private var searchDirectoryView: View? = null
        set(value) {
            field = value
            searchDirectoryTextView = value?.people_picker_search_directory_text
            updateSearchDirectoryText()
        }

    private var searchDirectoryTextView: TextView? = null
        set(value) {
            if (field == value)
                return
            field = value
            updateSearchDirectoryText()
        }

    constructor(context: Context, resource: Int, objects: List<IPersona>, filter: Filter) : super(context, resource, objects) {
        personas.addAll(objects)
        this.filter = filter
    }

    override fun getItem(position: Int): IPersona? = if (isSearchDirectoryButtonPosition(position)) null else personas[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = personas.size + if (showSearchDirectoryButton) 1 else 0

    override fun getFilter(): Filter = filter

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (isSearchDirectoryButtonPosition(position))
            getSearchDirectoryView(parent)
        else
            getPersonaView(convertView, position, parent)
    }

    private fun isSearchDirectoryButtonPosition(position: Int): Boolean = showSearchDirectoryButton && position == personas.size

    private fun getPersonaView(convertView: View?, position: Int, parent: ViewGroup?): View {
        val view = convertView as? PersonaView ?: PersonaView(context)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        view.avatarSize = AvatarSize.LARGE
        view.setPersona(personas[position])
        listView = parent as? ListView
        return view
    }

    private fun getSearchDirectoryView(parent: ViewGroup?): View {
        var view = searchDirectoryView
        return if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.people_picker_search_directory, parent, false)
            searchDirectoryView = view
            view
        } else
            view
    }

    private fun updateSearchDirectoryText() {
        if (isSearchingDirectory)
            searchDirectoryTextView?.setText(R.string.people_picker_search_progress)
        else
            searchDirectoryTextView?.setText(R.string.people_picker_search_directory)
    }

    private fun createDivider(): InsetDrawable {
        val spacing = PersonaView.getSpacing(context, AvatarSize.LARGE)
        return InsetDrawable(
            ContextCompat.getDrawable(context, R.drawable.ms_row_divider),
            spacing.insetLeft,
            0,
            spacing.cellPadding,
            0
        )
    }
}
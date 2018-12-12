/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.PersonaView
import com.microsoft.officeuifabric.view.TemplateView
import com.tokenautocomplete.TokenCompleteTextView
import kotlinx.android.synthetic.main.view_people_picker.view.*

typealias PeoplePickerPersonaChipClickStyle = TokenCompleteTextView.TokenClickStyle

/**
 * [PeoplePickerView] is a customizable [TemplateView] comprised of a label and [PeoplePickerTextView].
 *
 * TODO Future work:
 * - Handle cases where [pickedPersonas] is modified programmatically.
 * - Use Outlook's adjustDropDownPositionAndSize and getMaxAvailableHeight methods in ContactPickerView
 * to resize the drop down to the available space in the view. This will make the list less jumpy,
 * which is most noticeable when it appears above the [PeoplePickerTextView].
 */
class PeoplePickerView : TemplateView {
    /**
     * Label describing the [PeoplePickerTextView] field.
     */
    var label: String = ""
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * [valueHint] is important for accessibility but will not be displayed.
     */
    var valueHint: String = context.getString(R.string.people_picker_default_hint)
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * The list of personas that are available to be filtered and supplied to the dropdown
     * containing suggestions for the [PeoplePickerTextView].
     */
    var availablePersonas: ArrayList<IPersona>? = null
        set(value) {
            field = value
            peoplePickerTextViewAdapter = PeoplePickerTextViewAdapter(
                context,
                PersonaView.layoutId,
                value ?: ArrayList(),
                PersonaFilter(this)
            )
            updateViews()
        }
    /**
     * Tracks personas that have been added as PersonaChips to the [PeoplePickerTextView].
     */
    var pickedPersonas = ArrayList<IPersona>()
        set(value) {
            field = value
            updatePersonaChips()
        }
    /**
     * The number of characters required to be entered before showing the dropdown of filtered suggestions.
     */
    var characterThreshold: Int = 1
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * This will automatically remove persona chips from your text field, but you will need to do extra
     * filtering work to ensure duplicates don't end up in your dropdown list.
     */
    var allowDuplicatePersonaChips: Boolean = false
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * Defines what happens when a user clicks on a persona chip.
     */
    var personaChipClickStyle: PeoplePickerPersonaChipClickStyle = PeoplePickerPersonaChipClickStyle.Select
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * Collapse the [PeoplePickerTextView] to a single line when it loses focus.
     */
    var allowCollapse: Boolean = true
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * Provides callbacks for when a persona chip is added or removed from the [PeoplePickerTextView].
     */
    var pickedPersonasChangeListener: PickedPersonasChangeListener? = null
    /**
     * Callbacks for customized filtering. Supports async.
     */
    var personaSuggestionsListener: PersonaSuggestionsListener? = null

    private var peoplePickerTextViewAdapter: PeoplePickerTextViewAdapter? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.PeoplePickerView)
        label = styledAttrs.getString(R.styleable.PeoplePickerView_label) ?: ""
        valueHint = styledAttrs.getString(R.styleable.PeoplePickerView_valueHint) ?: ""
        val personaChipClickStyleOrdinal = styledAttrs.getInt(R.styleable.PeoplePickerView_personaChipClickStyle, PeoplePickerPersonaChipClickStyle.Select.ordinal)
        personaChipClickStyle = PeoplePickerPersonaChipClickStyle.values()[personaChipClickStyleOrdinal]

        styledAttrs.recycle()
    }

    // Template

    override val templateId: Int = R.layout.view_people_picker
    private var labelTextView: TextView? = null
    private var peoplePickerTextView: PeoplePickerTextView? = null

    override fun onTemplateLoaded() {
        labelTextView = people_picker_label
        peoplePickerTextView = people_picker_text_view

        // Fixed properties for TokenCompleteTextView.
        peoplePickerTextView?.apply {
            dropDownWidth = ViewGroup.LayoutParams.MATCH_PARENT
            allowCollapse(true)
            isLongClickable = true
            setTokenListener(TokenListener(this@PeoplePickerView))
            performBestGuess(false)
        }

        updatePersonaChips()
        updateViews()

        super.onTemplateLoaded()
    }

    private fun updateViews() {
        labelTextView?.text = label
        peoplePickerTextView?.apply {
            allowCollapse(allowCollapse)
            allowDuplicates(allowDuplicatePersonaChips)
            threshold = characterThreshold
            setAdapter(peoplePickerTextViewAdapter)
            personaChipClickStyle = this@PeoplePickerView.personaChipClickStyle
            hint = valueHint
        }
    }

    private fun updatePersonaChips() {
        peoplePickerTextView?.removeObjects(peoplePickerTextView?.objects)
        for (persona in pickedPersonas)
            peoplePickerTextView?.addObject(persona)
    }

    // Filter

    private class PersonaFilter(val view: PeoplePickerView) : Filter() {
        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            if (view.personaSuggestionsListener != null) {
                // Show the previous results until we get new ones.
                // This code allows us to keep dropdown open and not hidden on each key stroke.
                val suggestedPersonas = view.peoplePickerTextViewAdapter?.personas
                return FilterResults().apply {
                    values = suggestedPersonas
                    count = suggestedPersonas?.size ?: 0
                }
            }

            val availablePersonas = view.availablePersonas
            val suggestedPersonas: ArrayList<IPersona>
            if (availablePersonas == null)
                suggestedPersonas = ArrayList()
            else
                if (constraint != null) {
                    val searchTerm = constraint.toString().toLowerCase()
                    val filteredResults = availablePersonas.filter {
                        it.name.toLowerCase().contains(searchTerm) && !view.pickedPersonas.contains(it)
                    }
                    suggestedPersonas = ArrayList(filteredResults)
                } else
                    suggestedPersonas = availablePersonas
            return FilterResults().apply {
                values = suggestedPersonas
                count = suggestedPersonas.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
            val listener = view.personaSuggestionsListener
            if (listener != null) {
                listener.onGetSuggestedPersonas(constraint, view.availablePersonas, view.pickedPersonas) {
                    view.post {
                        view.peoplePickerTextViewAdapter?.personas = it
                    }
                }
            }
            else {
                view.peoplePickerTextViewAdapter?.personas = results.values as ArrayList<IPersona>
            }
        }
    }

    // Listeners

    /**
     * Callbacks for when a persona is added or removed from the [PeoplePickerTextView]
     */
    interface PickedPersonasChangeListener {
        fun onPersonaAdded(persona: IPersona)
        fun onPersonaRemoved(persona: IPersona)
    }

    /**
     * Callbacks for updating suggestions in the dropdown list of personas.
     */
    interface PersonaSuggestionsListener {
        fun onGetSuggestedPersonas(
            searchConstraint: CharSequence?,
            availablePersonas: ArrayList<IPersona>?,
            pickedPersonas: ArrayList<IPersona>,
            completion: (suggestedPersonas: ArrayList<IPersona>) -> Unit
        )
    }

    private class TokenListener(val view: PeoplePickerView) : TokenCompleteTextView.TokenListener<IPersona> {
        override fun onTokenAdded(token: IPersona) {
            view.pickedPersonas.add(token)
            view.pickedPersonasChangeListener?.onPersonaAdded(token)
        }

        override fun onTokenRemoved(token: IPersona) {
            view.pickedPersonas.remove(token)
            view.pickedPersonasChangeListener?.onPersonaRemoved(token)
        }
    }
}
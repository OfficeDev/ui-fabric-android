/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.method.MovementMethod
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.Persona
import com.microsoft.officeuifabric.persona.PersonaChipView
import com.microsoft.officeuifabric.persona.setPersona
import com.tokenautocomplete.CountSpan
import com.tokenautocomplete.TokenCompleteTextView

/**
 * [PeoplePickerTextView] provides all of the functionality needed to add [PersonaChipView]s as [tokens]
 * into an [EditText] view.
 *
 * Functionality we add in addition to [TokenCompleteTextView]'s functionality includes:
 * - Hiding the cursor when a token is selected
 * - Styling the [CountSpan]
 *
 * TODO Known issues:
 * - Using backspace to delete a selected token does not work if other text is entered in the input;
 * [TokenCompleteTextView] overrides [onCreateInputConnection] which blocks our ability to control this functionality.
 * - If you long press and select all the tokens, then press "Cut" to put them on the clipboard, the fragment crashes with an IndexOutOfBoundsException;
 * This same bug happens in Outlook.
 * - In [performCollapse] when the first token takes up the whole width of the text view, the CountSpan is in danger of being cut off.
 * Instead of shortening the token (PersonaChip in our implementation), [TokenCompleteTextView] removes it and adds it to the count in the CountSpan.
 * Shortening the PersonaChip would be more ideal.
 * -setTokenLimit is not working as intended. Need to debug this and add the public property back into the api.
 *
 * TODO Future work:
 * - Improve accessibility with something like the [TokenCompleteTextViewTouchHelper] class.
 * - Support drag and drop.
 * - Limit what appears in the long click context menu.
 * - Baseline align chips with other text
 */
internal class PeoplePickerTextView : TokenCompleteTextView<IPersona> {
    companion object {
        // Removes constraints to the input field
        private val noFilters = arrayOfNulls<InputFilter>(0)
        // Constrains changes that can be made to the input field to none
        private val blockInputFilters = arrayOf(InputFilter { _, _, _, _, _, _ -> "" })
    }

    /**
     * Defines what happens when a user clicks on a [personaChip].
     */
    var personaChipClickStyle: PeoplePickerPersonaChipClickStyle = PeoplePickerPersonaChipClickStyle.Select
        set(value) {
            field = value
            setTokenClickStyle(value)
        }

    private val countSpan: CountSpan?
        get() = text.getSpans(0, text.length, CountSpan::class.java).firstOrNull()
    private var blockedMovementMethod: MovementMethod? = null

    // @JvmOverloads does not work in this scenario due to parameter defaults
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun getViewForObject(`object`: IPersona): View {
        val view = PersonaChipView(context)
        view.showCloseIconWhenSelected = personaChipClickStyle == PeoplePickerPersonaChipClickStyle.Select
        view.listener = object : PersonaChipView.Listener {
            override fun onClicked() {
                // no op
            }

            override fun onSelected(selected: Boolean) {
                if (selected) {
                    blockInput()
                } else {
                    unblockInput()
                }
            }
        }
        view.setPersona(`object`)
        return view
    }

    override fun defaultObject(completionText: String): IPersona? {
        if (completionText.isEmpty() || !isEmailValid(completionText))
            return null

        val entry = Persona()
        entry.email = completionText
        return entry
    }

    override fun performCollapse(hasFocus: Boolean) {
        super.performCollapse(hasFocus)

        // Replace the CountSpan with a custom styled span
        val countSpan = countSpan
        if (countSpan == null) {
            removeReplacementCountSpan()
        } else {
            val countSpanStart = text.indexOfLast { it == '+' }
            val countSpanEnd = text.length
            text.removeSpan(countSpan)
            val replacementCountSpan = SpannableString(countSpan.text)
            replacementCountSpan.setSpan(
                TextAppearanceSpan(context, R.style.TextAppearance_UIFabric_PeoplePickerCountSpan),
                0,
                replacementCountSpan.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text.replace(countSpanStart, countSpanEnd, replacementCountSpan)
        }
    }

    override fun onFocusChanged(hasFocus: Boolean, direction: Int, previous: Rect?) {
        super.onFocusChanged(hasFocus, direction, previous)

        val inputmethodManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (hasFocus) {
            // Soft keyboard does not always show up without this
            Handler().post {
                inputmethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            inputmethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        // super.onSelectionChanged is buggy, but we still need the accessibility event from the super super call.
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        unblockInput()
    }

    internal fun removeObjects(personas: List<IPersona>?) {
        if (personas == null)
            return

        var i = 0
        while (i < personas.size) {
            removeObject(personas[i])
            i++
        }

        removeReplacementCountSpan()
    }

    private fun removeReplacementCountSpan() {
        val replacementCountSpanStart = text.indexOfFirst { it == '+' }
        if (replacementCountSpanStart > -1)
            text.delete(replacementCountSpanStart, text.length)
    }

    private fun isEmailValid(email: CharSequence): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun blockInput() {
        isCursorVisible = false
        filters = blockInputFilters

        // Prevents other input from being selected when a token is selected
        blockedMovementMethod = movementMethod
        movementMethod = null
    }

    private fun unblockInput() {
        isCursorVisible = true
        filters = noFilters

        // Restores original MovementMethod we blocked during selection
        if (blockedMovementMethod != null)
            movementMethod = blockedMovementMethod
    }
}
/**
 * Copyright © 2019 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.peoplepicker

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
import android.support.v4.widget.ExploreByTouchHelper
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.MovementMethod
import android.text.style.TextAppearanceSpan
import android.text.util.Rfc822Token
import android.text.util.Rfc822Tokenizer
import android.util.AttributeSet
import android.util.Patterns
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethodManager
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.peoplepicker.PeoplePickerView.PersonaChipClickListener
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.PersonaChipView
import com.microsoft.officeuifabric.persona.setPersona
import com.tokenautocomplete.CountSpan
import com.tokenautocomplete.TokenCompleteTextView

/**
 * [PeoplePickerTextView] provides all of the functionality needed to add [PersonaChipView]s as [tokens]
 * into an [EditText] view.
 *
 * Functionality we add in addition to [TokenCompleteTextView]'s functionality includes:
 * - Click api for SelectDeselect persona chips
 * - Drag and drop option
 * - Accessibility
 * - Hiding the cursor when a persona chip is selected
 * - Styling the [CountSpan]
 *
 * TODO Known issues:
 * - Using backspace to delete a selected token does not work if other text is entered in the input;
 * [TokenCompleteTextView] overrides [onCreateInputConnection] which blocks our ability to control this functionality.
 * - [CountSpan] text should be centered
 */
internal class PeoplePickerTextView : TokenCompleteTextView<IPersona> {
    companion object {
        // Max number of personas the screen reader will announce on focus.
        private const val MAX_PERSONAS_TO_READ = 3
        private const val BACKGROUND_DRAG_ALPHA = 75
        // Removes constraints to the input field
        private val noFilters = arrayOfNulls<InputFilter>(0)
        // Constrains changes that can be made to the input field to none
        private val blockInputFilters = arrayOf(InputFilter { _, _, _, _, _, _ -> "" })
    }

    /**
     * Defines what happens when a user clicks on a persona chip.
     */
    var personaChipClickStyle: PeoplePickerPersonaChipClickStyle = PeoplePickerPersonaChipClickStyle.Select
        set(value) {
            field = value
            setTokenClickStyle(value)
        }
    /**
     * Flag for enabling Drag and Drop persona chips.
     */
    var allowPersonaChipDragAndDrop: Boolean = false
    /**
     * Limits the total number of persona chips that can be added to the field.
     */
    var personaChipLimit: Int = -1
        set(value) {
            field = value
            setTokenLimit(value)
        }
    /**
     * Store the hint so that we can control when it is announced for accessibility
     */
    var valueHint: CharSequence = ""
        set(value) {
            field = value
            hint = value
        }

    /**
     * When a persona chip with a [PeoplePickerPersonaChipClickStyle] of SelectDeselect is selected,
     * the next touch will fire [PersonaChipClickListener.onClick].
     */
    var personaChipClickListener: PeoplePickerView.PersonaChipClickListener? = null
    lateinit var onCreatePersona: (name: String, email: String) -> IPersona

    val countSpanStart: Int
        get() = text.indexOfFirst { it == '+' }
    private val countSpanEnd: Int
        get() = text.length

    private val accessibilityTouchHelper = AccessibilityTouchHelper(this)
    private var blockedMovementMethod: MovementMethod? = null
    private var gestureDetector: GestureDetector
    // Keep track of persona selection for accessibility events
    private var selectedPersona: IPersona? = null
        set(value) {
            field = value
            if (value != null)
                blockInput()
            else
                unblockInput()
        }
    private var shouldAnnouncePersonaAddition: Boolean = false
    private var shouldAnnouncePersonaRemoval: Boolean = true
    private var searchConstraint: CharSequence = ""

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        ViewCompat.setAccessibilityDelegate(this, accessibilityTouchHelper)
        super.setTokenListener(TokenListener(this))
        gestureDetector = GestureDetector(context, SimpleGestureListener())
        setLineSpacing(resources.getDimension(R.dimen.uifabric_people_picker_persona_chip_vertical_spacing), 1f)
    }

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
                if (selected)
                    selectedPersona = `object`
                else
                    selectedPersona = null
            }
        }
        view.setPersona(`object`)
        return view
    }

    override fun defaultObject(completionText: String): IPersona? {
        if (completionText.isEmpty() || !isEmailValid(completionText))
            return null

        return onCreatePersona("", completionText)
    }

    override fun buildSpanForObject(obj: IPersona?): TokenImageSpan? {
        if (obj == null)
            return null

        // This ensures that persona spans will be short enough to leave room for the count span.
        val countSpanWidth = resources.getDimension(R.dimen.uifabric_people_picker_count_span_width).toInt()
        return TokenImageSpan(getViewForObject(obj), obj, maxTextWidth().toInt() - countSpanWidth)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // To ensure persona chips are correctly laid out when orientation changes, redraw them by re-adding them.
        val personas = objects
        removeObjects(personas)
        addObjects(personas)
    }

    override fun performCollapse(hasFocus: Boolean) {
        if (getOriginalCountSpan() == null)
            removeCountSpanText()

        super.performCollapse(hasFocus)

        // Remove viewPadding for single line to fix jittery virtual view bounds in ExploreByTouch.
        if (!hasFocus())
            setPadding(0, 0, 0, 0)
        else
            setPadding(0, resources.getDimension(R.dimen.uifabric_people_picker_text_view_padding).toInt(), 0, resources.getDimension(R.dimen.uifabric_people_picker_text_view_padding).toInt())

        updateCountSpanStyle()
    }

    override fun onFocusChanged(hasFocus: Boolean, direction: Int, previous: Rect?) {
        super.onFocusChanged(hasFocus, direction, previous)

        // Soft keyboard does not always show up when the view first loads without this
        if (hasFocus) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            post {
                inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        // super.onSelectionChanged is buggy, but we still need the accessibility event from the super super call.
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED)
        // This fixes buggy cursor position in accessibility mode.
        // Cutting spans to the clipboard is not functional so this also prevents that operation from being an option.
        setSelection(text.length)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        selectedPersona = null

        if (lengthAfter > lengthBefore || lengthAfter < lengthBefore && !text.isNullOrEmpty())
            setupSearchConstraint(text)
    }

    override fun replaceText(text: CharSequence?) {
        // Enforce personaChipLimit. TokenCompleteTextView enforces the limit for other scenarios.
        if (personaChipLimit != -1 && objects.size == personaChipLimit)
            return

        shouldAnnouncePersonaAddition = true
        super.replaceText(text)
    }

    override fun canDeleteSelection(beforeLength: Int): Boolean {
        // This method is called from keyboard events so any token removed would be coming from the user.
        shouldAnnouncePersonaRemoval = true
        return super.canDeleteSelection(beforeLength)
    }

    override fun removeObject(`object`: IPersona?) {
        shouldAnnouncePersonaRemoval = false
        super.removeObject(`object`)
    }

    override fun showDropDown() {
        dropDownHeight = getMaxAvailableHeight()
        super.showDropDown()
    }

    internal fun addObjects(personas: List<IPersona>?) {
        personas?.forEach { addObject(it) }
    }

    internal fun removeObjects(personas: List<IPersona>?) {
        if (personas == null)
            return

        personas.forEach { removeObject(it) }
        removeCountSpanText()
    }

    /**
     * Adapted from Android's PopupWindow.
     */
    private fun getMaxAvailableHeight(): Int {
        val displayFrame = Rect()
        getWindowVisibleDisplayFrame(displayFrame)

        val anchorLocationOnScreen = IntArray(2)
        getLocationOnScreen(anchorLocationOnScreen)

        val anchorTop = anchorLocationOnScreen[1]
        val distanceToBottom = displayFrame.bottom - (anchorTop + height)
        val distanceToTop = anchorTop - displayFrame.top
        var maxAvailableHeight = Math.max(distanceToBottom, distanceToTop)

        if (dropDownBackground != null) {
            val backgroundPadding = Rect()
            dropDownBackground.getPadding(backgroundPadding)
            maxAvailableHeight -= backgroundPadding.top + backgroundPadding.bottom
        }

        return maxAvailableHeight
    }

    private fun setupSearchConstraint(text: CharSequence?) {
        accessibilityTouchHelper.invalidateRoot()
        val personaSpanEnd = text?.indexOfLast { it == ',' }?.plus(1) ?: -1
        searchConstraint = when {
            // Ignore the count span
            countSpanStart != -1 -> ""
            // If we have personas, we'll also have comma tokenizers to remove from the text
            personaSpanEnd > 0 -> text?.removeRange(text.indexOfFirst { it == ',' }, personaSpanEnd)?.trim() ?: ""
            // Any other characters will be used as the search constraint to perform filtering.
            else -> text ?: ""
        }
        // This keeps the entered text accessibility focused as the user types, which makes the suggested personas list the next focusable view.
        accessibilityTouchHelper.sendEventForVirtualView(objects.size, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
    }

    private fun updateCountSpanStyle() {
        val originalCountSpan = getOriginalCountSpan() ?: return

        text.removeSpan(originalCountSpan)
        val replacementCountSpan = SpannableString(originalCountSpan.text)
        replacementCountSpan.setSpan(
            TextAppearanceSpan(context, R.style.TextAppearance_UIFabric_PeoplePickerCountSpan),
            0,
            replacementCountSpan.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.replace(countSpanStart, countSpanEnd, replacementCountSpan)
    }

    private fun removeCountSpanText() {
        val countSpanStart = countSpanStart
        if (countSpanStart > -1)
            text.delete(countSpanStart, countSpanEnd)
    }

    private fun isEmailValid(email: CharSequence): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun blockInput() {
        isCursorVisible = false
        filters = blockInputFilters

        // Prevents other input from being selected when a persona chip is selected
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

    private fun getPersonaSpans(start: Int = 0, end: Int = text.length): Array<TokenCompleteTextView<IPersona>.TokenImageSpan> =
        text.getSpans(start, end, TokenImageSpan::class.java) as Array<TokenCompleteTextView<IPersona>.TokenImageSpan>

    private fun getOriginalCountSpan(): CountSpan? =
        text.getSpans(0, text.length, CountSpan::class.java).firstOrNull()

    private fun getSpanForPersona(persona: Any): TokenImageSpan? =
        getPersonaSpans().firstOrNull { it.token === persona }

    // Token listener

    private var tokenListener: TokenCompleteTextView.TokenListener<IPersona>? = null

    override fun setTokenListener(l: TokenCompleteTextView.TokenListener<IPersona>?) {
        tokenListener = l
    }

    private class TokenListener(val view: PeoplePickerTextView) : TokenCompleteTextView.TokenListener<IPersona> {
        override fun onTokenAdded(token: IPersona) {
            view.tokenListener?.onTokenAdded(token)
            view.announcePersonaAdded(token)
            view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
        }

        override fun onTokenRemoved(token: IPersona) {
            view.tokenListener?.onTokenRemoved(token)
            view.announcePersonaRemoved(token)
        }
    }

    // Drag and drop

    private var isDraggingPersonaChip: Boolean = false
    private var initialTouchedPersonaSpan: TokenImageSpan? = null

    override fun onTouchEvent(event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event)

    override fun onDragEvent(event: DragEvent): Boolean {
        if (!allowPersonaChipDragAndDrop)
            return false

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> return event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)

            DragEvent.ACTION_DRAG_ENTERED -> requestFocus()

            DragEvent.ACTION_DROP -> return addPersonaFromDragEvent(event)

            DragEvent.ACTION_DRAG_ENDED -> {
                if (!event.result && isDraggingPersonaChip)
                    addPersonaFromDragEvent(event)
                isDraggingPersonaChip = false
            }
        }
        return false
    }

    // This declares whether personaChipClickListener could be called
    private fun isPersonaChipClickable(persona: IPersona): Boolean =
        selectedPersona != null &&
        personaChipClickStyle == PeoplePickerPersonaChipClickStyle.SelectDeselect &&
        persona == selectedPersona

    private fun getClipDataForPersona(persona: IPersona): ClipData? {
        val name = persona.name
        val email = persona.email
        val rfcToken = Rfc822Token(name, email, null)
        return ClipData.newPlainText(if (TextUtils.isEmpty(name)) email else name, rfcToken.toString())
    }

    private fun getPersonaForClipData(clipData: ClipData): IPersona? {
        if (!clipData.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipData.itemCount != 1)
            return null

        val clipDataItem = clipData.getItemAt(0) ?: return null

        val data = clipDataItem.text
        if (TextUtils.isEmpty(data))
            return null

        val rfcTokens = Rfc822Tokenizer.tokenize(data)
        if (rfcTokens == null || rfcTokens.isEmpty())
            return null

        val rfcToken = rfcTokens[0]
        return onCreatePersona(rfcToken.name ?: "", rfcToken.address ?: "")
    }

    private fun startPersonaDragAndDrop(persona: IPersona) {
        val clipData = getClipDataForPersona(persona) ?: return

        // Layout a copy of the persona chip to use as the drag shadow
        val personaChipView = getViewForObject(persona)
        personaChipView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        personaChipView.layout(0, 0, personaChipView.measuredWidth, personaChipView.measuredHeight)
        personaChipView.background = ColorDrawable(ContextCompat.getColor(context, R.color.uifabric_people_picker_text_view_drag_background))
        personaChipView.background.alpha = BACKGROUND_DRAG_ALPHA

        // We pass the persona object as LocalState so we can restore it when dropping
        // [startDrag] is deprecated, but the new [startDragAndDrop] requires a higher api than our min
        isDraggingPersonaChip = startDrag(clipData, View.DragShadowBuilder(personaChipView), persona, 0)
        if (isDraggingPersonaChip)
            removeObject(persona)
    }

    private fun getPersonaSpanAt(x: Float, y: Float): TokenImageSpan? {
        if (text.isEmpty())
            return null

        val offset = getOffsetForPosition(x, y)
        if (offset == -1)
            return null

        return getPersonaSpans(offset, offset).firstOrNull()
    }

    private fun addPersonaFromDragEvent(event: DragEvent): Boolean {
        var persona = event.localState as? IPersona

        // If it looks like the drag & drop is not coming from us, try to extract a persona object from the clipData
        if (persona == null && event.clipData != null)
            persona = getPersonaForClipData(event.clipData)

        if (persona == null)
            return false

        addObject(persona)

        return true
    }

    private inner class SimpleGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(event: MotionEvent) {
            val touchedPersonaSpan = getPersonaSpanAt(event.x, event.y) ?: return
            if (allowPersonaChipDragAndDrop && !isDraggingPersonaChip)
                startPersonaDragAndDrop(touchedPersonaSpan.token)
        }

        override fun onDown(event: MotionEvent): Boolean {
            if (!isFocused)
                requestFocus()

            val touchedPersonaSpan = getPersonaSpanAt(event.x, event.y) ?: return true
            if (allowPersonaChipDragAndDrop)
                initialTouchedPersonaSpan = touchedPersonaSpan

            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            val touchedPersonaSpan = getPersonaSpanAt(event.x, event.y) ?: return true
            if (isFocused && initialTouchedPersonaSpan == touchedPersonaSpan) {
                if (isPersonaChipClickable(touchedPersonaSpan.token))
                    personaChipClickListener?.onClick(touchedPersonaSpan.token)
                touchedPersonaSpan.onClick()
            }

            initialTouchedPersonaSpan = null
            return true
        }
    }

    // Accessibility

    private var customAccessibilityTextProvider: PeoplePickerAccessibilityTextProvider? = null
    private val defaultAccessibilityTextProvider = PeoplePickerAccessibilityTextProvider(resources)
    val accessibilityTextProvider: PeoplePickerAccessibilityTextProvider
        get() = customAccessibilityTextProvider ?: defaultAccessibilityTextProvider

    fun setAccessibilityTextProvider(accessibilityTextProvider: PeoplePickerAccessibilityTextProvider?) {
        customAccessibilityTextProvider = accessibilityTextProvider
    }

    override fun dispatchHoverEvent(motionEvent: MotionEvent): Boolean {
        // Accessibility first
        return if (accessibilityTouchHelper.dispatchHoverEvent(motionEvent))
            true
        else
            super.dispatchHoverEvent(motionEvent)
    }

    private fun announcePersonaAdded(persona: IPersona) {
        accessibilityTouchHelper.invalidateRoot()

        val replacedText = if (searchConstraint.isNotEmpty())
            "${resources.getString(R.string.people_picker_accessibility_replaced, searchConstraint)} "
        else
            ""

        // We only want to announce when a persona was added by a user.
        // If text has been replaced in the text editor and a token was added, the user added a token.
        if (shouldAnnouncePersonaAddition) {
            announceForAccessibility("$replacedText ${getAnnouncementText(
                persona,
                R.string.people_picker_accessibility_persona_added
            )}")
        }
    }

    private fun announcePersonaRemoved(persona: IPersona) {
        accessibilityTouchHelper.invalidateRoot()

        // We only want to announce when a persona was removed by a user.
        if (shouldAnnouncePersonaRemoval) {
            announceForAccessibility(getAnnouncementText(
                persona,
                R.string.people_picker_accessibility_persona_removed
            ))
        }
    }

    private fun getAnnouncementText(persona: IPersona, stringResourceId: Int): CharSequence =
        resources.getString(stringResourceId, accessibilityTextProvider.getPersonaDescription(persona))

    private fun positionIsInsidePersonaBounds(x: Float, y: Float, personaSpan: TokenImageSpan?): Boolean =
        getBoundsForPersonaSpan(personaSpan).contains(x.toInt(), y.toInt())

    private fun positionIsInsideSearchConstraintBounds(x: Float, y: Float): Boolean {
        if (searchConstraint.isNotEmpty())
            return getBoundsForSearchConstraint().contains(x.toInt(), y.toInt())
        return false
    }

    private fun getBoundsForSearchConstraint(): Rect {
        val start = text.indexOf(searchConstraint[0])
        val end = text.length
        return calculateBounds(start, end, resources.getDimension(R.dimen.uifabric_people_picker_accessibility_search_constraint_extra_space).toInt())
    }

    private fun getBoundsForPersonaSpan(personaSpan: TokenImageSpan? = null): Rect {
        val start = text.getSpanStart(personaSpan)
        val end = text.getSpanEnd(personaSpan)
        return calculateBounds(start, end)
    }

    private fun calculateBounds(start: Int, end: Int, extraSpaceForLegibility: Int = 0): Rect {
        val line = layout.getLineForOffset(end)
        // Persona spans increase line height. Without them, we need to make the virtual view bound bottom lower.
        val bounds = Rect(
            layout.getPrimaryHorizontal(start).toInt() - extraSpaceForLegibility,
            layout.getLineTop(line),
            layout.getPrimaryHorizontal(end).toInt() + extraSpaceForLegibility,
            if (getPersonaSpans().isEmpty()) bottom else layout.getLineBottom(line)
        )
        bounds.offset(paddingLeft, paddingTop)
        return bounds
    }

    private fun setHint() {
        if (!isFocused)
        // If the edit box is not focused, there is no event that requires a hint.
            hint = ""
        else
            hint = valueHint
    }

    private inner class AccessibilityTouchHelper(host: View) : ExploreByTouchHelper(host) {
        // Host

        val peoplePickerTextViewBounds = Rect(0, 0, width, height)

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            setHint()
            setInfoText(info)
        }

        override fun onPopulateAccessibilityEvent(host: View?, event: AccessibilityEvent?) {
            super.onPopulateAccessibilityEvent(host, event)
            /**
             * The CommaTokenizer is confusing in the screen reader.
             * This overrides announcements that include the CommaTokenizer.
             * We handle cases for replaced text and persona spans added / removed through callbacks.
             */
            if (event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
                event.text.clear()
        }

        private fun setInfoText(info: AccessibilityNodeInfoCompat) {
            val personas = objects
            if (personas == null || personas.isEmpty())
                return

            var infoText = ""
            // Read all of the personas if the list of personas in the field is short
            // Otherwise, read how many personas are in the field
            if (personas.size <= MAX_PERSONAS_TO_READ)
                infoText += personas.map { accessibilityTextProvider.getPersonaDescription(it) }.joinToString { it }
            else
                infoText = accessibilityTextProvider.getPersonaQuantityText(personas as ArrayList<IPersona>)

            info.text = infoText +
                // Also read any entered text in the field
                if (searchConstraint.isNotEmpty())
                    ", $searchConstraint"
                else
                    ""
        }

        // Virtual views

        override fun getVirtualViewAt(x: Float, y: Float): Int {
            if (objects == null || objects.size == 0)
                return ExploreByTouchHelper.INVALID_ID

            val offset = getOffsetForPosition(x, y)
            if (offset != -1) {
                val personaSpan = getPersonaSpans(offset, offset).firstOrNull()
                if (personaSpan != null && positionIsInsidePersonaBounds(x, y, personaSpan) && isFocused)
                    return objects.indexOf(personaSpan.token)
                else if (searchConstraint.isNotEmpty() && positionIsInsideSearchConstraintBounds(x, y))
                    return objects.size
                else if (peoplePickerTextViewBounds.contains(x.toInt(), y.toInt())) {
                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                    return ExploreByTouchHelper.HOST_ID
                }
            }

            return ExploreByTouchHelper.INVALID_ID
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
            virtualViewIds.clear()

            if (objects == null || objects.size == 0 || !isFocused)
                return

            for (i in objects.indices)
                virtualViewIds.add(i)

            if (searchConstraint.isNotEmpty())
                virtualViewIds.add(objects.size)
        }

        override fun onPopulateEventForVirtualView(virtualViewId: Int, event: AccessibilityEvent) {
            if (objects == null || virtualViewId >= objects.size) {
                // The content description is mandatory.
                event.contentDescription = ""
                return
            }

            if (!isFocused) {
                // Only respond to events for persona chips if the edit box is focused.
                // Without this the user still gets haptic feedback when hovering over a persona chip.
                event.recycle()
                event.contentDescription = ""
                return
            }

            if (virtualViewId == objects.size) {
                event.contentDescription = searchConstraint
                return
            }

            val persona = objects[virtualViewId]
            val personaSpan = getSpanForPersona(persona)
            if (personaSpan != null)
                event.contentDescription = accessibilityTextProvider.getPersonaDescription(persona)

            if (event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED || (personaSpan != null && persona == selectedPersona))
                event.contentDescription = String.format(
                    resources.getString(R.string.people_picker_accessibility_selected_persona),
                    event.contentDescription
                ) + getSelectedActionText(personaSpan)
        }

        override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
            if (objects == null || virtualViewId > objects.size) {
                // the content description & the bounds are mandatory.
                node.contentDescription = ""
                node.setBoundsInParent(peoplePickerTextViewBounds)
                return
            }

            if (!isFocused) {
                // Only populate nodes for persona chips if the edit box is focused.
                node.recycle()
                node.contentDescription = ""
                node.setBoundsInParent(peoplePickerTextViewBounds)
                return
            }

            if (virtualViewId == objects.size) {
                if (searchConstraint.isNotEmpty()){
                    node.contentDescription = searchConstraint
                    node.setBoundsInParent(getBoundsForSearchConstraint())
                } else {
                    node.contentDescription = ""
                    node.setBoundsInParent(peoplePickerTextViewBounds)
                }
                return
            }

            val persona = objects[virtualViewId]
            val personaSpan = getSpanForPersona(persona)
            if (personaSpan != null) {
                setPersonaSpanClickAction(personaSpan, node)
                if (node.isAccessibilityFocused)
                    node.contentDescription = accessibilityTextProvider.getPersonaDescription(persona)
                else
                    node.contentDescription = ""
                node.setBoundsInParent(getBoundsForPersonaSpan(personaSpan))
            }
        }

        override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            if (objects == null || virtualViewId >= objects.size)
                return false

            if (AccessibilityNodeInfo.ACTION_CLICK == action) {
                val persona = objects[virtualViewId]
                val personaSpan = getSpanForPersona(persona)
                if (personaSpan != null) {
                    personaSpan.onClick()
                    onPersonaSpanAccessibilityClick(personaSpan)
                    shouldAnnouncePersonaRemoval = true
                    return true
                }
            }

            return false
        }

        private fun onPersonaSpanAccessibilityClick(personaSpan: TokenImageSpan) {
            val persona = personaSpan.token
            val personaSpanIndex = getPersonaSpans().indexOf(personaSpan)
            when (personaChipClickStyle) {
                PeoplePickerPersonaChipClickStyle.Select, PeoplePickerPersonaChipClickStyle.SelectDeselect -> {
                    if (selectedPersona != null && selectedPersona == persona) {
                        invalidateVirtualView(personaSpanIndex)
                        sendEventForVirtualView(personaSpanIndex, AccessibilityEvent.TYPE_VIEW_CLICKED)
                        sendEventForVirtualView(personaSpanIndex, AccessibilityEvent.TYPE_VIEW_SELECTED)
                    } else {
                        if (personaChipClickStyle == PeoplePickerPersonaChipClickStyle.SelectDeselect) {
                            if (personaChipClickListener != null) {
                                personaChipClickListener?.onClick(persona)
                                announceForAccessibility(resources.getString(
                                    R.string.people_picker_accessibility_clicked_persona,
                                    accessibilityTextProvider.getDefaultPersonaDescription(persona)
                                ))
                            } else {
                                announceForAccessibility(resources.getString(
                                    R.string.people_picker_accessibility_deselected_persona,
                                    accessibilityTextProvider.getDefaultPersonaDescription(persona)
                                ))
                            }
                        }
                        sendEventForVirtualView(personaSpanIndex, AccessibilityEvent.TYPE_VIEW_CLICKED)
                        if (personaChipClickStyle == PeoplePickerPersonaChipClickStyle.Select && personaSpanIndex == -1)
                            invalidateRoot()
                    }
                }
                PeoplePickerPersonaChipClickStyle.Delete -> {
                    sendEventForVirtualView(personaSpanIndex, AccessibilityEvent.TYPE_VIEW_CLICKED)
                    sendEventForVirtualView(personaSpanIndex, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
                }
            }
        }

        private fun setPersonaSpanClickAction(personaSpan: TokenImageSpan, node: AccessibilityNodeInfoCompat) {
            if (personaChipClickStyle == PeoplePickerPersonaChipClickStyle.None)
                return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val clickAction = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK,
                    getActionText(personaSpan)
                )
                node.addAction(clickAction)
            } else {
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
        }

        /**
         * Sets text for the custom click action depending on persona chip click style and selection state.
         */
        private fun getActionText(personaSpan: TokenImageSpan): String {
            return if (personaSpan.token == selectedPersona) {
                when (personaChipClickStyle) {
                    PeoplePickerPersonaChipClickStyle.Select ->
                        resources.getString(R.string.people_picker_accessibility_delete_persona)
                    PeoplePickerPersonaChipClickStyle.SelectDeselect ->
                        if (personaChipClickListener != null)
                            resources.getString(R.string.people_picker_accessibility_click_persona)
                        else
                            resources.getString(R.string.people_picker_accessibility_deselect_persona)
                    else -> ""
                }
            } else {
                when (personaChipClickStyle) {
                    PeoplePickerPersonaChipClickStyle.Select, PeoplePickerPersonaChipClickStyle.SelectDeselect ->
                        resources.getString(R.string.people_picker_accessibility_select_persona)
                    PeoplePickerPersonaChipClickStyle.Delete ->
                        resources.getString(R.string.people_picker_accessibility_delete_persona)
                    else -> ""
                }
            }
        }

        /**
         * Describes the action that will happen when already selected personas are activated.
         * We can't set a second action for the the virtual view, so we describe it after the first event occurs.
         */
        private fun getSelectedActionText(personaSpan: TokenImageSpan?): String {
            if (personaSpan == null || personaSpan.token != selectedPersona)
                return ""

            return when (personaChipClickStyle) {
                PeoplePickerPersonaChipClickStyle.Select ->
                    resources.getString(R.string.people_picker_accessibility_delete_selected_persona)
                PeoplePickerPersonaChipClickStyle.SelectDeselect ->
                    if (personaChipClickListener != null)
                        resources.getString(R.string.people_picker_accessibility_click_selected_persona)
                    else
                        resources.getString(R.string.people_picker_accessibility_deselect_selected_persona)
                else -> ""
            }
        }
    }
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.persona

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.view.TemplateView
import kotlinx.android.synthetic.main.view_persona_chip.view.*

class PersonaChipView : TemplateView {
    companion object {
        const val DISABLED_BACKGROUND_OPACITY = .6f
        const val ENABLED_BACKGROUND_OPACITY = 1.0f
    }

    var name: String = ""
        set(value) {
            field = value
            updateViews()
        }
    var email: String = ""
        set(value) {
            field = value
            updateViews()
        }
    var avatarImageBitmap: Bitmap? = null
        set(value) {
            field = value
            avatarView?.avatarImageBitmap = value
        }
    var avatarImageDrawable: Drawable? = null
        set(value) {
            field = value
            avatarView?.avatarImageDrawable = value
        }
    var avatarImageResourceId: Int? = null
        set(value) {
            field = value
            avatarView?.avatarImageResourceId = value
        }
    var avatarImageUri: Uri? = null
        set(value) {
            field = value
            avatarView?.avatarImageUri = value
        }
    /**
     * Flag for setting the chip's error state
     */
    var hasError: Boolean = false
        set(value) {
            field = value
            updateState()
        }
    /**
     * Determines whether the [closeIcon] is shown in place of the [avatarView]
     * when the [PersonaChipView] is selected.
     */
    var showCloseIconWhenSelected: Boolean = true

    /**
     * When a chip is selected, the next touch will fire the [listener]'s [onClicked] method.
     */
    var listener: Listener? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        if (attrs == null)
            return
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.PersonaChipView)
        name = styledAttrs.getString(R.styleable.PersonaChipView_name) ?: ""
        email = styledAttrs.getString(R.styleable.PersonaChipView_email) ?: ""
        showCloseIconWhenSelected = styledAttrs.getBoolean(R.styleable.PersonaChipView_showCloseIconWhenSelected, true)

        val avatarImageResourceId = styledAttrs.getResourceId(R.styleable.PersonaChipView_avatarImageDrawable, 0)
        if (avatarImageResourceId > 0 && resources.getResourceTypeName(avatarImageResourceId) == "drawable")
            avatarImageDrawable = styledAttrs.getDrawable(R.styleable.PersonaChipView_avatarImageDrawable)

        styledAttrs.recycle()
    }

    // Template

    override val templateId: Int = R.layout.view_persona_chip
    private var avatarView: AvatarView? = null
    private var closeIcon: ImageView? = null
    private var textView: TextView? = null

    override fun onTemplateLoaded() {
        super.onTemplateLoaded()
        textView = persona_chip_text
        avatarView = persona_chip_avatar
        closeIcon = persona_chip_close
        updateCloseIconVisibility(false)
        updateState()
        updateViews()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateState()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        updateState()
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateState()
        listener?.onSelected(selected)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled)
            return false

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                isPressed = false
                isSelected = !isSelected
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                return true
            }
            else -> return false
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (isSelected) {
            listener?.onClicked()
        }
        return true
    }

    private fun updateState() {
        when {
            !isEnabled -> setDisabledState()
            isSelected -> setSelectedState()
            isPressed && !hasError -> setPressedState()
            else -> setNormalState()
        }
    }

    private fun setDisabledState() {
        avatarView?.alpha = DISABLED_BACKGROUND_OPACITY
        updateStateStyles(R.drawable.persona_chip_background_normal, R.color.uifabric_persona_chip_disabled_text)
    }

    private fun setNormalState() {
        avatarView?.alpha = ENABLED_BACKGROUND_OPACITY
        if (showCloseIconWhenSelected) {
            updateCloseIconVisibility(false)
        }
        if (hasError) {
            updateStateStyles(R.drawable.persona_chip_background_normal_error, R.color.uifabric_persona_chip_error_text)

        } else {
            updateStateStyles(R.drawable.persona_chip_background_normal, R.color.uifabric_persona_chip_normal_text)
        }
    }

    private fun setPressedState() {
        updateStateStyles(R.drawable.persona_chip_background_pressed, R.color.uifabric_persona_chip_normal_text)
    }

    private fun setSelectedState() {
        if (showCloseIconWhenSelected) {
            updateCloseIconVisibility(true)
        }
        if (hasError) {
            updateStateStyles(R.drawable.persona_chip_background_active_error, R.color.uifabric_persona_chip_active_text)
        } else {
            updateStateStyles(R.drawable.persona_chip_background_active, R.color.uifabric_persona_chip_active_text)
        }
    }

    private fun updateStateStyles(backgroundDrawable: Int, textColor: Int) {
        background = ContextCompat.getDrawable(context, backgroundDrawable)
        textView?.setTextColor(ContextCompat.getColor(context, textColor))
    }

    private fun updateCloseIconVisibility(visible: Boolean) {
        if (visible) {
            closeIcon?.visibility = View.VISIBLE
            avatarView?.visibility = View.GONE
        } else {
            closeIcon?.visibility = View.GONE
            avatarView?.visibility = View.VISIBLE
        }
    }

    private fun updateViews() {
        textView?.text = when {
            !name.isEmpty() -> name
            !email.isEmpty() -> email
            else -> context.getString(R.string.persona_title_placeholder)
        }
        avatarView?.name = name
        avatarView?.email = email
        avatarView?.avatarImageDrawable = avatarImageDrawable
    }

    interface Listener {
        fun onClicked()
        fun onSelected(selected: Boolean)
    }
}

fun PersonaChipView.setPersona(persona: IPersona) {
    name = persona.name
    email = persona.email
    avatarImageBitmap = persona.avatarImageBitmap
    avatarImageDrawable = persona.avatarImageDrawable
    avatarImageResourceId = persona.avatarImageResourceId
    avatarImageUri = persona.avatarImageUri
}
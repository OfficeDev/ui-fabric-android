/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.persona

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.view.TemplateView
import kotlinx.android.synthetic.main.view_persona.view.*

/**
 * [PersonaView] is comprised of an [AvatarView] and three TextViews, all single line by default.
 * [AvatarSize.SMALL], [AvatarSize.LARGE], and [AvatarSize.XXLARGE] are the recommended AvatarSizes to use with [PersonaView].
 * [AvatarSize.SMALL] will only have name text. [AvatarSize.LARGE] should use both name and subtitle texts.
 * [AvatarSize.XXLARGE] should use name, subtitle, and footer texts.
 */
class PersonaView : TemplateView {
    companion object {
        val personaAvatarSizes = arrayOf(AvatarSize.SMALL, AvatarSize.LARGE, AvatarSize.XXLARGE)

        internal val layoutId = R.layout.view_persona

        internal data class Spacing(val cellPadding: Int, val insetLeft: Int)

        internal fun getSpacing(context: Context, avatarSize: AvatarSize): Spacing {
            val avatarDisplaySize = avatarSize.getDisplayValue(context)
            val spacingRight = context.resources.getDimension(R.dimen.uifabric_persona_horizontal_spacing)
            val cellPadding = context.resources.getDimension(R.dimen.uifabric_persona_horizontal_padding).toInt()
            val insetLeft = (avatarDisplaySize + spacingRight + cellPadding).toInt()
            return Spacing(cellPadding, insetLeft)
        }
    }

    /**
     * [AvatarSize] used to set internal [AvatarView]'s layout width and height.
     */
    var avatarSize = AvatarView.defaultAvatarSize
        set(value) {
            if (!personaAvatarSizes.contains(value)) {
                throw UnsupportedOperationException("""
                    AvatarSize $value is not supported in PersonaViews.
                    Please replace with one of the following AvatarSizes: ${personaAvatarSizes.joinToString(", ")}
                """.trimIndent())
            }

            if (field == value)
                return
            field = value
            updateViews()
        }

    /**
     * Text for the top hierarchy of the three TextViews.
     */
    var name = ""
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }

    var email = ""
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * Text for the middle hierarchy of the three TextViews
     */
    var subtitle: String = ""
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    /**
     * Text for the bottom hierarchy of the three TextViews
     */
    var footer: String = ""
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    var avatarImageBitmap: Bitmap? = null
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    var avatarImageDrawable: Drawable? = null
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    var avatarImageResourceId: Int? = null
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }
    var avatarImageUri: Uri? = null
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }

    private var title: String = name
        set(value) {
            if (field == value)
                return
            field = value
            updateViews()
        }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.PersonaView)
        val avatarSizeOrdinal = styledAttrs.getInt(R.styleable.PersonaView_avatarSize, AvatarView.defaultAvatarSize.ordinal)
        avatarSize = AvatarSize.values()[avatarSizeOrdinal]
        name = styledAttrs.getString(R.styleable.PersonaView_name) ?: ""
        email = styledAttrs.getString(R.styleable.PersonaView_email) ?: ""
        subtitle = styledAttrs.getString(R.styleable.PersonaView_subtitle) ?: ""
        footer = styledAttrs.getString(R.styleable.PersonaView_footer) ?: ""

        val avatarImageResourceId = styledAttrs.getResourceId(R.styleable.PersonaView_avatarImageDrawable, 0)
        if (avatarImageResourceId > 0 && resources.getResourceTypeName(avatarImageResourceId) == "drawable")
            avatarImageDrawable = styledAttrs.getDrawable(R.styleable.PersonaView_avatarImageDrawable)

        styledAttrs.recycle()
    }

    // Template

    override val templateId: Int = R.layout.view_persona
    private var avatarView: AvatarView? = null
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var footerView: TextView? = null

    override fun onTemplateLoaded() {
        super.onTemplateLoaded()
        avatarView = persona_avatar_view
        titleView = persona_title
        subtitleView = persona_subtitle
        footerView = persona_footer

        updateViews()
    }

    private fun updateViews() {
        avatarView?.apply {
            name = this@PersonaView.name
            email = this@PersonaView.email
            avatarSize = this@PersonaView.avatarSize
            avatarImageDrawable = this@PersonaView.avatarImageDrawable
            avatarImageBitmap = this@PersonaView.avatarImageBitmap
            avatarImageUri = this@PersonaView.avatarImageUri
        }

        title = when {
            !name.isEmpty() -> name
            !email.isEmpty() -> email
            else -> context.getString(R.string.persona_title_placeholder)
        }

        titleView?.text = title
        subtitleView?.text = subtitle
        footerView?.text = footer

        footerView?.visibility = if (footer != "" && avatarSize != AvatarSize.SMALL) View.VISIBLE else View.GONE
        subtitleView?.visibility = if (subtitle != "" && avatarSize != AvatarSize.SMALL) View.VISIBLE else View.GONE
    }
}

fun PersonaView.setPersona(persona: IPersona) {
    name = persona.name
    email = persona.email
    subtitle = persona.subtitle
    footer = persona.footer
    avatarImageBitmap = persona.avatarImageBitmap
    avatarImageDrawable = persona.avatarImageDrawable
    avatarImageResourceId = persona.avatarImageResourceId
    avatarImageUri = persona.avatarImageUri
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.persona

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.microsoft.officeuifabric.R
import kotlinx.android.synthetic.main.view_persona.view.*

/**
 * [PersonaView] is a custom compound ViewGroup. It is comprised of an [AvatarView] and three TextViews, all single line by default.
 * [AvatarSize.SMALL], [AvatarSize.LARGE], and [AvatarSize.XXLARGE] are the recommended AvatarSizes to use with [PersonaView].
 * [AvatarSize.SMALL] will only have name text. [AvatarSize.LARGE] should use both name and subtitle texts.
 * [AvatarSize.XXLARGE] should use name, subtitle, and footer texts.
 */
class PersonaView : LinearLayout {
    companion object {
        val personaAvatarSizes = arrayOf(AvatarSize.SMALL, AvatarSize.LARGE, AvatarSize.XXLARGE)
    }

    /**
     * Bitmap used for internal [AvatarView].
     */
    var avatarImageBitmap: Bitmap? = null
        set(value) {
            field = value
            persona_avatar_view.setImageBitmap(value)
        }

    /**
     * Drawable used for internal [AvatarView].
     */
    var avatarImageDrawable: Drawable? = null
        set(value) {
            field = value
            persona_avatar_view.setImageDrawable(value)
        }

    /**
     * Resource used for internal [AvatarView].
     */
    var avatarImageResource: Int? = null
        set(value) {
            field = value
            value?.let { persona_avatar_view.setImageResource(it) }
        }

    /**
     * Uri used for internal [AvatarView].
     */
    var avatarImageUri: Uri? = null
        set(value) {
            field = value
            persona_avatar_view.setImageURI(value)
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
            field = value
            persona_avatar_view.avatarSize = value
        }

    /**
     * Text for the top hierarchy of the three TextViews.
     * [name] is used in conjunction with [email] to set initials for internal [AvatarView].
     */
    var name = ""
        set(value) {
            field = value
            updateTextViews()
        }

    /**
     * [email] is used in conjunction with [name] to set initials for internal [AvatarView].
     */
    var email = ""
        set(value) {
            field = value
            updateTextViews()
        }

    /**
     * Text for the middle hierarchy of the three TextViews
     */
    var subtitle: String = ""
        set(value) {
            field = value
            persona_subtitle.text = value
            updateTextViews()
        }

    /**
     * Text for the bottom hierarchy of the three TextViews
     */
    var footer: String = ""
        set(value) {
            field = value
            persona_footer.text = value
            updateTextViews()
        }

    private var title: String = name
        set(value) {
            field = value
            persona_title.text = value
        }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initializeViews()
        attrs?.let { setPropertiesFromAttributes(context, it) }
        updateTextViews()
    }

    /**
     * Use this function to conveniently set the strings for [name], [subtitle], and [footer] TextView text.
     */
    fun setDisplayText(name: String, subtitle: String = "", footer: String = "") {
        this.name = name
        this.subtitle = subtitle
        this.footer = footer
    }

    private fun initializeViews() {
        LayoutInflater.from(context).inflate(R.layout.view_persona, this)
    }

    private fun setPropertiesFromAttributes(context: Context, attrs: AttributeSet) {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.PersonaView)
        val avatarSizeOrdinal = styledAttrs.getInt(R.styleable.PersonaView_avatarSize, AvatarView.defaultAvatarSize.ordinal)
        avatarSize = AvatarSize.values()[avatarSizeOrdinal]
        name = styledAttrs.getString(R.styleable.PersonaView_name) ?: ""
        email = styledAttrs.getString(R.styleable.PersonaView_email) ?: ""
        subtitle = styledAttrs.getString(R.styleable.PersonaView_subtitle) ?: ""
        footer = styledAttrs.getString(R.styleable.PersonaView_footer) ?: ""
        avatarImageDrawable = styledAttrs.getDrawable(R.styleable.PersonaView_avatarDrawable)
        styledAttrs.recycle()
    }

    private fun updateTextViews() {
        title = when {
            !name.isEmpty() -> name
            !email.isEmpty() -> email
            else -> context.getString(R.string.persona_title_placeholder)
        }
        persona_avatar_view.setInfo(name, email)
        persona_footer.visibility = if (footer != "" && avatarSize != AvatarSize.SMALL) View.VISIBLE else View.GONE
        persona_subtitle.visibility = if (subtitle != "" && avatarSize != AvatarSize.SMALL) View.VISIBLE else View.GONE
    }
}
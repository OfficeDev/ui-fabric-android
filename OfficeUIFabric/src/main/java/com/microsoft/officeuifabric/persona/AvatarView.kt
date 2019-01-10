/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.persona

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.microsoft.officeuifabric.R

/**
 * [AvatarView] is a custom ImageView that displays the initials of a person on top of a colored circular
 * background. The initials are extracted from their name or email. The color of the circular
 * background is computed from the name and is based on an array of colors.
 */
open class AvatarView : AppCompatImageView {
    companion object {
        internal val defaultAvatarSize = AvatarSize.LARGE
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        if (attrs == null)
            return
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.AvatarView)
        val avatarSizeOrdinal = styledAttrs.getInt(R.styleable.AvatarView_avatarSize, defaultAvatarSize.ordinal)
        name = styledAttrs.getString(R.styleable.AvatarView_name) ?: ""
        email = styledAttrs.getString(R.styleable.AvatarView_email) ?: ""
        avatarSize = AvatarSize.values()[avatarSizeOrdinal]

        val avatarImageResourceId = styledAttrs.getResourceId(R.styleable.AvatarView_avatarImageDrawable, 0)
        if (avatarImageResourceId > 0 && resources.getResourceTypeName(avatarImageResourceId) == "drawable")
            avatarImageDrawable = styledAttrs.getDrawable(R.styleable.AvatarView_avatarImageDrawable)

        styledAttrs.recycle()
    }

    /**
     * [avatarSize] is a reference to a dimen. [avatarDisplaySize] is the converted int value
     * that is used to set [AvatarView]'s layout height and width.
     */
    var avatarSize: AvatarSize = defaultAvatarSize
        set(value) {
            field = value
            avatarDisplaySize = value.getDisplayValue(context)
        }
    var name: String = ""
        set(value) {
            field = value
            initials.setInfo(name, email)
        }
    var email: String = ""
        set(value) {
            field = value
            initials.setInfo(name, email)
        }
    var avatarImageBitmap: Bitmap? = null
        set(value) {
            field = value
            setImageBitmap(value)
        }
    var avatarImageDrawable: Drawable? = null
        set(value) {
            field = value
            setImageDrawable(value)
        }
    var avatarImageResourceId: Int? = null
        set(value) {
            field = value
            value?.let { setImageResource(it) }
        }
    var avatarImageUri: Uri? = null
        set(value) {
            field = value
            setImageURI(value)
        }

    private val initials = InitialsDrawable(context)
    private var avatarDisplaySize = defaultAvatarSize.getDisplayValue(context)

    override fun draw(canvas: Canvas) {
        initials.bounds = Rect(0, 0, width, height)
        initials.draw(canvas)
        super.draw(canvas)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable is BitmapDrawable) {
            setImageBitmap(drawable.bitmap)
        } else {
            super.setImageDrawable(drawable)
        }
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        if (bitmap == null)
            return

        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedBitmapDrawable.isCircular = true
        super.setImageDrawable(roundedBitmapDrawable)
    }

    override fun setImageResource(resId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        setImageBitmap(bitmap)
    }

    override fun setImageURI(uri: Uri?) {
        if (uri == null)
            return

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            setImageBitmap(bitmap)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSizeAndState(avatarDisplaySize, widthMeasureSpec, 0),
            resolveSizeAndState(avatarDisplaySize, heightMeasureSpec, 0)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        layoutParams.width = avatarDisplaySize
        layoutParams.height = avatarDisplaySize
        Handler().post { requestLayout() }
        super.onSizeChanged(w, h, oldw, oldh)
    }
}

// TODO: See if this can be used anywhere
fun AvatarView.setAvatar(avatar: IAvatar) {
    name = avatar.name
    email = avatar.email
    avatarImageBitmap = avatar.avatarImageBitmap
    avatarImageDrawable = avatar.avatarImageDrawable
    avatarImageResourceId = avatar.avatarImageResourceId
    avatarImageUri = avatar.avatarImageUri
}
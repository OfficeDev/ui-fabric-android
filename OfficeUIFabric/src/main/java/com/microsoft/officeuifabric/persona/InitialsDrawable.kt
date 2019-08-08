/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.persona

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.IntRange
import android.text.*
import com.microsoft.officeuifabric.R
import com.microsoft.officeuifabric.util.ThemeUtil
import java.util.*

/**
 * [InitialsDrawable] generates initials and background color for the AvatarView.
 */
internal class InitialsDrawable : Drawable {
    companion object {
        // This is the displayed value for avatars / headers when the name starts with a symbol
        private const val DEFAULT_SYMBOL_HEADER = '#'
        private const val DEFAULT_INITIALS_TEXT_SIZE_RATIO = 0.4f
        private var backgroundColors: IntArray? = null

        fun getInitials(name: String, email: String): String {
            val names = name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var initials = ""

            if (names.isNotEmpty())
                for (partialName in names) {
                    val trimmed = partialName.trim { it <= ' ' }
                    if (trimmed.isNotEmpty() && initials.length < 2) {
                        val initial = trimmed[0]
                        // Skip this character if it's in our ignored list
                        if (!Character.isLetterOrDigit(initial))
                            continue

                        initials += initial
                    }
                }

            if (initials.isEmpty())
                initials = if (email.length > 1) email.substring(0, 1) else DEFAULT_SYMBOL_HEADER.toString()

            return initials.toUpperCase(Locale.getDefault())
        }

        @ColorInt
        fun getInitialsBackgroundColor(backgroundColors: IntArray?, name: String, email: String): Int {
            val s = name + email
            val whichColor = Math.abs(s.hashCode()) % (backgroundColors?.size ?: 1)
            return backgroundColors?.get(whichColor) ?: 0
        }
    }

    var avatarStyle: AvatarStyle = AvatarStyle.SQUARE

    private val context: Context
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path: Path = Path()
    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var initials: String? = null
    private var initialsBackgroundColor: Int = 0
    private var initialsLayout: Layout? = null

    constructor(context: Context) : super() {
        this.context = context

        textPaint.color = Color.WHITE
        textPaint.density = context.resources.displayMetrics.density

        if (backgroundColors == null)
            backgroundColors = ThemeUtil.getColors(context, R.array.uifabric_avatar_background_colors)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        path.reset()

        when (avatarStyle) {
            AvatarStyle.CIRCLE ->
                path.addCircle(width / 2f, height / 2f, width / 2f, Path.Direction.CW)
            AvatarStyle.SQUARE -> {
                val cornerRadius = context.resources.getDimension(R.dimen.uifabric_avatar_square_corner_radius)
                path.addRoundRect(RectF(bounds), cornerRadius, cornerRadius, Path.Direction.CW)
            }
        }

        paint.color = initialsBackgroundColor
        paint.style = Paint.Style.FILL

        canvas.drawPath(path, paint)

        initialsLayout?.let {
            canvas.save()
            canvas.translate(0f, (height - it.height) / 2f)
            it.draw(canvas)
            canvas.restore()
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        if (TextUtils.isEmpty(initials))
            return

        val size = right - left
        textPaint.textSize = size * DEFAULT_INITIALS_TEXT_SIZE_RATIO

        val boringMetrics = BoringLayout.isBoring(initials, textPaint)

        if (boringMetrics != null)
            if (initialsLayout is BoringLayout)
                initialsLayout = (initialsLayout as BoringLayout).replaceOrMake(initials, textPaint, size,
                    Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, boringMetrics, false)
            else
                initialsLayout = BoringLayout.make(initials, textPaint, size,
                    Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, boringMetrics, false)
        else
            initialsLayout = StaticLayout(initials, textPaint, size,
                Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    /**
     * Uses [name] and [email] to generate initials
     */
    fun setInfo(name: String, email: String, @ColorInt colorInt: Int? = null) {
        val initialsBackgroundColor = colorInt ?: getInitialsBackgroundColor(backgroundColors, name, email)
        initials = getInitials(name, email)
        this.initialsBackgroundColor = initialsBackgroundColor
        invalidateSelf()
    }
}

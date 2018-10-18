/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.microsoft.officeuifabric.persona.AvatarSize
import com.microsoft.officeuifabric.persona.AvatarView
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_avatar.*

class AvatarFragment : DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_avatar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avatar drawables with bitmap
        loadBitmapFromPicasso(avatar_example_picasso)
        loadBitmapFromGlide(avatar_example_glide)
        avatar_example_local.setImageResource(R.drawable.avatar_erik_nason)

        // Avatar drawable with initials
        avatar_example_initials.setInfo(getString(R.string.persona_name_kat_larsson), getString(R.string.persona_email_kat_larsson))

        // Add AvatarView with code
        createNewAvatarFromCode()
    }

    private fun loadBitmapFromPicasso(imageView: ImageView) {
        Picasso.get()
            .load(R.drawable.avatar_celeste_burton)
            .into(imageView)
    }

    private fun loadBitmapFromGlide(imageView: ImageView) {
        Glide.with(this)
            .load(R.drawable.avatar_isaac_fielder)
            .into(imageView)
    }

    private fun createNewAvatarFromCode() {
        val context = context ?: return
        val avatarView = AvatarView(context)
        avatarView.avatarSize = AvatarSize.XXLARGE
        avatarView.setInfo(getString(R.string.persona_name_mauricio_august), getString(R.string.persona_email_mauricio_august))
        avatarView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        avatarView.id = R.id.avatar_example_code
        avatar_layout.addView(avatarView)
    }
}

//
// Copyright © 2018 Microsoft Corporation. All rights reserved.
//

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.microsoft.officeuifabric.AvatarSize
import com.microsoft.officeuifabric.AvatarView
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
        avatar_example_local.setImageResource(R.drawable.avatar_male)

        // Avatar drawable with initials
        avatar_example_initials.setInfo(getString(R.string.avatar_female_name), getString(R.string.avatar_female_email))

        // Add AvatarView with code
        createNewAvatarFromCode()
    }

    private fun loadBitmapFromPicasso(imageView: ImageView) {
        Picasso.get()
            .load(R.drawable.avatar_female)
            .into(imageView)
    }

    private fun loadBitmapFromGlide(imageView: ImageView) {
        Glide.with(this)
            .load(R.drawable.avatar_female)
            .into(imageView)
    }

    private fun createNewAvatarFromCode() {
        val avatarView = AvatarView(context)
        avatarView.avatarSize = AvatarSize.SIZE_70
        avatarView.setInfo(getString(R.string.avatar_male_name), getString(R.string.avatar_male_email))
        avatarView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        avatarView.id = R.id.avatar_example_code
        avatar_layout.addView(avatarView)
    }
}

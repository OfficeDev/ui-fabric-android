/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.Persona
import com.microsoft.officeuifabricdemo.R

fun createPersonaList(context: Context?): ArrayList<IPersona> {
    val context = context ?: return arrayListOf()
    return arrayListOf(
        createPersona(
            context.getString(R.string.persona_name_amanda_brady),
            context.getString(R.string.persona_subtitle_manager),
            imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_amanda_brady)
        ),
        createPersona(
            context.getString(R.string.persona_name_lydia_bauer),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_daisy_phillips),
            context.getString(R.string.persona_subtitle_designer),
            imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_daisy_phillips)
        ),
        createPersona(
            context.getString(R.string.persona_name_allan_munger) + context.getString(R.string.persona_truncation),
            context.getString(R.string.persona_subtitle_manager)
        ),
        createPersona(
            context.getString(R.string.persona_name_mauricio_august),
            context.getString(R.string.persona_subtitle_designer),
            imageBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.avatar_mauricio_august)
        ),
        createPersona(
            context.getString(R.string.persona_name_ashley_mccarthy),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_miguel_garcia),
            context.getString(R.string.persona_subtitle_researcher),
            imageUri = getUriFromResource(context, R.drawable.avatar_miguel_garcia)
        ),
        createPersona(
            context.getString(R.string.persona_name_carole_poland),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_mona_kane),
            context.getString(R.string.persona_subtitle_designer)
        ),
        createPersona(
            context.getString(R.string.persona_name_carlos_slattery),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_wanda_howard),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_tim_deboer),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_robin_counts),
            context.getString(R.string.persona_subtitle_designer)
        ),
        createPersona(
            context.getString(R.string.persona_name_elliot_woordward),
            context.getString(R.string.persona_subtitle_designer)
        ),
        createPersona(
            context.getString(R.string.persona_name_cecil_folk),
            context.getString(R.string.persona_subtitle_manager)
        ),
        createPersona(
            context.getString(R.string.persona_name_celeste_burton),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_elvia_atkins),
            context.getString(R.string.persona_subtitle_designer),
            imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_elvia_atkins)
        ),
        createPersona(
            context.getString(R.string.persona_name_colin_ballinger),
            context.getString(R.string.persona_subtitle_manager),
            imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_colin_ballinger)
        ),
        createPersona(
            context.getString(R.string.persona_name_katri_ahokas),
            context.getString(R.string.persona_subtitle_designer),
            imageBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.avatar_katri_ahokas)
        ),
        createPersona(
            context.getString(R.string.persona_name_henry_brill),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_johnie_mcconnell),
            context.getString(R.string.persona_subtitle_researcher),
            imageUri = getUriFromResource(context, R.drawable.avatar_johnie_mcconnell)
        ),
        createPersona(
            context.getString(R.string.persona_name_kevin_sturgis),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_kristen_patterson),
            context.getString(R.string.persona_subtitle_designer)
        ),
        createPersona(
            context.getString(R.string.persona_name_charlotte_waltson),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_erik_nason),
            context.getString(R.string.persona_subtitle_engineer)
        ),
        createPersona(
            context.getString(R.string.persona_name_isaac_fielder),
            context.getString(R.string.persona_subtitle_researcher)
        ),
        createPersona(
            context.getString(R.string.persona_name_mauricio_august),
            context.getString(R.string.persona_subtitle_designer)
        )
    )
}

private fun getUriFromResource(context: Context, avatarDrawable: Int): Uri? {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" + context.resources.getResourcePackageName(avatarDrawable) +
        '/'.toString() + context.resources.getResourceTypeName(avatarDrawable) +
        '/'.toString() + context.resources.getResourceEntryName(avatarDrawable)
    )
}

private fun createPersona(
    name: String,
    subtitle: String,
    imageResource: Int? = null,
    imageDrawable: Drawable? = null,
    imageBitmap: Bitmap? = null,
    imageUri: Uri? = null
): IPersona {
    val persona = Persona(name)
    persona.subtitle = subtitle
    persona.avatarImageResourceId = imageResource
    persona.avatarImageDrawable = imageDrawable
    persona.avatarImageBitmap = imageBitmap
    persona.avatarImageUri = imageUri
    return persona
}
/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabric.peoplepicker.PersonaListAdapter
import com.microsoft.officeuifabric.persona.IPersona
import com.microsoft.officeuifabric.persona.Persona
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_people_picker.*

class PersonaListViewFragment : DemoFragment() {
    private lateinit var personaList: ArrayList<IPersona>

    override fun needsScrollableContainer(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        preparePersonaList()
        return inflater.inflate(R.layout.fragment_people_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        people_picker_example.personaList = personaList
        people_picker_example.onItemClicked = object : PersonaListAdapter.Callback {
            override fun onItemClicked(persona: IPersona) {
                val snackbar = Snackbar.make(
                    view,
                    "You clicked on the cell for ${persona.name}, ${persona.subtitle}",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
            }
        }
    }

    private fun preparePersonaList() {
        val context = context ?: return
        personaList = arrayListOf(
            createPersona(
                getString(R.string.persona_name_amanda_brady),
                getString(R.string.persona_subtitle_manager),
                R.drawable.avatar_amanda_brady
            ),
            createPersona(
                getString(R.string.persona_name_lydia_bauer),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_daisy_phillips),
                getString(R.string.persona_subtitle_designer),
                imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_daisy_phillips)
            ),
            createPersona(
                getString(R.string.persona_name_allan_munger) + getString(R.string.persona_truncation),
                getString(R.string.persona_subtitle_manager)
            ),
            createPersona(
                getString(R.string.persona_name_mauricio_august),
                getString(R.string.persona_subtitle_designer),
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.avatar_mauricio_august)
            ),
            createPersona(
                getString(R.string.persona_name_ashley_mccarthy),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_miguel_garcia),
                getString(R.string.persona_subtitle_researcher),
                imageUri = getUriFromResource(context, R.drawable.avatar_miguel_garcia)
            ),
            createPersona(
                getString(R.string.persona_name_carole_poland),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_mona_kane),
                getString(R.string.persona_subtitle_designer)
            ),
            createPersona(
                getString(R.string.persona_name_carlos_slattery),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_wanda_howard),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_tim_deboer),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_robin_counts),
                getString(R.string.persona_subtitle_designer)
            ),
            createPersona(
                getString(R.string.persona_name_elliot_woordward),
                getString(R.string.persona_subtitle_designer)
            ),
            createPersona(
                getString(R.string.persona_name_cecil_folk),
                getString(R.string.persona_subtitle_manager),
                R.drawable.avatar_colin_ballinger
            ),
            createPersona(
                getString(R.string.persona_name_celeste_burton),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_elvia_atkins),
                getString(R.string.persona_subtitle_designer),
                imageDrawable = ContextCompat.getDrawable(context, R.drawable.avatar_elvia_atkins)
            ),
            createPersona(
                getString(R.string.persona_name_colin_ballinger),
                getString(R.string.persona_subtitle_manager)
            ),
            createPersona(
                getString(R.string.persona_name_katri_ahokas),
                getString(R.string.persona_subtitle_designer),
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.avatar_katri_ahokas)
            ),
            createPersona(
                getString(R.string.persona_name_henry_brill),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_johnie_mcconnell),
                getString(R.string.persona_subtitle_researcher),
                imageUri = getUriFromResource(context, R.drawable.avatar_johnie_mcconnell)
            ),
            createPersona(
                getString(R.string.persona_name_kevin_sturgis),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_kristen_patterson),
                getString(R.string.persona_subtitle_designer)
            ),
            createPersona(
                getString(R.string.persona_name_charlotte_waltson),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_erik_nason),
                getString(R.string.persona_subtitle_engineer)
            ),
            createPersona(
                getString(R.string.persona_name_isaac_fielder),
                getString(R.string.persona_subtitle_researcher)
            ),
            createPersona(
                getString(R.string.persona_name_mauricio_august),
                getString(R.string.persona_subtitle_designer)
            ),
            createPersona(
                getString(R.string.persona_name_amanda_brady),
                getString(R.string.persona_subtitle_designer)
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

    private fun createPersona(name: String, subtitle: String, imageResource: Int? = null, imageDrawable: Drawable? = null,
                              imageBitmap: Bitmap? = null, imageUri: Uri? = null): Persona {
        val persona = Persona(name)
        persona.subtitle = subtitle
        persona.avatarImageResourceId = imageResource
        persona.avatarImageDrawable = imageDrawable
        persona.avatarImageBitmap = imageBitmap
        persona.avatarImageUri = imageUri
        return persona
    }
}
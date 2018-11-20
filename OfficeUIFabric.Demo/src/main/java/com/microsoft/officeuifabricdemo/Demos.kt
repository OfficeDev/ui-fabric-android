package com.microsoft.officeuifabricdemo

import android.support.v4.app.Fragment
import com.microsoft.officeuifabricdemo.demos.*
import java.util.*
import kotlin.reflect.KClass

const val AVATAR = "Avatar"
const val BUTTON = "Button"
const val DATE_PICKER = "DatePicker"
const val DATE_TIME_PICKER_DIALOG = "DateTimePickerDialog"
const val PERSONA = "Persona"
const val PERSONA_CHIP = "PersonaChip"
const val PERSONA_LIST_VIEW = "PersonaListView"
const val TEMPLATE_VIEW = "TemplateView"
const val TYPOGRAPHY = "Typography"

val DEMOS = arrayListOf(
    Demo(AVATAR, AvatarFragment::class),
    Demo(BUTTON, ButtonFragment::class),
    Demo(DATE_PICKER, DatePickerFragment::class),
    Demo(DATE_TIME_PICKER_DIALOG, DateTimePickerDialogFragment::class),
    Demo(PERSONA, PersonaFragment::class),
    Demo(PERSONA_CHIP, PersonaChipFragment::class),
    Demo(PERSONA_LIST_VIEW, PersonaListViewFragment::class),
    Demo(TEMPLATE_VIEW, TemplateViewFragment::class),
    Demo(TYPOGRAPHY, TypographyFragment::class)
)

data class Demo(val title: String, val demoClass: KClass<out Fragment>) {
    val id: UUID = UUID.randomUUID()
}

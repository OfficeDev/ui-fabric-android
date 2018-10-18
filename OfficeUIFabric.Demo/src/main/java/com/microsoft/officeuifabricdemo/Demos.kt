package com.microsoft.officeuifabricdemo

import android.support.v4.app.Fragment
import com.microsoft.officeuifabricdemo.demos.*
import java.util.*
import kotlin.reflect.KClass

val DEMOS = arrayListOf(
    Demo("Avatar", AvatarFragment::class),
    Demo("Button", ButtonFragment::class),
    Demo("PeoplePicker", PeoplePickerFragment::class),
    Demo("Persona", PersonaFragment::class),
    Demo("TemplateView", TemplateViewFragment::class),
    Demo("Typography", TypographyFragment::class)
)

data class Demo(val title: String, val demoClass: KClass<out Fragment>) {
    val id: UUID = UUID.randomUUID()
}

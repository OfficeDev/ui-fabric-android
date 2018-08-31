package com.microsoft.officeuifabricdemo

import android.support.v4.app.Fragment
import com.microsoft.officeuifabricdemo.demos.AvatarFragment
import com.microsoft.officeuifabricdemo.demos.ButtonFragment
import java.util.*
import kotlin.reflect.KClass

val DEMOS = arrayListOf(
    Demo("Avatar", AvatarFragment::class),
    Demo("Button", ButtonFragment::class),
    Demo("TextEdit", DemoFragment::class),  //!!!
    Demo("TextField", DemoFragment::class)  //!!!
)

data class Demo(val title: String, val demoClass: KClass<out Fragment>) {
    val id: UUID = UUID.randomUUID()
}

package com.microsoft.officeuifabric

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ButtonUnitTest {
    @Test
    fun testSimpleButton() {
        val button = Button(RuntimeEnvironment.application)
        Assert.assertNotNull(button)
    }

    @Test
    fun testStyledButton() {
        val button = Button(RuntimeEnvironment.application)
        button.showBorder = false
        Assert.assertFalse(button.showBorder)
    }
}
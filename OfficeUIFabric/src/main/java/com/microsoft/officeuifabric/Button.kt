package com.microsoft.officeuifabric

import android.content.Context
import android.util.AttributeSet

open class Button : android.widget.Button {
    private var _showBorder: Boolean = true

    var showBorder: Boolean
        get() = _showBorder
        set(newValue) {
            _showBorder = newValue
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
    }
}

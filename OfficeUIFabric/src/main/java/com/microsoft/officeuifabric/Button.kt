package com.microsoft.officeuifabric

import android.content.Context

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

    private fun init() {
    }
}

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
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.Button)
        showBorder = styledAttrs.getBoolean(R.styleable.Button_showBorder, showBorder)
        styledAttrs.recycle()
    }

    private fun init() {
    }
}

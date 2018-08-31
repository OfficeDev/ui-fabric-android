//
// Copyright © 2018 Microsoft Corporation. All rights reserved.
//

package com.microsoft.officeuifabric

import android.content.Context
import android.util.AttributeSet

open class Button : android.support.v7.widget.AppCompatButton {
    private var _showBorder: Boolean = true

    var showBorder: Boolean
        get() = _showBorder
        set(newValue) {
            _showBorder = newValue
        }

    init {
        // core initialization
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeFromStyle(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeFromStyle(context, attrs)
    }

    private fun initializeFromStyle(context: Context, attrs: AttributeSet) {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.Button)
        showBorder = styledAttrs.getBoolean(R.styleable.Button_showBorder, showBorder)
        styledAttrs.recycle()
    }
}

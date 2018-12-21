/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_typography.*

class TypographyActivity : DemoActivity() {
    override val contentLayoutId: Int
        get() = R.layout.activity_typography

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TextViewCompat.setTextAppearance(typography_example_body_2, R.style.TextAppearance_UIFabric_Body2)
    }
}
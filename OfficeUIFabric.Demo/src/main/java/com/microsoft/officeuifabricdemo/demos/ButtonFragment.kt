//
// Copyright © 2018 Microsoft Corporation. All rights reserved.
//

package com.microsoft.officeuifabricdemo.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microsoft.officeuifabricdemo.DemoFragment
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.fragment_button.*

class ButtonFragment : DemoFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.showBorder = true
    }
}

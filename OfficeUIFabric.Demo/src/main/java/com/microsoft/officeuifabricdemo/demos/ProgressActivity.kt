/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.ProgressBar
import com.microsoft.officeuifabricdemo.DemoActivity
import com.microsoft.officeuifabricdemo.R
import kotlinx.android.synthetic.main.activity_progress.*

class ProgressActivity : DemoActivity() {
    override val contentLayoutId: Int
        get() = R.layout.activity_progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDrawableColorPreLollipop(progress_bar_large_primary)
        setDrawableColorPreLollipop(progress_bar_medium_primary)
        setDrawableColorPreLollipop(progress_bar_small_primary)
        setDrawableColorPreLollipop(progress_bar_xsmall_primary)
    }

    /**
     * For pre-Lollipop versions, you can set the progress bar drawable color in code like this.
     * For Lollipop and after, use our styles in styles.xml such as Widget.UIFabric.CircularProgress.Primary.
     */
    private fun setDrawableColorPreLollipop(progressBar: ProgressBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return

        val mutatedProgressBarDrawable = progressBar.indeterminateDrawable.mutate()
        mutatedProgressBarDrawable.setColorFilter(ContextCompat.getColor(this, R.color.uifabric_progress_primary), PorterDuff.Mode.SRC_IN)
        progressBar.indeterminateDrawable = mutatedProgressBarDrawable
    }
}
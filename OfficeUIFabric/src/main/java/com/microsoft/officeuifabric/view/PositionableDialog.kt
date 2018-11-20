/**
 * Copyright © 2018 Microsoft Corporation. All rights reserved.
 */

package com.microsoft.officeuifabric.view

import android.graphics.Rect
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.View

import java.util.Arrays

/**
 * A dialog that can be positioned and sized
 *
 * Pass an integer pair that describes the custom location of the dialog using the `EXTRA_CUSTOM_SCREEN_POSITION`
 * parameter. Gravity will be set to top-left when using a custom location
 *
 * Pass an integer pair that describes the custom size of the dialog using the `EXTRA_CUSTOM_SCREEN_SIZE`
 * parameter. A width/height value <= 0 will be ignored
 *
 */
open class PositionableDialog : DialogFragment() {
    companion object {
        const val EXTRA_CUSTOM_SCREEN_POSITION = "EXTRA_CUSTOM_SCREEN_POSITION"
        const val EXTRA_CUSTOM_SCREEN_SIZE = "EXTRA_CUSTOM_SCREEN_SIZE"

        /**
         * Find the position of the given view on screen in a format that can be used
         * to position a dialog window
         *
         * @param anchor the target anchor view
         * @return the top-left corner of the anchor view
         */
        @JvmStatic
        fun findAnchorPosition(anchor: View): IntArray {
            val screenPos = IntArray(2)
            val displayFrame = Rect()
            anchor.getLocationOnScreen(screenPos)
            anchor.getWindowVisibleDisplayFrame(displayFrame)

            val viewLeft = screenPos[0] - displayFrame.left
            val viewTop = screenPos[1] - displayFrame.top

            return intArrayOf(viewLeft, viewTop)
        }
    }

    override fun onStart() {
        super.onStart()
        updateCustomPositionIfNeeded()
    }

    /**
     * Gives derived classes a chance to modify custom location if necessary
     *
     * @param customPosition the custom location integer pair supplied in our bundle
     */
    protected open fun adjustCustomPosition(customPosition: IntArray) { }

    private fun updateCustomPositionIfNeeded() {
        val customPosition = arguments?.getIntArray(EXTRA_CUSTOM_SCREEN_POSITION)
        val customSize = arguments?.getIntArray(EXTRA_CUSTOM_SCREEN_SIZE)
        setCustomPosition(customPosition, customSize)
    }

    private fun setCustomPosition(customPosition: IntArray?, customSize: IntArray?) {
        if (!isValidCustomPair(customPosition) && !isValidCustomPair(customSize))
            return

        val window = activity?.window ?: return
        val params = window.attributes

        if (customPosition != null) {
            window.setGravity(Gravity.TOP or Gravity.START)
            val customPositionCopy = Arrays.copyOf(customPosition, customPosition.size)
            adjustCustomPosition(customPositionCopy)
            params.x = customPositionCopy[0]
            params.y = customPositionCopy[1]
        }

        if (customSize != null) {
            if (customSize[0] > 0)
                params.width = customSize[0]

            if (customSize[1] > 0)
                params.height = customSize[1]
        }

        window.attributes = params
    }

    private fun isValidCustomPair(pair: IntArray?): Boolean {
        return pair != null && pair.size == 2
    }
}

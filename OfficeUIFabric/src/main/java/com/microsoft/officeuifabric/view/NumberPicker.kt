package com.microsoft.officeuifabric.view

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatEditText
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextUtils
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.view.LayoutInflater.Filter
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

import com.microsoft.officeuifabric.R

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.text.DecimalFormatSymbols
import java.util.ArrayList
import java.util.Locale

/**
 * A widget that enables the user to select a number from a predefined range.
 * There are two flavors of this widget and which one is presented to the user
 * depends on the current theme.
 *
 *  *
 * If the current theme is derived from [android.R.style.Theme] the widget
 * presents the current value as an editable input field with an increment button
 * above and a decrement button below. Long pressing the buttons allows for a quick
 * change of the current value. Tapping on the input field allows to type in
 * a desired value.
 *
 *  *
 * If the current theme is derived from [android.R.style.Theme_Holo] or
 * [android.R.style.Theme_Holo_Light] the widget presents the current
 * value as an editable input field with a lesser value above and a greater
 * value below. Tapping on the lesser or greater value selects it by animating
 * the number axis up or down to make the chosen value current. Flinging up
 * or down allows for multiple increments or decrements of the current value.
 * Long pressing on the lesser and greater values also allows for a quick change
 * of the current value. Tapping on the current value allows to type in a
 * desired value.
 *
 *
 *
 *
 * For an example of using this widget, see [android.widget.TimePicker].
 *
 */
internal class NumberPicker : LinearLayout {
    companion object {
        const val BUTTON_INCREMENT = 1
        const val BUTTON_DECREMENT = 2

        private const val UNDEFINED = Integer.MIN_VALUE
        private const val VIRTUAL_VIEW_ID_INCREMENT = 1
        private const val VIRTUAL_VIEW_ID_INPUT = 2
        private const val VIRTUAL_VIEW_ID_DECREMENT = 3

        /**
         * The number of items show in the selector wheel.
         */
        private const val DEFAULT_SELECTOR_WHEEL_ITEM_COUNT = 3
        /**
         * The default update interval during long press.
         */
        private const val DEFAULT_LONG_PRESS_UPDATE_INTERVAL: Long = 300
        /**
         * The coefficient by which to adjust (divide) the max fling velocity.
         */
        private const val SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8
        /**
         * The the duration for adjusting the selector wheel.
         */
        private const val SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800
        /**
         * The duration of scrolling while snapping to a given position.
         */
        private const val SNAP_SCROLL_DURATION = 300
        /**
         * The strength of fading in the top and bottom while drawing the selector.
         */
        private const val TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f
        /**
         * The default unscaled height of the selection divider.
         */
        private const val UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2
        /**
         * The default unscaled distance between the selection dividers.
         */
        private const val UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48
        /**
         * The resource id for the default layout.
         */
        // private val DEFAULT_LAYOUT_RESOURCE_ID = R.layout.number_picker_with_selector_wheel
        /**
         * Constant for unspecified size.
         */
        private const val SIZE_UNSPECIFIED = -1

        private const val ALIGN_LEFT = 0
        private const val ALIGN_CENTER = 1
        private const val ALIGN_RIGHT = 2

        private const val QUICK_ANIMATE_THRESHOLD = 15
        private val sTwoDigitFormatter = TwoDigitFormatter()
        /**
         * @hide
         */
        val twoDigitFormatter: android.widget.NumberPicker.Formatter
            get() = sTwoDigitFormatter
        /**
         * The numbers accepted by the input text's [Filter]
         */
        private val DIGIT_CHARACTERS = charArrayOf(
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8', '\u06f9',
            // Hindi and Marathi (Devanagari script)
            '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e', '\u096f',
            // Bengali
            '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee', '\u09ef',
            // Kannada
            '\u0ce6', '\u0ce7', '\u0ce8', '\u0ce9', '\u0cea', '\u0ceb', '\u0cec', '\u0ced', '\u0cee', '\u0cef')

        private fun formatNumberWithLocale(value: Int): String {
            return String.format(Locale.getDefault(), "%d", value)
        }
    }
    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    /**
     * Set the current value for the number picker.
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current value is set to the [NumberPicker.getMinValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMinValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current value is set to the [NumberPicker.getMaxValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `false` the
     * current value is set to the [NumberPicker.getMaxValue] value.
     *
     *
     *
     * If the argument is less than the [NumberPicker.getMaxValue] and
     * [NumberPicker.getWrapSelectorWheel] is `true` the
     * current value is set to the [NumberPicker.getMinValue] value.
     *
     *
     * @param value The current value.
     * @see .setWrapSelectorWheel
     * @see .setMinValue
     * @see .setMaxValue
     */
    var value: Int
        get() = mValue
        set(value) = setValueInternal(value, false)
    /**
     * The values to be displayed instead the indices.
     */
    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     *
     * **Note:** The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    // Allow text entry rather than strictly numeric entry.
    var displayedValues: Array<String>? = null
        set(displayedValues) {
            if (this.displayedValues == displayedValues)
                return

            field = displayedValues
            if (displayedValues != null) {
                mInputText?.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            } else {
                mInputText?.setRawInputType(InputType.TYPE_CLASS_NUMBER)
            }
            updateInputTextView()
            initializeSelectorWheelIndices()
            tryComputeMaxWidth()
        }
    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     */
    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    /**
     * Sets the min value of the picker.
     *
     * @param minValue The min value inclusive.
     *
     * **Note:** The length of the displayed values array
     * set via [.setDisplayedValues] must be equal to the
     * range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    var minValue: Int = 0
        set(value) {
            if (minValue == value)
                return

            if (value < 0)
                throw IllegalArgumentException("minValue must be >= 0")

            field = value
            if (minValue > mValue)
                mValue = minValue

            mSelectorIndices?.let { wrapSelectorWheel = maxValue - minValue > it.size }
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }
    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     */
    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    /**
     * Sets the max value of the picker.
     *
     * @param maxValue The max value inclusive.
     *
     * **Note:** The length of the displayed values array
     * set via [.setDisplayedValues] must be equal to the
     * range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    var maxValue: Int = 0
        set(value) {
            if (maxValue == value)
                return

            if (value < 0)
                throw IllegalArgumentException("maxValue must be >= 0")

            field = value
            if (maxValue < mValue)
                mValue = maxValue

            mSelectorIndices?.let { wrapSelectorWheel = maxValue - minValue > it.size }
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    private var mSelectorWheelItemCount: Int = 0
    /**
     * The index of the middle selector item.
     */
    private var mSelectorMiddleItemIndex: Int = 0
    /**
     * The increment button.
     */
    private var mIncrementButton: ImageButton? = null
    /**
     * The decrement button.
     */
    private var mDecrementButton: ImageButton? = null
    /**
     * The text for showing the current value.
     */
    private var mInputText: EditText? = null
    /**
     * The distance between the two selection dividers.
     */
    private var mSelectionDividersDistance: Int = 0
    /**
     * The min height of this widget.
     */
    private var mMinHeight: Int = 0
    /**
     * The max height of this widget.
     */
    private var mMaxHeight: Int = 0
    /**
     * The max width of this widget.
     */
    private var mMinWidth: Int = 0
    /**
     * The max width of this widget.
     */
    private var mMaxWidth: Int = 0
    /**
     * Flag whether to compute the max width.
     */
    private var mComputeMaxWidth: Boolean = false
    /**
     * The height of the text.
     */
    private var mTextSize: Int = 0
    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapHeight: Int = 0
    /**
     * Current value of this NumberPicker
     */
    private var mValue: Int = 0
    /**
     * Listener to be notified upon current value change.
     */
    private var mOnValueChangeListener: OnValueChangeListener? = null
    /**
     * Listener to be notified upon scroll state change.
     */
    private var mOnScrollListener: OnScrollListener? = null
    /**
     * Formatter for for displaying the current value.
     */
    private var mFormatter: android.widget.NumberPicker.Formatter? = null
    /**
     * The speed for updating the value form long press.
     */
    private var mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL
    /**
     * Cache for the string representation of selector indices.
     */
    private val mSelectorIndexToStringCache = SparseArray<String>()
    /**
     * The selector indices whose value are show by the selector.
     */
    private var mSelectorIndices: IntArray? = null
    /**
     * The [Paint] for drawing the selector.
     */
    private var mSelectorWheelPaint: Paint? = null
    /**
     * The [Drawable] for pressed virtual (increment/decrement) buttons.
     */
    private var mVirtualButtonPressedDrawable: Drawable? = null
    /**
     * The height of a selector element (text + gap).
     */
    private var mSelectorElementHeight: Int = 0
    /**
     * The initial offset of the scroll selector.
     */
    private var mInitialScrollOffset = Integer.MIN_VALUE
    /**
     * The current offset of the scroll selector.
     */
    private var mCurrentScrollOffset: Int = 0
    /**
     * The [Scroller] responsible for flinging the selector.
     */
    private var mFlingScroller: Scroller? = null
    /**
     * The [Scroller] responsible for adjusting the selector.
     */
    private var mAdjustScroller: Scroller? = null
    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private var mPreviousScrollerY: Int = 0
    /**
     * Handle to the reusable command for setting the input text selection.
     */
    private var mSetSelectionCommand: SetSelectionCommand? = null
    /**
     * Handle to the reusable command for changing the current value from long
     * press by one.
     */
    private var mChangeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? = null
    /**
     * Command for beginning an edit of the current value via IME on long press.
     */
    private var mBeginSoftInputOnLongPressCommand: BeginSoftInputOnLongPressCommand? = null
    /**
     * The Y position of the last down event.
     */
    private var mLastDownEventY: Float = 0.toFloat()
    /**
     * The time of the last down event.
     */
    private var mLastDownEventTime: Long = 0
    /**
     * The Y position of the last down or move event.
     */
    private var mLastDownOrMoveEventY: Float = 0.toFloat()
    /**
     * Determines speed during touch scrolling.
     */
    private var mVelocityTracker: VelocityTracker? = null
    /**
     * @see ViewConfiguration.getScaledTouchSlop
     */
    private var mTouchSlop: Int = 0
    /**
     * @see ViewConfiguration.getScaledMinimumFlingVelocity
     */
    private var mMinimumFlingVelocity: Int = 0
    /**
     * @see ViewConfiguration.getScaledMaximumFlingVelocity
     */
    private var mMaximumFlingVelocity: Int = 0
    /**
     * Flag whether the selector should wrap around.
     */
    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see .getMinValue
     * @see .getMaxValue
     */
    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the [NumberPicker.getMinValue] and
     * [NumberPicker.getMaxValue] values.
     *
     *
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     *
     *
     *
     * **Note:** If the number of items, i.e. the range (
     * [.getMaxValue] - [.getMinValue]) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     *
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    var wrapSelectorWheel: Boolean = false
        set(wrapSelectorWheel) {
            val wrappingAllowed = maxValue - minValue >= mSelectorIndices!!.size
            if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != this.wrapSelectorWheel) {
                field = wrapSelectorWheel
            }
        }
    /**
     * The back ground color used to optimize scroller fading.
     */
    private var mSolidColor: Int = 0
    /**
     * Flag whether this widget has a selector wheel.
     */
    private var mHasSelectorWheel: Boolean = false
    /**
     * Divider for showing item to be selected while scrolling
     */
    private var mSelectionDivider: Drawable? = null
    /**
     * The height of the selection divider.
     */
    private var mSelectionDividerHeight: Int = 0
    /**
     * The current scroll state of the number picker.
     */
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE
    /**
     * Flag whether to ignore move events - we ignore such when we show in IME
     * to prevent the content from scrolling.
     */
    private var mIgnoreMoveEvents: Boolean = false
    /**
     * Flag whether to perform a click on tap.
     */
    private var mPerformClickOnTap: Boolean = false
    /**
     * The top of the top selection divider.
     */
    private var mTopSelectionDividerTop: Int = 0
    /**
     * The bottom of the bottom selection divider.
     */
    private var mBottomSelectionDividerBottom: Int = 0
    /**
     * The virtual id of the last hovered child.
     */
    private var mLastHoveredChildVirtualViewId: Int = 0
    /**
     * Whether the increment virtual button is pressed.
     */
    private var mIncrementVirtualButtonPressed: Boolean = false
    /**
     * Whether the decrement virtual button is pressed.
     */
    private var mDecrementVirtualButtonPressed: Boolean = false
    /**
     * Provider to report to clients the semantic structure of this widget.
     */
    private var mAccessibilityNodeProvider: AccessibilityNodeProviderImpl? = null
    /**
     * Helper class for managing pressed state of the virtual buttons.
     */
    private var mPressedStateHelper: PressedStateHelper? = null
    /**
     * The keycode of the last handled DPAD down event.
     */
    private var mLastHandledDownDpadKeyCode = -1
    /**
     * If true then the selector wheel is hidden until the picker has focus.
     */
    private var mHideWheelUntilFocused: Boolean = false

    private var mSelectedTextColor: Int = 0
    private var mTextColor: Int = 0

    private var mTextTypeface: Typeface? = null
    private var mSelectedTextTypeface: Typeface? = null

    private var mAllowKeyboardInput: Boolean = false

    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    class TwoDigitFormatter : android.widget.NumberPicker.Formatter {
        private val mBuilder = StringBuilder()
        private var mZeroDigit: Char = ' '
        private var mFmt: java.util.Formatter? = null
        private val mArgs = arrayOfNulls<Any>(1)

        init {
            val locale = Locale.getDefault()
            init(locale)
        }

        private fun init(locale: Locale) {
            mFmt = createFormatter(locale)
            mZeroDigit = getZeroDigit(locale)
        }

        override fun format(value: Int): String {
            val currentLocale = Locale.getDefault()
            if (mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale)
            }
            mArgs[0] = value
            mBuilder.delete(0, mBuilder.length)
            mFmt?.format("%02d", *mArgs)
            return mFmt.toString()
        }

        private fun getZeroDigit(locale: Locale): Char {
            //            return LocaleData.get(locale).zeroDigit;
            return DecimalFormatSymbols(locale).zeroDigit
        }

        private fun createFormatter(locale: Locale): java.util.Formatter {
            return java.util.Formatter(mBuilder, locale)
        }
    }

    /**
     * Interface to listen for changes of the current value.
     */
    interface OnValueChangeListener {
        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int)
    }

    /**
     * Interface to listen for the picker scroll state.
     */
    interface OnScrollListener {
        /** @hide
         */
        @IntDef(SCROLL_STATE_IDLE, SCROLL_STATE_TOUCH_SCROLL, SCROLL_STATE_FLING)
        @Retention(RetentionPolicy.SOURCE)
        annotation class ScrollState

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param view The view whose scroll state is being reported.
         * @param scrollState The current scroll state. One of
         * [.SCROLL_STATE_IDLE],
         * [.SCROLL_STATE_TOUCH_SCROLL] or
         * [.SCROLL_STATE_IDLE].
         */
        fun onScrollStateChange(view: NumberPicker, @ScrollState scrollState: Int)

        companion object {
            /**
             * The view is not scrolling.
             */
            const val SCROLL_STATE_IDLE = 0
            /**
             * The user is scrolling using touch, and his finger is still on the screen.
             */
            const val SCROLL_STATE_TOUCH_SCROLL = 1
            /**
             * The user had previously been scrolling using touch and performed a fling.
             */
            const val SCROLL_STATE_FLING = 2
        }
    }

    interface Formatter2 : android.widget.NumberPicker.Formatter {
        fun formatForAccessibility(value: Int): String
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     */
    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    /**
     * Create a new number picker
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val attributesArray = context.obtainStyledAttributes(
            attrs, R.styleable.NumberPicker, defStyleAttr, defStyleRes)

        mHasSelectorWheel = true // layoutResId == DEFAULT_LAYOUT_RESOURCE_ID

        mSelectorWheelItemCount = attributesArray.getInt(
            R.styleable.NumberPicker_selectorWheelItemCount, DEFAULT_SELECTOR_WHEEL_ITEM_COUNT)
        mSelectorMiddleItemIndex = mSelectorWheelItemCount / 2
        mSelectorIndices = IntArray(mSelectorWheelItemCount)

        mAllowKeyboardInput = attributesArray.getBoolean(
            R.styleable.NumberPicker_allowKeyboardInput, true)

        mHideWheelUntilFocused = attributesArray.getBoolean(
            R.styleable.NumberPicker_hideWheelUntilFocused, false)
        mSolidColor = attributesArray.getColor(R.styleable.NumberPicker_solidColor, 0)
        mSelectionDivider = attributesArray.getDrawable(R.styleable.NumberPicker_selectionDivider)
        val defSelectionDividerHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT.toFloat(),
            resources.displayMetrics).toInt()
        mSelectionDividerHeight = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_selectionDividerHeight, defSelectionDividerHeight)
        val defSelectionDividerDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE.toFloat(),
            resources.displayMetrics).toInt()
        mSelectionDividersDistance = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_selectionDividersDistance, defSelectionDividerDistance)
        mSelectedTextColor = ContextCompat.getColor(context, R.color.uifabric_number_picker_selected_text)
        mTextColor = ContextCompat.getColor(context, R.color.uifabric_number_picker_default_text)
        mMinHeight = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED)
        mMaxHeight = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_internalMaxHeight, SIZE_UNSPECIFIED)
        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED
            && mMinHeight > mMaxHeight) {
            throw IllegalArgumentException("minHeight > maxHeight")
        }
        mMinWidth = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_internalMinWidth, SIZE_UNSPECIFIED)
        mMaxWidth = attributesArray.getDimensionPixelSize(
            R.styleable.NumberPicker_internalMaxWidth, SIZE_UNSPECIFIED)

        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED
            && mMinWidth > mMaxWidth) {
            throw IllegalArgumentException("minWidth > maxWidth")
        }
        mComputeMaxWidth = mMaxWidth == SIZE_UNSPECIFIED
        mVirtualButtonPressedDrawable = attributesArray.getDrawable(
            R.styleable.NumberPicker_virtualButtonPressedDrawable)

        val textAlign = attributesArray.getInt(
            R.styleable.NumberPicker_textAlign, ALIGN_CENTER)

        attributesArray.recycle()
        mPressedStateHelper = PressedStateHelper()
        // By default LinearLayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(!mHasSelectorWheel)
        /*val inflater = getContext().getSystemService(
            Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(layoutResId, this, true)*/
        val onClickListener = OnClickListener { v ->
            hideSoftInput()
            mInputText!!.clearFocus()
            if (v.id == R.id.uifabric_number_picker_increment) {
                changeValueByOne(true)
            } else {
                changeValueByOne(false)
            }
        }
        val onLongClickListener = OnLongClickListener { v ->
            hideSoftInput()
            mInputText!!.clearFocus()
            if (v.id == R.id.uifabric_number_picker_increment) {
                postChangeCurrentByOneFromLongPress(true, 0)
            } else {
                postChangeCurrentByOneFromLongPress(false, 0)
            }
            true
        }
        // increment button
        if (!mHasSelectorWheel) {
            mIncrementButton = findViewById<View>(R.id.uifabric_number_picker_increment) as ImageButton
            if (mIncrementButton == null) {
                mHasSelectorWheel = false
            } else {
                mIncrementButton!!.setOnClickListener(onClickListener)
                mIncrementButton!!.setOnLongClickListener(onLongClickListener)
            }
        } else {
            mIncrementButton = null
        }
        // decrement button
        if (!mHasSelectorWheel) {
            mDecrementButton = findViewById<View>(R.id.uifabric_number_picker_decrement) as ImageButton
            if (mDecrementButton == null) {
                mHasSelectorWheel = false
            } else {
                mDecrementButton!!.setOnClickListener(onClickListener)
                mDecrementButton!!.setOnLongClickListener(onLongClickListener)
            }
        } else {
            mDecrementButton = null
        }
        // input text
        mInputText = CustomEditText(context)
        mInputText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mInputText!!.selectAll()
            } else {
                mInputText!!.setSelection(0, 0)
                validateInputTextView(v)
            }
        }
        mInputText!!.filters = arrayOf<InputFilter>(InputTextFilter())
        mInputText!!.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        mInputText!!.imeOptions = EditorInfo.IME_ACTION_DONE
        val disableEditText = context !is Activity
        mInputText!!.isFocusable = !disableEditText
        mInputText!!.isFocusableInTouchMode = !disableEditText
        mInputText!!.isClickable = !disableEditText
        mInputText!!.visibility = if (mAllowKeyboardInput) View.VISIBLE else View.INVISIBLE

        // initialize constants
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumFlingVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumFlingVelocity = configuration.scaledMaximumFlingVelocity / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT
        mTextSize = mInputText!!.textSize.toInt()

        mTextTypeface = Typeface.create("sans-serif", Typeface.NORMAL)
        mSelectedTextTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        mInputText!!.typeface = mSelectedTextTypeface

        // create the selector wheel paint
        val paint = Paint()
        paint.isAntiAlias = true
        when (textAlign) {
            ALIGN_LEFT -> paint.textAlign = Align.LEFT
            ALIGN_CENTER -> paint.textAlign = Align.CENTER
            ALIGN_RIGHT -> paint.textAlign = Align.RIGHT
        }
        paint.textSize = mTextSize.toFloat()
        paint.typeface = mInputText!!.typeface
        mInputText!!.setTextColor(mSelectedTextColor)
        paint.color = mTextColor
        mSelectorWheelPaint = paint
        // create the fling and adjust scrollers
        mFlingScroller = Scroller(getContext(), null, true)
        mAdjustScroller = Scroller(getContext(), DecelerateInterpolator(2.5f))
        updateInputTextView()
        // If not explicitly specified this view is important for accessibility.
        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (!mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom)
            return
        }
        val msrdWdth = measuredWidth
        val msrdHght = measuredHeight
        // Input text centered horizontally.
        val inptTxtMsrdWdth = mInputText!!.measuredWidth
        val inptTxtMsrdHght = mInputText!!.measuredHeight
        val inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2
        val inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2
        val inptTxtRight = inptTxtLeft + inptTxtMsrdWdth
        val inptTxtBottom = inptTxtTop + inptTxtMsrdHght
        mInputText!!.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom)
        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()
            mTopSelectionDividerTop = (height - mSelectionDividersDistance) / 2 - mSelectionDividerHeight
            mBottomSelectionDividerBottom = (mTopSelectionDividerTop + 2 * mSelectionDividerHeight
                + mSelectionDividersDistance)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        // Try greedily to fit the max width and height.
        val newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth)
        val newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        // Flag if we are measured with width or height less than the respective min.
        val widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, measuredWidth,
            widthMeasureSpec)
        val heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, measuredHeight,
            heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private fun moveToFinalScrollerPosition(scroller: Scroller): Boolean {
        scroller.forceFinished(true)
        var amountToScroll = scroller.finalY - scroller.currY
        val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementHeight
        var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight
                } else {
                    overshootAdjustment += mSelectorElementHeight
                }
            }
            amountToScroll += overshootAdjustment
            scrollBy(0, amountToScroll)
            return true
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!mHasSelectorWheel || !isEnabled) {
            return false
        }
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                removeAllCallbacks()
                if (mAllowKeyboardInput) {
                    mInputText!!.visibility = View.INVISIBLE
                }
                mLastDownEventY = event.y
                mLastDownOrMoveEventY = mLastDownEventY
                mLastDownEventTime = event.eventTime
                mIgnoreMoveEvents = false
                mPerformClickOnTap = false
                // Handle pressed state before any state change.
                if (mLastDownEventY < mTopSelectionDividerTop) {
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper!!.buttonPressDelayed(
                            BUTTON_DECREMENT)
                    }
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper!!.buttonPressDelayed(
                            BUTTON_INCREMENT)
                    }
                }
                // Make sure we support flinging inside scrollables.
                parent.requestDisallowInterceptTouchEvent(true)
                if (!mFlingScroller!!.isFinished) {
                    mFlingScroller!!.forceFinished(true)
                    mAdjustScroller!!.forceFinished(true)
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                } else if (!mAdjustScroller!!.isFinished) {
                    mFlingScroller!!.forceFinished(true)
                    mAdjustScroller!!.forceFinished(true)
                } else if (mLastDownEventY < mTopSelectionDividerTop) {
                    hideSoftInput()
                    postChangeCurrentByOneFromLongPress(
                        false, ViewConfiguration.getLongPressTimeout().toLong())
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    hideSoftInput()
                    postChangeCurrentByOneFromLongPress(
                        true, ViewConfiguration.getLongPressTimeout().toLong())
                } else {
                    mPerformClickOnTap = true
                    postBeginSoftInputOnLongPressCommand()
                }
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || !mHasSelectorWheel) {
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_MOVE -> run {
                if (mIgnoreMoveEvents) {
                    return@run
                }
                val currentMoveY = event.y
                if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    val deltaDownY = Math.abs(currentMoveY - mLastDownEventY).toInt()
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks()
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    }
                } else {
                    val deltaMoveY = (currentMoveY - mLastDownOrMoveEventY).toInt()
                    scrollBy(0, deltaMoveY)
                    invalidate()
                }
                mLastDownOrMoveEventY = currentMoveY
            }
            MotionEvent.ACTION_UP -> {
                removeBeginSoftInputCommand()
                removeChangeCurrentByOneFromLongPress()
                mPressedStateHelper!!.cancel()
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                val initialVelocity = velocityTracker.yVelocity.toInt()
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity)
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                } else {
                    val eventY = event.y.toInt()
                    val deltaMoveY = Math.abs(eventY - mLastDownEventY).toInt()
                    val deltaTime = event.eventTime - mLastDownEventTime
                    if (deltaMoveY <= mTouchSlop && deltaTime < ViewConfiguration.getTapTimeout()) {
                        if (mPerformClickOnTap) {
                            mPerformClickOnTap = false
                            performClick()
                        } else {
                            val selectorIndexOffset = eventY / mSelectorElementHeight - mSelectorMiddleItemIndex
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true)
                                mPressedStateHelper!!.buttonTapped(
                                    BUTTON_INCREMENT)
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false)
                                mPressedStateHelper!!.buttonTapped(
                                    BUTTON_DECREMENT)
                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted()
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> removeAllCallbacks()
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP -> run {
                if (!mHasSelectorWheel) {
                    return@run
                }
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> if (wrapSelectorWheel || if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                            value < maxValue
                        else
                            value > minValue) {
                        requestFocus()
                        mLastHandledDownDpadKeyCode = keyCode
                        removeAllCallbacks()
                        if (mFlingScroller!!.isFinished) {
                            changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                        }
                        return true
                    }
                    KeyEvent.ACTION_UP -> if (mLastHandledDownDpadKeyCode == keyCode) {
                        mLastHandledDownDpadKeyCode = -1
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTrackballEvent(event)
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (!mHasSelectorWheel) {
            return super.dispatchHoverEvent(event)
        }
        if ((context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
            val eventY = event.y.toInt()
            val hoveredVirtualViewId: Int
            if (eventY < mTopSelectionDividerTop) {
                hoveredVirtualViewId = VIRTUAL_VIEW_ID_DECREMENT
            } else if (eventY > mBottomSelectionDividerBottom) {
                hoveredVirtualViewId = VIRTUAL_VIEW_ID_INCREMENT
            } else {
                hoveredVirtualViewId = VIRTUAL_VIEW_ID_INPUT
            }
            val action = event.actionMasked
            val provider = accessibilityNodeProvider as AccessibilityNodeProviderImpl
            when (action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                        AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
                    mLastHoveredChildVirtualViewId = hoveredVirtualViewId
                    provider.performAction(hoveredVirtualViewId,
                        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                }
                MotionEvent.ACTION_HOVER_MOVE -> {
                    if (mLastHoveredChildVirtualViewId != hoveredVirtualViewId && mLastHoveredChildVirtualViewId != View.NO_ID) {
                        provider.sendAccessibilityEventForVirtualView(
                            mLastHoveredChildVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT)
                        provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
                        mLastHoveredChildVirtualViewId = hoveredVirtualViewId
                        provider.performAction(hoveredVirtualViewId,
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                    }
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                        AccessibilityEvent.TYPE_VIEW_HOVER_EXIT)
                    mLastHoveredChildVirtualViewId = View.NO_ID
                }
            }
        }
        return false
    }

    override fun computeScroll() {
        var scroller = mFlingScroller
        if (scroller!!.isFinished) {
            scroller = mAdjustScroller
            if (scroller!!.isFinished) {
                return
            }
        }
        scroller.computeScrollOffset()
        val currentScrollerY = scroller.currY
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.startY
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY)
        mPreviousScrollerY = currentScrollerY
        if (scroller.isFinished) {
            onScrollerFinished(scroller)
        } else {
            invalidate()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!mHasSelectorWheel) {
            mIncrementButton!!.isEnabled = enabled
        }
        if (!mHasSelectorWheel) {
            mDecrementButton!!.isEnabled = enabled
        }
        mInputText!!.isEnabled = enabled
    }

    override fun scrollBy(x: Int, y: Int) {
        val selectorIndices = mSelectorIndices
        if (!wrapSelectorWheel && y > 0
            && selectorIndices!![mSelectorMiddleItemIndex] <= minValue) {
            mCurrentScrollOffset = mInitialScrollOffset
            return
        }
        if (!wrapSelectorWheel && y < 0
            && selectorIndices!![mSelectorMiddleItemIndex] >= maxValue) {
            mCurrentScrollOffset = mInitialScrollOffset
            return
        }
        mCurrentScrollOffset += y
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight
            decrementSelectorIndices(selectorIndices!!)
            setValueInternal(selectorIndices[mSelectorMiddleItemIndex], true)
            if (!wrapSelectorWheel && selectorIndices[mSelectorMiddleItemIndex] <= minValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight
            incrementSelectorIndices(selectorIndices!!)
            setValueInternal(selectorIndices[mSelectorMiddleItemIndex], true)
            if (!wrapSelectorWheel && selectorIndices[mSelectorMiddleItemIndex] >= maxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
    }

    override fun computeVerticalScrollOffset(): Int {
        return mCurrentScrollOffset
    }

    override fun computeVerticalScrollRange(): Int {
        return (maxValue - minValue + 1) * mSelectorElementHeight
    }

    override fun computeVerticalScrollExtent(): Int {
        return height
    }

    override fun getSolidColor(): Int {
        return mSolidColor
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener) {
        mOnValueChangeListener = onValueChangedListener
    }

    /**
     * Set listener to be notified for scroll state changes.
     *
     * @param onScrollListener The listener.
     */
    fun setOnScrollListener(onScrollListener: OnScrollListener) {
        mOnScrollListener = onScrollListener
    }

    /**
     * Set the formatter to be used for formatting the current value.
     *
     *
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     *
     *
     * @param formatter The formatter object. If formatter is `null`,
     * [String.valueOf] will be used.
     * @see .setDisplayedValues
     */
    fun setFormatter(formatter: android.widget.NumberPicker.Formatter) {
        if (formatter === mFormatter) {
            return
        }
        mFormatter = formatter
        initializeSelectorWheelIndices()
        updateInputTextView()
    }

    override fun performClick(): Boolean {
        if (!mHasSelectorWheel) {
            return super.performClick()
        } else if (!super.performClick()) {
            showSoftInput()
        }
        return true
    }

    override fun performLongClick(): Boolean {
        if (!mHasSelectorWheel) {
            return super.performLongClick()
        } else if (!super.performLongClick()) {
            showSoftInput()
            mIgnoreMoveEvents = true
        }
        return true
    }

    /**
     * Shows the soft input for its input text.
     */
    private fun showSoftInput() {
        if (!mAllowKeyboardInput) {
            return
        }

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null) {
            if (mHasSelectorWheel) {
                mInputText!!.visibility = View.VISIBLE
            }
            mInputText!!.requestFocus()
            inputMethodManager.showSoftInput(mInputText, 0)
        }
    }

    /**
     * Hides the soft input if it is active for the input text.
     */
    private fun hideSoftInput() {
        if (!mAllowKeyboardInput) {
            return
        }

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null && inputMethodManager.isActive(mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            if (mHasSelectorWheel) {
                mInputText!!.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private fun tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return
        }
        var maxTextWidth = 0
        if (displayedValues == null) {
            var maxDigitWidth = 0f
            for (i in 0..9) {
                val digitWidth = mSelectorWheelPaint!!.measureText(formatNumberWithLocale(i))
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth
                }
            }
            var numberOfDigits = 0
            var current = maxValue
            while (current > 0) {
                numberOfDigits++
                current = current / 10
            }
            maxTextWidth = (numberOfDigits * maxDigitWidth).toInt()
        } else {
            val valueCount = displayedValues!!.size
            for (i in 0 until valueCount) {
                val textWidth = mSelectorWheelPaint!!.measureText(displayedValues!![i])
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth.toInt()
                }
            }
        }
        maxTextWidth += mInputText!!.paddingLeft + mInputText!!.paddingRight
        if (mMaxWidth != maxTextWidth) {
            if (maxTextWidth > mMinWidth) {
                mMaxWidth = maxTextWidth
            } else {
                mMaxWidth = mMinWidth
            }
            invalidate()
        }
    }

    /**
     * Sets the speed at which the numbers be incremented and decremented when
     * the up and down buttons are long pressed respectively.
     *
     *
     * The default value is 300 ms.
     *
     *
     * @param intervalMillis The speed (in milliseconds) at which the numbers
     * will be incremented and decremented.
     */
    fun setOnLongPressUpdateInterval(intervalMillis: Long) {
        mLongPressUpdateInterval = intervalMillis
    }

    override fun getTopFadingEdgeStrength(): Float {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllCallbacks()
    }

    override fun onDraw(canvas: Canvas) {
        if (!mHasSelectorWheel) {
            super.onDraw(canvas)
            return
        }
        val showSelectorWheel = if (mHideWheelUntilFocused) hasFocus() else true
        val x: Float
        when (mSelectorWheelPaint!!.textAlign) {
            Paint.Align.LEFT -> x = ViewCompat.getPaddingStart(this).toFloat()
            Paint.Align.RIGHT -> x = (measuredWidth - ViewCompat.getPaddingEnd(this)).toFloat()
            Paint.Align.CENTER -> x = (measuredWidth / 2).toFloat()
            else -> x = (measuredWidth / 2).toFloat()
        }
        var y = mCurrentScrollOffset.toFloat()
        // draw the virtual buttons pressed state if needed
        if (showSelectorWheel && mVirtualButtonPressedDrawable != null
            && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mDecrementVirtualButtonPressed) {
                mVirtualButtonPressedDrawable!!.state = View.PRESSED_STATE_SET
                mVirtualButtonPressedDrawable!!.setBounds(0, 0, right, mTopSelectionDividerTop)
                mVirtualButtonPressedDrawable!!.draw(canvas)
            }
            if (mIncrementVirtualButtonPressed) {
                mVirtualButtonPressedDrawable!!.state = View.PRESSED_STATE_SET
                mVirtualButtonPressedDrawable!!.setBounds(0, mBottomSelectionDividerBottom, right,
                    bottom)
                mVirtualButtonPressedDrawable!!.draw(canvas)
            }
        }
        // draw the selector wheel
        val selectorIndices = mSelectorIndices
        for (i in selectorIndices!!.indices) {
            val selectorIndex = selectorIndices[i]
            val scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex)
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old value intermixed
            // with the new one.
            if (showSelectorWheel && i != mSelectorMiddleItemIndex || i == mSelectorMiddleItemIndex && mInputText!!.visibility != View.VISIBLE) {
                if (i == mSelectorMiddleItemIndex) {
                    mSelectorWheelPaint!!.color = mSelectedTextColor
                    mSelectorWheelPaint!!.typeface = mSelectedTextTypeface
                } else {
                    mSelectorWheelPaint!!.color = mTextColor
                    mSelectorWheelPaint!!.typeface = mTextTypeface
                }
                canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint!!)
            }
            y += mSelectorElementHeight.toFloat()
        }
        // draw the selection dividers
        if (showSelectorWheel && mSelectionDivider != null) {
            // draw the top divider
            val topOfTopDivider = mTopSelectionDividerTop
            val bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight
            mSelectionDivider!!.setBounds(0, topOfTopDivider, right, bottomOfTopDivider)
            mSelectionDivider!!.draw(canvas)
            // draw the bottom divider
            val bottomOfBottomDivider = mBottomSelectionDividerBottom
            val topOfBottomDivider = bottomOfBottomDivider - mSelectionDividerHeight
            mSelectionDivider!!.setBounds(0, topOfBottomDivider, right, bottomOfBottomDivider)
            mSelectionDivider!!.draw(canvas)
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = NumberPicker::class.java.name
        event.isScrollable = true
        event.scrollY = (minValue + mValue) * mSelectorElementHeight
        event.maxScrollY = (maxValue - minValue) * mSelectorElementHeight
    }

    override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider? {
        if (!mHasSelectorWheel) {
            return super.getAccessibilityNodeProvider()
        }
        if (mAccessibilityNodeProvider == null) {
            mAccessibilityNodeProvider = AccessibilityNodeProviderImpl()
        }
        return mAccessibilityNodeProvider
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private fun makeMeasureSpec(measureSpec: Int, maxSize: Int): Int {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec
        }
        val size = View.MeasureSpec.getSize(measureSpec)
        val mode = View.MeasureSpec.getMode(measureSpec)
        when (mode) {
            View.MeasureSpec.EXACTLY -> return measureSpec
            View.MeasureSpec.AT_MOST -> return View.MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), View.MeasureSpec.EXACTLY)
            View.MeasureSpec.UNSPECIFIED -> return View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY)
            else -> throw IllegalArgumentException("Unknown measure mode: $mode")
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec The current measure spec.
     * @return The resolved size and state.
     */
    private fun resolveSizeAndStateRespectingMinSize(
        minSize: Int, measuredSize: Int, measureSpec: Int): Int {
        if (minSize != SIZE_UNSPECIFIED) {
            val desiredWidth = Math.max(minSize, measuredSize)
            return View.resolveSizeAndState(desiredWidth, measureSpec, 0)
        } else {
            return measuredSize
        }
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private fun initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear()
        val selectorIndices = mSelectorIndices
        val current = value
        for (i in mSelectorIndices!!.indices) {
            var selectorIndex = current + (i - mSelectorMiddleItemIndex)
            if (wrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices?.let {
                it[i] = selectorIndex
                ensureCachedScrollSelectorValue(it[i])
            }

        }
    }

    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private fun setValueInternal(current: Int, notifyChange: Boolean) {
        var current = current
        if (mValue == current) {
            return
        }
        // Wrap around the values if we go past the start or end
        if (wrapSelectorWheel) {
            current = getWrappedSelectorIndex(current)
        } else {
            current = Math.max(current, minValue)
            current = Math.min(current, maxValue)
        }
        val previous = mValue
        mValue = current
        updateInputTextView()
        if (notifyChange) {
            notifyChange(previous, current)
        }
        initializeSelectorWheelIndices()
        invalidate()
    }

    fun animateValueTo(value: Int) {
        if (mValue == value) {
            return
        }
        changeValueBy(value - mValue, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
    }

    /**
     * Jump to a value near target value then animate to target value
     *
     * @param value The target value
     */
    fun quicklyAnimateValueTo(value: Int) {
        if (mValue == value) {
            return
        }

        val delta = mValue - value
        if (delta > QUICK_ANIMATE_THRESHOLD) {
            this.value = value + QUICK_ANIMATE_THRESHOLD
        } else if (delta < -QUICK_ANIMATE_THRESHOLD) {
            this.value = value - QUICK_ANIMATE_THRESHOLD
        }

        animateValueTo(value)
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    private fun changeValueByOne(increment: Boolean) {
        changeValueBy(if (increment) 1 else -1, SNAP_SCROLL_DURATION)
    }

    private fun changeValueBy(value: Int, duration: Int) {
        if (mHasSelectorWheel) {
            mInputText!!.visibility = View.INVISIBLE
            if (!moveToFinalScrollerPosition(mFlingScroller!!)) {
                moveToFinalScrollerPosition(mAdjustScroller!!)
            }
            mPreviousScrollerY = 0
            mFlingScroller!!.startScroll(0, 0, 0, value * -mSelectorElementHeight, duration)
            invalidate()
        } else {
            setValueInternal(mValue + value, true)
        }
    }

    private fun initializeSelectorWheel() {
        initializeSelectorWheelIndices()
        val selectorIndices = mSelectorIndices
        val totalTextHeight = selectorIndices!!.size * mTextSize
        val totalTextGapHeight = (bottom - top - totalTextHeight).toFloat()
        val textGapCount = selectorIndices.size.toFloat()
        mSelectorTextGapHeight = (totalTextGapHeight / textGapCount + 0.5f).toInt()
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight
        // Ensure that the middle item is positioned the same as the text in
        // mInputText
        val editTextTextPosition = mInputText!!.baseline + mInputText!!.top
        mInitialScrollOffset = editTextTextPosition - mSelectorElementHeight * mSelectorMiddleItemIndex
        mCurrentScrollOffset = mInitialScrollOffset
        updateInputTextView()
    }

    private fun initializeFadingEdges() {
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength((bottom - top - mTextSize) / 2)
    }

    /**
     * Callback invoked upon completion of a given `scroller`.
     */
    private fun onScrollerFinished(scroller: Scroller?) {
        if (scroller == mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView()
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else {
            if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                updateInputTextView()
            }
        }
    }

    /**
     * Handles transition to a given `scrollState`
     */
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        if (mOnScrollListener != null) {
            mOnScrollListener!!.onScrollStateChange(this, scrollState)
        }
    }

    /**
     * Flings the selector with the given `velocityY`.
     */
    private fun fling(velocityY: Int) {
        mPreviousScrollerY = 0
        if (velocityY > 0) {
            mFlingScroller!!.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE)
        } else {
            mFlingScroller!!.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE)
        }
        invalidate()
    }

    /**
     * @return The wrapped index `selectorIndex` value.
     */
    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > maxValue) {
            return minValue + (selectorIndex - maxValue) % (maxValue - minValue) - 1
        } else if (selectorIndex < minValue) {
            return maxValue - (minValue - selectorIndex) % (maxValue - minValue) + 1
        }
        return selectorIndex
    }

    /**
     * Increments the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (wrapSelectorWheel && nextScrollSelectorIndex > maxValue) {
            nextScrollSelectorIndex = minValue
        }
        selectorIndices[selectorIndices.size - 1] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Decrements the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun decrementSelectorIndices(selectorIndices: IntArray) {
        for (i in selectorIndices.size - 1 downTo 1) {
            selectorIndices[i] = selectorIndices[i - 1]
        }
        var nextScrollSelectorIndex = selectorIndices[1] - 1
        if (wrapSelectorWheel && nextScrollSelectorIndex < minValue) {
            nextScrollSelectorIndex = maxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Ensures we have a cached string representation of the given `
     * selectorIndex` to avoid multiple instantiations of the same string.
     */
    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = mSelectorIndexToStringCache
        var scrollSelectorValue: String? = cache.get(selectorIndex)
        if (scrollSelectorValue != null) {
            return
        }
        if (selectorIndex < minValue || selectorIndex > maxValue) {
            scrollSelectorValue = ""
        } else {
            if (displayedValues != null) {
                val displayedValueIndex = selectorIndex - minValue
                scrollSelectorValue = displayedValues!![displayedValueIndex]
            } else {
                scrollSelectorValue = formatNumber(selectorIndex)
            }
        }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String {
        return if (mFormatter != null) mFormatter!!.format(value) else formatNumberWithLocale(value)
    }

    private fun formatNumberForAccessibility(value: Int): String {
        return if (mFormatter != null)
            if (mFormatter is Formatter2)
                (mFormatter as Formatter2).formatForAccessibility(value)
            else
                mFormatter!!.format(value)
        else
            formatNumberWithLocale(value)
    }

    private fun validateInputTextView(v: View) {
        val str = (v as TextView).text.toString()
        if (TextUtils.isEmpty(str)) {
            // Restore to the old value as we don't allow empty values
            updateInputTextView()
        } else {
            // Check the new value and ensure it's in range
            val current = getSelectedPos(str)
            setValueInternal(current, true)
        }
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in [.setFormatter]
     * will be used to format the number.
     *
     * @return Whether the text was updated.
     */
    private fun updateInputTextView(): Boolean {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        val text = if (displayedValues == null)
            formatNumber(mValue)
        else
            displayedValues!![mValue - minValue]
        if (!TextUtils.isEmpty(text) && text != mInputText!!.text.toString()) {
            mInputText!!.setText(text)
            return true
        }
        return false
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private fun notifyChange(previous: Int, current: Int) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener!!.onValueChange(this, previous, mValue)
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private fun postChangeCurrentByOneFromLongPress(increment: Boolean, delayMillis: Long) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        mChangeCurrentByOneFromLongPressCommand!!.setStep(increment)
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis)
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private fun removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
    }

    /**
     * Posts a command for beginning an edit of the current value via IME on
     * long press.
     */
    private fun postBeginSoftInputOnLongPressCommand() {
        if (mBeginSoftInputOnLongPressCommand == null) {
            mBeginSoftInputOnLongPressCommand = BeginSoftInputOnLongPressCommand()
        } else {
            removeCallbacks(mBeginSoftInputOnLongPressCommand)
        }
        postDelayed(mBeginSoftInputOnLongPressCommand, ViewConfiguration.getLongPressTimeout().toLong())
    }

    /**
     * Removes the command for beginning an edit of the current value via IME.
     */
    private fun removeBeginSoftInputCommand() {
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand)
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private fun removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        if (mSetSelectionCommand != null) {
            removeCallbacks(mSetSelectionCommand)
        }
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand)
        }
        mPressedStateHelper!!.cancel()
    }

    /**
     * @return The selected index given its displayed `value`.
     */
    private fun getSelectedPos(value: String): Int {
        var value = value
        if (displayedValues == null) {
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        } else {
            for (i in displayedValues!!.indices) {
                // Don't force the user to type in jan when ja will do
                value = value.toLowerCase()
                if (displayedValues!![i].toLowerCase().startsWith(value)) {
                    return minValue + i
                }
            }
            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            try {
                return Integer.parseInt(value)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        }
        return minValue
    }

    /**
     * Posts an [SetSelectionCommand] from the given `selectionStart
    ` *  to `selectionEnd`.
     */
    private fun postSetSelectionCommand(selectionStart: Int, selectionEnd: Int) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = SetSelectionCommand()
        } else {
            removeCallbacks(mSetSelectionCommand)
        }
        mSetSelectionCommand!!.mSelectionStart = selectionStart
        mSetSelectionCommand!!.mSelectionEnd = selectionEnd
        post(mSetSelectionCommand)
    }

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    internal inner class InputTextFilter : NumberKeyListener() {
        // XXX This doesn't allow for range limits when controlled by a
        // soft input method!
        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_TEXT
        }

        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        override fun filter(
            source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            if (displayedValues == null) {
                var filtered: CharSequence? = super.filter(source, start, end, dest, dstart, dend)
                if (filtered == null) {
                    filtered = source.subSequence(start, end)
                }
                val result = (dest.subSequence(0, dstart).toString() + filtered
                    + dest.subSequence(dend, dest.length))
                if ("" == result) {
                    return result
                }
                val `val` = getSelectedPos(result)
                /*
                 * Ensure the user can't type in a value greater than the max
                 * allowed. We have to allow less than min as the user might
                 * want to delete some numbers and then type a new number.
                 * And prevent multiple-"0" that exceeds the length of upper
                 * bound number.
                 */
                return if (`val` > maxValue || result.length > maxValue.toString().length) {
                    ""
                } else {
                    filtered
                }
            } else {
                val filtered = source.subSequence(start, end).toString()
                if (TextUtils.isEmpty(filtered)) {
                    return ""
                }
                val result = (dest.subSequence(0, dstart).toString() + filtered
                    + dest.subSequence(dend, dest.length))
                val str = result.toLowerCase()
                for (`val` in displayedValues!!) {
                    val valLowerCase = `val`.toLowerCase()
                    if (valLowerCase.startsWith(str)) {
                        postSetSelectionCommand(result.length, `val`.length)
                        return `val`.subSequence(dstart, `val`.length)
                    }
                }
                return ""
            }
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     *
     * @return Whether an adjustment has been made.
     */
    private fun ensureScrollWheelAdjusted(): Boolean {
        // adjust to the closest value
        var deltaY = mInitialScrollOffset - mCurrentScrollOffset
        if (deltaY != 0) {
            mPreviousScrollerY = 0
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += if (deltaY > 0) -mSelectorElementHeight else mSelectorElementHeight
            }
            mAdjustScroller!!.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            invalidate()
            return true
        }
        return false
    }

    internal inner class PressedStateHelper : Runnable {
        private val MODE_PRESS = 1
        private val MODE_TAPPED = 2
        private var mManagedButton: Int = 0
        private var mMode: Int = 0
        fun cancel() {
            mMode = 0
            mManagedButton = 0
            this@NumberPicker.removeCallbacks(this)
            if (mIncrementVirtualButtonPressed) {
                mIncrementVirtualButtonPressed = false
                invalidate(0, mBottomSelectionDividerBottom, right, bottom)
            }
            mDecrementVirtualButtonPressed = false
            if (mDecrementVirtualButtonPressed) {
                invalidate(0, 0, right, mTopSelectionDividerTop)
            }
        }

        fun buttonPressDelayed(button: Int) {
            cancel()
            mMode = MODE_PRESS
            mManagedButton = button
            this@NumberPicker.postDelayed(this, ViewConfiguration.getTapTimeout().toLong())
        }

        fun buttonTapped(button: Int) {
            cancel()
            mMode = MODE_TAPPED
            mManagedButton = button
            this@NumberPicker.post(this)
        }

        override fun run() {
            when (mMode) {
                MODE_PRESS -> {
                    when (mManagedButton) {
                        BUTTON_INCREMENT -> {
                            mIncrementVirtualButtonPressed = true
                            invalidate(0, mBottomSelectionDividerBottom, right, bottom)
                        }
                        BUTTON_DECREMENT -> {
                            mDecrementVirtualButtonPressed = true
                            invalidate(0, 0, right, mTopSelectionDividerTop)
                        }
                    }
                }
                MODE_TAPPED -> {
                    when (mManagedButton) {
                        BUTTON_INCREMENT -> {
                            if (!mIncrementVirtualButtonPressed) {
                                this@NumberPicker.postDelayed(this,
                                    ViewConfiguration.getPressedStateDuration().toLong())
                            }
                            mIncrementVirtualButtonPressed = mIncrementVirtualButtonPressed xor true
                            invalidate(0, mBottomSelectionDividerBottom, right, bottom)
                        }
                        BUTTON_DECREMENT -> {
                            if (!mDecrementVirtualButtonPressed) {
                                this@NumberPicker.postDelayed(this,
                                    ViewConfiguration.getPressedStateDuration().toLong())
                            }
                            mDecrementVirtualButtonPressed = mDecrementVirtualButtonPressed xor true
                            invalidate(0, 0, right, mTopSelectionDividerTop)
                        }
                    }
                }
            }
        }
    }

    /**
     * Command for setting the input text selection.
     */
    internal inner class SetSelectionCommand : Runnable {
        var mSelectionStart: Int = 0
        var mSelectionEnd: Int = 0
        override fun run() {
            mInputText!!.setSelection(mSelectionStart, mSelectionEnd)
        }
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    internal inner class ChangeCurrentByOneFromLongPressCommand : Runnable {
        private var mIncrement: Boolean = false
        fun setStep(increment: Boolean) {
            mIncrement = increment
        }

        override fun run() {
            changeValueByOne(mIncrement)
            postDelayed(this, mLongPressUpdateInterval)
        }
    }

    /**
     * @hide
     */
    class CustomEditText : AppCompatEditText {

        @JvmOverloads
        constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
            maxLines = 1
            background = null
        }

        override fun onEditorAction(actionCode: Int) {
            super.onEditorAction(actionCode)
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                clearFocus()
            }
        }

        override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(event)
            if (context !is Activity) {
                event.className = View::class.java.name
            }
        }

        override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(info)
            if (context !is Activity) {
                info.className = View::class.java.name
            }
        }
    }

    /**
     * Command for beginning soft input on long press.
     */
    internal inner class BeginSoftInputOnLongPressCommand : Runnable {
        override fun run() {
            performLongClick()
        }
    }

    /**
     * Class for managing virtual view tree rooted at this picker.
     */
    internal inner class AccessibilityNodeProviderImpl : AccessibilityNodeProvider() {
        private val mTempRect = Rect()
        private val mTempArray = IntArray(2)
        private var mAccessibilityFocusedView = UNDEFINED
        private val virtualDecrementButtonText: String?
            get() {
                var value = mValue - 1
                if (wrapSelectorWheel) {
                    value = getWrappedSelectorIndex(value)
                }
                return if (value >= minValue) {
                    if (displayedValues == null)
                        formatNumberForAccessibility(value)
                    else
                        displayedValues!![value - minValue]
                } else null
            }
        private val virtualIncrementButtonText: String?
            get() {
                var value = mValue + 1
                if (wrapSelectorWheel) {
                    value = getWrappedSelectorIndex(value)
                }
                return if (value <= maxValue) {
                    if (displayedValues == null)
                        formatNumberForAccessibility(value)
                    else
                        displayedValues!![value - minValue]
                } else null
            }

        override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo {
            when (virtualViewId) {
                View.NO_ID -> return createAccessibilityNodeInfoForNumberPicker(scrollX, scrollY,
                    scrollX + (right - left), scrollY + (bottom - top))
                VIRTUAL_VIEW_ID_DECREMENT -> return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_DECREMENT,
                    virtualDecrementButtonText, scrollX, scrollY,
                    scrollX + (right - left),
                    mTopSelectionDividerTop + mSelectionDividerHeight)
                VIRTUAL_VIEW_ID_INPUT -> return createAccessibilityNodeInfoForInputText(scrollX,
                    mTopSelectionDividerTop + mSelectionDividerHeight,
                    scrollX + (right - left),
                    mBottomSelectionDividerBottom - mSelectionDividerHeight)
                VIRTUAL_VIEW_ID_INCREMENT -> return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_INCREMENT,
                    virtualIncrementButtonText, scrollX,
                    mBottomSelectionDividerBottom - mSelectionDividerHeight,
                    scrollX + (right - left), scrollY + (bottom - top))
            }
            return super.createAccessibilityNodeInfo(virtualViewId)
        }

        override fun findAccessibilityNodeInfosByText(searched: String,
                                                      virtualViewId: Int): List<AccessibilityNodeInfo> {
            if (TextUtils.isEmpty(searched)) {
                return emptyList()
            }
            val searchedLowerCase = searched.toLowerCase()
            val result = ArrayList<AccessibilityNodeInfo>()
            when (virtualViewId) {
                View.NO_ID -> {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_DECREMENT, result)
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_INPUT, result)
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_INCREMENT, result)
                    return result
                }
                VIRTUAL_VIEW_ID_DECREMENT, VIRTUAL_VIEW_ID_INCREMENT, VIRTUAL_VIEW_ID_INPUT -> {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId,
                        result)
                    return result
                }
            }
            return super.findAccessibilityNodeInfosByText(searched, virtualViewId)
        }

        override fun performAction(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            when (virtualViewId) {
                View.NO_ID -> {
                    when (action) {
                        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS -> {
                            run {
                                if (mAccessibilityFocusedView != virtualViewId) {
                                    mAccessibilityFocusedView = virtualViewId
                                    //                                requestAccessibilityFocus();
                                    performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
                                    return true
                                }
                            }
                            return false
                        }
                        AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS -> {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED
                                //                                clearAccessibilityFocus();
                                performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null)
                                return true
                            }
                            return false
                        }
                        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> {
                            run {
                                if (this@NumberPicker.isEnabled && (wrapSelectorWheel || value < maxValue)) {
                                    changeValueByOne(true)
                                    return true
                                }
                            }
                            return false
                        }
                        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> {
                            run {
                                if (this@NumberPicker.isEnabled && (wrapSelectorWheel || value > minValue)) {
                                    changeValueByOne(false)
                                    return true
                                }
                            }
                            return false
                        }
                    }
                }
                VIRTUAL_VIEW_ID_INPUT -> {
                    run {
                        when (action) {
                            AccessibilityNodeInfo.ACTION_FOCUS -> {
                                if (this@NumberPicker.isEnabled && !mInputText!!.isFocused) {
                                    return mInputText!!.requestFocus()
                                }
                            }
                            AccessibilityNodeInfo.ACTION_CLEAR_FOCUS -> {
                                if (this@NumberPicker.isEnabled && mInputText!!.isFocused) {
                                    mInputText!!.clearFocus()
                                    return true
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_CLICK -> {
                                if (this@NumberPicker.isEnabled) {
                                    performClick()
                                    return true
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_LONG_CLICK -> {
                                if (this@NumberPicker.isEnabled) {
                                    performLongClick()
                                    return true
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView != virtualViewId) {
                                        mAccessibilityFocusedView = virtualViewId
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                                        mInputText!!.invalidate()
                                        return true
                                    }
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView == virtualViewId) {
                                        mAccessibilityFocusedView = UNDEFINED
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
                                        mInputText!!.invalidate()
                                        return true
                                    }
                                }
                                return false
                            }
                            else -> {
                                return mInputText!!.performAccessibilityAction(action, arguments)
                            }
                        }
                    }
                    return false
                }
                VIRTUAL_VIEW_ID_INCREMENT -> {
                    run {
                        when (action) {
                            AccessibilityNodeInfo.ACTION_CLICK -> {
                                run {
                                    if (this@NumberPicker.isEnabled) {
                                        this@NumberPicker.changeValueByOne(true)
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_CLICKED)
                                        return true
                                    }
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView != virtualViewId) {
                                        mAccessibilityFocusedView = virtualViewId
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                                        invalidate(0, mBottomSelectionDividerBottom, right, bottom)
                                        return true
                                    }
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView == virtualViewId) {
                                        mAccessibilityFocusedView = UNDEFINED
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
                                        invalidate(0, mBottomSelectionDividerBottom, right, bottom)
                                        return true
                                    }
                                }
                                return false
                            }
                            else -> {
                                return false
                            }
                        }
                    }
                    return false
                }
                VIRTUAL_VIEW_ID_DECREMENT -> {
                    run {
                        when (action) {
                            AccessibilityNodeInfo.ACTION_CLICK -> {
                                run {
                                    if (this@NumberPicker.isEnabled) {
                                        val increment = virtualViewId == VIRTUAL_VIEW_ID_INCREMENT
                                        this@NumberPicker.changeValueByOne(increment)
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_CLICKED)
                                        return true
                                    }
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView != virtualViewId) {
                                        mAccessibilityFocusedView = virtualViewId
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
                                        invalidate(0, 0, right, mTopSelectionDividerTop)
                                        return true
                                    }
                                }
                                return false
                            }
                            AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS -> {
                                run {
                                    if (mAccessibilityFocusedView == virtualViewId) {
                                        mAccessibilityFocusedView = UNDEFINED
                                        sendAccessibilityEventForVirtualView(virtualViewId,
                                            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED)
                                        invalidate(0, 0, right, mTopSelectionDividerTop)
                                        return true
                                    }
                                }
                                return false
                            }
                            else -> {
                                return false
                            }
                        }
                    }
                    return false
                }
            }
            return super.performAction(virtualViewId, action, arguments)
        }

        fun sendAccessibilityEventForVirtualView(virtualViewId: Int, eventType: Int) {
            when (virtualViewId) {
                VIRTUAL_VIEW_ID_DECREMENT -> {
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                            virtualDecrementButtonText)
                    }
                }
                VIRTUAL_VIEW_ID_INPUT -> {
                    sendAccessibilityEventForVirtualText(eventType)
                }
                VIRTUAL_VIEW_ID_INCREMENT -> {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                            virtualIncrementButtonText)
                    }
                }
            }
        }

        private fun sendAccessibilityEventForVirtualText(eventType: Int) {
            if ((context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
                val event = AccessibilityEvent.obtain(eventType)
                mInputText!!.onInitializeAccessibilityEvent(event)
                mInputText!!.onPopulateAccessibilityEvent(event)
                event.setSource(this@NumberPicker, VIRTUAL_VIEW_ID_INPUT)
                requestSendAccessibilityEvent(this@NumberPicker, event)
            }
        }

        private fun sendAccessibilityEventForVirtualButton(virtualViewId: Int, eventType: Int,
                                                           text: String?) {
            if ((context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
                val event = AccessibilityEvent.obtain(eventType)
                event.className = Button::class.java.name
                event.packageName = context.packageName
                event.text.add(text)
                event.isEnabled = this@NumberPicker.isEnabled
                event.setSource(this@NumberPicker, virtualViewId)
                requestSendAccessibilityEvent(this@NumberPicker, event)
            }
        }

        private fun findAccessibilityNodeInfosByTextInChild(searchedLowerCase: String,
                                                            virtualViewId: Int, outResult: MutableList<AccessibilityNodeInfo>) {
            when (virtualViewId) {
                VIRTUAL_VIEW_ID_DECREMENT -> {
                    run {
                        val text = virtualDecrementButtonText
                        if (!TextUtils.isEmpty(text) && text!!.toString().toLowerCase().contains(searchedLowerCase)) {
                            outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_DECREMENT))
                        }
                    }
                    return
                }
                VIRTUAL_VIEW_ID_INPUT -> {
                    val text = mInputText!!.text
                    if (!TextUtils.isEmpty(text) && text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT))
                        return
                    }
                    val contentDesc = mInputText!!.text
                    if (!TextUtils.isEmpty(contentDesc) && contentDesc.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT))
                        return
                    }
                }
                VIRTUAL_VIEW_ID_INCREMENT -> {
                    run {
                        val text = virtualIncrementButtonText
                        if (!TextUtils.isEmpty(text) && text!!.toString().toLowerCase().contains(searchedLowerCase)) {
                            outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INCREMENT))
                        }
                    }
                    return
                }
            }
        }

        private fun createAccessibilityNodeInfoForInputText(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ): AccessibilityNodeInfo {
            val info = mInputText!!.createAccessibilityNodeInfo()
            info.setSource(this@NumberPicker, VIRTUAL_VIEW_ID_INPUT)
            if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            }
            if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
            }
            val boundsInParent = mTempRect
            boundsInParent.set(left, top, right, bottom)
            //          TODO:  info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent)
            val locationOnScreen = mTempArray
            getLocationOnScreen(locationOnScreen)
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1])
            info.setBoundsInScreen(boundsInParent)
            info.text = formatNumberForAccessibility(mValue)
            info.contentDescription = formatNumberForAccessibility(mValue)
            info.isEnabled = mInputText!!.isEnabled
            return info
        }

        private fun createAccessibilityNodeInfoForVirtualButton(
            virtualViewId: Int,
            text: String?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ): AccessibilityNodeInfo {
            val info = AccessibilityNodeInfo.obtain()
            info.className = Button::class.java.name
            info.packageName = context.packageName
            info.setSource(this@NumberPicker, virtualViewId)
            info.setParent(this@NumberPicker)
            info.text = text
            info.isClickable = true
            info.isLongClickable = true
            info.isEnabled = this@NumberPicker.isEnabled
            val boundsInParent = mTempRect
            boundsInParent.set(left, top, right, bottom)
            //          TODO:  info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent)
            val locationOnScreen = mTempArray
            getLocationOnScreen(locationOnScreen)
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1])
            info.setBoundsInScreen(boundsInParent)
            if (mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            }
            if (mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
            }
            if (this@NumberPicker.isEnabled) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            return info
        }

        private fun createAccessibilityNodeInfoForNumberPicker(left: Int, top: Int,
                                                               right: Int, bottom: Int): AccessibilityNodeInfo {
            val info = AccessibilityNodeInfo.obtain()
            info.className = NumberPicker::class.java.name
            info.packageName = context.packageName
            info.setSource(this@NumberPicker)
            if (hasVirtualDecrementButton()) {
                info.addChild(this@NumberPicker, VIRTUAL_VIEW_ID_DECREMENT)
            }
            info.addChild(this@NumberPicker, VIRTUAL_VIEW_ID_INPUT)
            if (hasVirtualIncrementButton()) {
                info.addChild(this@NumberPicker, VIRTUAL_VIEW_ID_INCREMENT)
            }
            info.setParent(parentForAccessibility as View)
            info.isEnabled = this@NumberPicker.isEnabled
            info.isScrollable = true
            // TODO:
            //            final float applicationScale =
            //                    getContext().getResources().getCompatibilityInfo().applicationScale;
            //            Rect boundsInParent = mTempRect;
            //            boundsInParent.set(left, top, right, bottom);
            //            boundsInParent.scale(applicationScale);
            //            info.setBoundsInParent(boundsInParent);
            //            info.setVisibleToUser(isVisibleToUser());
            //            Rect boundsInScreen = boundsInParent;
            //            int[] locationOnScreen = mTempArray;
            //            getLocationOnScreen(locationOnScreen);
            //            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            //            boundsInScreen.scale(applicationScale);
            //            info.setBoundsInScreen(boundsInScreen);
            if (mAccessibilityFocusedView != View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
            }
            if (mAccessibilityFocusedView == View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
            }
            if (this@NumberPicker.isEnabled) {
                if (wrapSelectorWheel || value < maxValue) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                }
                if (wrapSelectorWheel || value > minValue) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                }
            }
            return info
        }

        private fun hasVirtualDecrementButton(): Boolean {
            return wrapSelectorWheel || value > minValue
        }

        private fun hasVirtualIncrementButton(): Boolean {
            return wrapSelectorWheel || value < maxValue
        }
    }
}
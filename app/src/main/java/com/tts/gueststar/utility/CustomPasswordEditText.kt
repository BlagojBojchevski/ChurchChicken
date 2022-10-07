package com.tts.gueststar.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatEditText
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.tts.gueststar.R

open class CustomPasswordEditText : AppCompatEditText, View.OnTouchListener,
    View.OnFocusChangeListener,
    CustomTextWatcher.TextWatcherListener {

    private var isVisible = false
    private var loc: Location? = Location.RIGHT

    private var xD: Drawable? = null
    private var listener: Listener? = null

    private var l: OnTouchListener? = null
    private var f: OnFocusChangeListener? = null

    private val displayedDrawable: Drawable?
        get() = if (loc != null) compoundDrawables[loc!!.idx] else null

    enum class Location(internal val idx: Int) {
        LEFT(0), RIGHT(2)
    }

    interface Listener {
        fun didClearText()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    override fun setOnTouchListener(l: OnTouchListener) {
        this.l = l
    }

    override fun setOnFocusChangeListener(f: OnFocusChangeListener) {
        this.f = f
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (displayedDrawable != null) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val left = if (loc == Location.LEFT) 0 else width - paddingRight - xD!!.intrinsicWidth
            val right = if (loc == Location.LEFT) paddingLeft + xD!!.intrinsicWidth else width
            val tappedX = x in left..right && y >= 0 && y <= bottom - top
            if (tappedX) {
                if (event.action == MotionEvent.ACTION_UP) {
                    // setText("")
                    if (isVisible) {
                        transformationMethod = PasswordTransformationMethod()
                        isVisible = false
                        this.setSelection(this.length())
                        setClearIconVisible(false)
                        init1()
                    } else {
                        transformationMethod = null
                        isVisible = true
                        this.setSelection(this.length())
                        setClearIconVisible(false)
                        init1()
                    }

                    if (listener != null) {
                        listener!!.didClearText()
                    }
                }
                return true
            }
        }
        return if (l != null) {
            l!!.onTouch(v, event)
        } else false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
     //   setClearIconVisible(true)
        if (f != null) {
            f!!.onFocusChange(v, hasFocus)
        }
    }

    override fun onTextChanged(view: EditText, text: String) {
        val maxLength = AppConstants.maxPasswordCharacters
        val filterArea = arrayOfNulls<InputFilter>(1)
        filterArea[0] = InputFilter.LengthFilter(maxLength)
        view.filters = arrayOf(Engine().spaceFilter, filterArea[0])
        if (isFocused) {
      //      setClearIconVisible(true)
            if (Engine().filterValidPassword(view)) {
                if (view.isFocused)
                    view.setBackgroundResource(0)
            } else {
                view.setBackgroundResource(R.drawable.edit_text_style_red)
            }
        }

    }


    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        super.setCompoundDrawables(left, top, right, bottom)
        initIconOff()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        addTextChangedListener(CustomTextWatcher(this, this))
        if (isVisible)
            initIconOn()
        else
            initIconOff()
       // setClearIconVisible(true)
        val maxLength = AppConstants.maxPasswordCharacters
        val filterArea = arrayOfNulls<InputFilter>(1)
        filterArea[0] = InputFilter.LengthFilter(maxLength)
        super.setFilters(arrayOf(Engine().spaceFilter, filterArea[0]))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init1() {
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        addTextChangedListener(CustomTextWatcher(this, this))
        if (isVisible)
            initIconOn()
        else
            initIconOff()
     //   setClearIconVisible(true)
    }

    private fun initIconOff() {
        xD = null
        if (loc != null) {
            xD = compoundDrawables[loc!!.idx]
        }
        if (xD == null) {
            xD = resources.getDrawable(R.drawable.eye_on, context.theme)
        }
        xD!!.setBounds(0, 0, xD!!.intrinsicWidth, xD!!.intrinsicHeight)
        val min = paddingTop + xD!!.intrinsicHeight + paddingBottom
        if (suggestedMinimumHeight < min) {
            minimumHeight = min
        }
    }

    private fun initIconOn() {
        xD = null
        if (loc != null) {
            xD = compoundDrawables[loc!!.idx]
        }
        if (xD == null) {
            xD = resources.getDrawable(R.drawable.eye_off, context.theme)
        }
        xD!!.setBounds(0, 0, xD!!.intrinsicWidth, xD!!.intrinsicHeight)
        val min = paddingTop + xD!!.intrinsicHeight + paddingBottom
        if (suggestedMinimumHeight < min) {
            minimumHeight = min
        }
    }

    private fun setClearIconVisible(visible: Boolean) {
        val cd = compoundDrawables
        val displayed = displayedDrawable
        val wasVisible = displayed != null
        if (visible != wasVisible) {
            val x = if (visible) xD else null
            super.setCompoundDrawables(
                if (loc == Location.LEFT) x else cd[0],
                cd[1],
                if (loc == Location.RIGHT) x else cd[2],
                cd[3]
            )
        }
    }
}
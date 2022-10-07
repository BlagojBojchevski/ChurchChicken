package com.tts.gueststar.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import com.tts.gueststar.R

class CustomUnderlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    private var lineBoundsRect: Rect? = null
    private var underlinePaint: Paint? = null
    private  var  mStrokeWidth: Float = 0.0f

    var underLineColor: Int
        @ColorInt
        get() = underlinePaint!!.color
        set(@ColorInt mColor) {
            underlinePaint!!.color = mColor
            invalidate()
        }

    var underlineWidth: Float
        get() {
            return underlinePaint!!.strokeWidth
        }
        set(mStrokeWidth) {
            underlinePaint!!.strokeWidth = mStrokeWidth
            invalidate()
        }

    init {
        init(context, attrs, defStyleAttr)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attributeSet: AttributeSet?, defStyle: Int) {

        val density = context.resources.displayMetrics.density



        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.UnderlinedTextView, defStyle, 0)
        val mColor = typedArray.getColor(R.styleable.UnderlinedTextView_underlineColor, -0x10000)
        mStrokeWidth = typedArray.getDimension(R.styleable.UnderlinedTextView_underlineWidth, density * 2)


        typedArray.recycle()

        lineBoundsRect = Rect()
        underlinePaint = Paint()
        underlinePaint!!.style = Paint.Style.STROKE
        underlinePaint!!.color = mColor //color of the underline
        underlinePaint!!.strokeWidth = mStrokeWidth


    }

    override fun onDraw(canvas: Canvas) {


        val layout = layout
        var x_start: Float
        var x_stop: Float
        var x_diff: Float
        var firstCharInLine: Int

        for (i in 0 until 1) {
            val baseline = getLineBounds(i, lineBoundsRect)
            firstCharInLine = layout.getLineStart(i)

            x_start = layout.getPrimaryHorizontal(firstCharInLine)
            x_diff = layout.getPrimaryHorizontal(firstCharInLine + 1) - x_start
            x_stop = layout.getPrimaryHorizontal(1) + x_diff

            canvas.drawLine(x_start, 5 + baseline + mStrokeWidth, x_stop, 5 + baseline + mStrokeWidth, underlinePaint!!)
        }

        super.onDraw(canvas)
    }
}

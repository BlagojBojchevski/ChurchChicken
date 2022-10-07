package com.tts.gueststar.utility

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax : InputFilter {

    private var min: Double = 0.0
    private var max: Double = 0.0

    constructor(min: Double, max: Double) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = min.toDouble()
        this.max = max.toDouble()
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toDouble()
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Double, b: Double, c: Double): Boolean {
        return if (b > a) c >= a && c <= b else c >= b && c <= a
    }
}
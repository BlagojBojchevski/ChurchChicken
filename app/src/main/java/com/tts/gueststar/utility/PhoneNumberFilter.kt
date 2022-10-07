package com.tts.gueststar.utility

import android.text.InputType
import android.text.Spanned
import android.text.method.NumberKeyListener

class PhoneNumberFilter : NumberKeyListener() {
    override fun getInputType(): Int {
        return InputType.TYPE_CLASS_PHONE
    }

    override fun getAcceptedChars(): CharArray {
        return charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
    }

    override fun filter(
        source: CharSequence, start: Int, end: Int,
        dest: Spanned, dstart: Int, dend: Int
    ): CharSequence? {

        if (dstart == 0 && source == "0")
            return ""

        if (end > start) {
            val destTxt = dest.toString()
            val resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend)

            if (!resultingTxt.matches("^\\d(\\d(\\d(-(\\d(\\d(\\d(-(\\d(\\d(\\d(\\d)?)?)?)?)?)?)?)?)?)?)?".toRegex())) {
                return ""
            }
        }
        return null
    }
}
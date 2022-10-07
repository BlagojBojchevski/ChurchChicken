package com.tts.gueststar.utility

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher

class PhoneNumberTextWatcher : TextWatcher {

    private var isFormatting: Boolean = false
    private var deletingHyphen: Boolean = false
    private var hyphenStart: Int = 0
    private var deletingBackward: Boolean = false

    override fun afterTextChanged(text: Editable?) {
        if (isFormatting)
            return

        isFormatting = true

        // If deleting hyphen, also delete character before or after it
        if (deletingHyphen && hyphenStart > 0) {
            if (deletingBackward) {
                if (text != null) {
                    if (hyphenStart - 1 < text.length) {
                        text.delete(hyphenStart - 1, hyphenStart)
                    }
                }
            } else if (text != null) {
                if (hyphenStart < text.length) {
                    text.delete(hyphenStart, hyphenStart + 1)
                }
            }
        }
        if (text != null) {
            if (text.length == 3 || text.length == 7) {
                text.append('-')
            }
        }

        isFormatting = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (isFormatting)
            return

        // Make sure user is deleting one char, without a selection
        val selStart = Selection.getSelectionStart(s)
        val selEnd = Selection.getSelectionEnd(s)
        if (s != null) {
            if (s.length > 1 // Can delete another character

                && count == 1 // Deleting only one character

                && after == 0 // Deleting

                && s[start] == '-' // a hyphen

                && selStart == selEnd
            ) { // no selection
                deletingHyphen = true
                hyphenStart = start
                // Check if the user is deleting forward or backward
                deletingBackward = selStart == start + 1
            } else {
                deletingHyphen = false
            }
        }
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
}
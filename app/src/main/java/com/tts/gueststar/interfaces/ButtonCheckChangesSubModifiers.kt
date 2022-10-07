package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Modifier
import com.tts.olosdk.models.Option

interface ButtonCheckChangesSubModifiers {
    fun onRadioBtnSubModifiersCheckChanges(
        group: Modifier,
        modifier: Option,
        check: Boolean
    )

    fun onCheckBoxSubModifiersCheck(
        group: Modifier,
        modifier: Option,
        check: Boolean
    )
    fun updatePrice()
}
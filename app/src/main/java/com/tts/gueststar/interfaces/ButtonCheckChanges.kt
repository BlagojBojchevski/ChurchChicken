package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Modifier
import com.tts.olosdk.models.Option
import com.tts.olosdk.models.Optiongroup

interface ButtonCheckChanges {
    fun onRadioBtnCheckChanges(
        group: Optiongroup,
        modifier: Option,
        check: Boolean
    )

    fun updatePrice()


    fun onCheckBoxCheck(
        group: Optiongroup,
        modifier: Option,
        check: Boolean
    )

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
}
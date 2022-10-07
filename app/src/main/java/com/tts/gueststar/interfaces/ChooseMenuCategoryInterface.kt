package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Category

interface ChooseMenuCategoryInterface {
    fun chooseMenuCategory(address: Category,position:Int)
}
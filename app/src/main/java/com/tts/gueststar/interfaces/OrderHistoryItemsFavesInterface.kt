package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Fave

interface OrderHistoryItemsFavesInterface {
    fun reorder(order: Fave)
    fun remove(order: Fave)
}
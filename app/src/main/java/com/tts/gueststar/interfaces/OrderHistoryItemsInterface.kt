package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Order

interface OrderHistoryItemsInterface {
    fun reorder(order: Order)
}
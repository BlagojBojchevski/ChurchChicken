package com.tts.gueststar.interfaces
import com.tts.olosdk.models.Product1

interface OrderSummaryItemsInterface {
    // fun showDetails(showHide: Boolean)
    fun editItem(choice: Product1)

    fun removeItem(choice: Product1)
}
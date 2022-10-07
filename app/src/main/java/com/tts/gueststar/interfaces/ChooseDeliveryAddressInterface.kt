package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Deliveryaddresse
interface ChooseDeliveryAddressInterface {
    fun chooseDeliveryAddress(address: Deliveryaddresse)
    fun deleteDeliveryAddress(address: Deliveryaddresse)
}
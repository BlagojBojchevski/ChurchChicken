package com.tts.gueststar.interfaces

import app.com.relevantsdk.sdk.models.LocationsResponse

interface OnSubmitReceiptLocationPicked {
    fun onFavLocationPicked(location: LocationsResponse.Location)
}
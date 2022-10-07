package com.tts.gueststar.interfaces

import app.com.relevantsdk.sdk.models.LocationsResponse

interface OnFavLocationPicked {
    fun onFavLocationPicked(location: LocationsResponse.Location, locationName: String, locationId: Int)
}
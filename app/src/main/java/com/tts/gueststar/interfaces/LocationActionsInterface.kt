package com.tts.gueststar.interfaces

import app.com.relevantsdk.sdk.models.LocationsResponse

interface LocationActionsInterface {
    fun callRestaurant(phoneNumber: String)
    fun directions(latitude: Double, longitude: Double)
    fun orderOnline(restaurant: LocationsResponse.Location)
    fun openWebView(url: String, name: String)
}
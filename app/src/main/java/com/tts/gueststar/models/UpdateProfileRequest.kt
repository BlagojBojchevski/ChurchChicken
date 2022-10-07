package com.tts.gueststar.models

import android.os.Parcelable
import com.tts.nsrsdkrelevant.cloudconnect.models.responses.MarketingInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateProfileRequest(
    var address: String? = "",
    var app_usage_purpose: String = "",
    var birthday: Long?=null,
    var email: String,
    var favorite_location_id: Int,
    var favorite_menu_items: List<String>? = null,
    var first_name: String,
    var gender: String="",
    var last_name: String="",
    var locale_id: Int=0,
    var mall_employee: Boolean,
    var marketing_info: MarketingInfo,
    var phone_number: String,
    var retailer: String,
    var special_occassion: Long,
    var username: String,
    var zipcode: String
):Parcelable

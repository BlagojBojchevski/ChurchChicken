package com.tts.gueststar.utility

import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.olosdk.models.*
import java.util.*
import kotlin.collections.HashMap

object OrderHelper {
    var flagChechForRewards = false
    var selectedGiftPosition = 0
    var fromHome = false
    var fromHistory = false
    var fromSummary = false
    var fromLogin = false
    var restaurantDetails: OloGetRestaurantDetailsResponse? = null
    var isavailable: Boolean? = null
    var advanceonly: Boolean? = null
    var advanceorderdays = 0
    var vendorId: Int = 0
    var basketId: String? = ""
    var deliveryMode: String = ""
    var location: LocationsResponse.Location? = null
    var locationAddress: String = ""
    var showCalories: Boolean = false
    var basket: OLOBasketResponse? = null
    var supportsbaskettransfers: Boolean = false
    var supportsSpecialCaracters: Boolean = false
    var specialInstrutionsQuantity: Int = 0
    var selectedModifiersIds = mutableListOf<Long>()
    var selectedModifiersDedault = mutableListOf<Long>()
    var selectedOptionsIds = mutableListOf<Long>()
    var carInfo = ""
    var deliveryAddress = ""
    var tip = "other"

    var giftCardAccountId = 0L
    var saveOnFile = false
    var currentAddedAccount: Account? = null
    var currentAddedGiftCard: Billingfield? = null
    var currentAddedGiftCardBalance: Double? = null
    var giftcardBillingSchemes: Billingscheme? = null
    var pickupTime: String? = null

    var groupQuantityModifiers: HashMap<Long, Int> = HashMap()
    var quantityModifiers: HashMap<Long, Int> = HashMap()
    var checkedRadioGruopWithModifiers: HashMap<Long, Long> = HashMap()
    var checkedCheckBoxWithModifiers: HashMap<Long, Long> = HashMap()
    var currentPrice: Double = 0.0
    var currentCalories: Long = 0
    var restaurantResponse: OLORestaurantMenuResponse? = null

    var fromEditForEdit = false
    var fromEdit = false
    var TMPselectedModifiersIds = mutableListOf<Long>()

    var acceptsordersbeforeopening: Boolean = false
    var acceptsordersuntilclosing: Boolean = false
    var open_time = ""

    var mCurrentDate: Date? = null
    var calendarRange: OLORestaurantCalendarResponse? = null

    var saveCreditOnFile = false

    var filed_make = 0
    var filed_model = 0
    var filed_color = 0
}
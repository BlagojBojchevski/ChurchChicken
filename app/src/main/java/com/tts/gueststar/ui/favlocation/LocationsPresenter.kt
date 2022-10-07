package com.tts.gueststar.ui.favlocation

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.OloGetRestaurantDetailsResponse
import com.tts.olosdk.models.VncrOloSettings
import rxjavasdk.api.RxJavaApiHandler

class LocationsPresenter(var view: LocationsView) : ApiListener.LocationsInterface,
    VncrOloSettingsInterface, OloRestaurantDetailsInterface, CreatebasketInterface,
    AddDeliveryModeInterface, OloTransferBasketInterface {
    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onSuccess(response: LocationsResponse?) {
        view.onSuccessGetLocations(response)

    }

    fun getLocations(context: Context, appkey: String, lat: Double, lng: Double, search: String) {
        RxJavaApiHandler().getLocations(context, appkey, lat, lng, search, this)
    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettingsNew(context, BuildConfig.APPKEY, this)
    }

    fun getLocationsNoSearch(
        context: Context,
        appkey: String,
        lat: Double,
        lng: Double
    ) {
        RxJavaApiHandler().getLocationsNoSearch(context, appkey, lat, lng, this)
    }

    fun getSiteById(context: Context, siteId: String) {
        RxJavaOloApiHelper().getRestaurantDetails(context, siteId, this)
    }

    fun createBasket(context: Context, vendorId: Int, oloAuthToken: String) {
        RxJavaOloApiHelper().createBasket(context, vendorId, oloAuthToken, this)
    }

    fun addDeliveryModeToBasket(context: Context, deliveryMode: String, basketId: String) {
        RxJavaOloApiHelper().addDeliveryModeToBasket(context, deliveryMode, basketId, this)
    }

    fun transferBasket(context: Context, vendorId: Int, basketId: String) {
        RxJavaOloApiHelper().transferBasket(context, vendorId, basketId, this)
    }

    interface LocationsView : BasePresenterView {
        fun onSuccessGetLocations(response: LocationsResponse?)
        fun enableMapButton()
        fun disableMapButton()
        fun setNoLocationsFound()
        fun getSitebyIdafterCloudConnect()
        fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse)
        fun onSuccessCreateBasket(response: OLOBasketResponse)
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
        fun onSuccessAddDeliveryMode(response: OLOBasketResponse)
    }

    fun onDestroy() {
        RxJavaApiHandler().onDestroy()
    }

    override fun onSuccessOloSettings(response: VncrOloSettings) {
        view.getSitebyIdafterCloudConnect()
    }

    override fun onFailureOloSettings(error: String) {
        view.showError(error)
    }

    override fun onFailureOloSettings() {
        view.showGenericError()
    }

    override fun onSuccessGetRestaurantDetails(response: OloGetRestaurantDetailsResponse) {
        view.onSuccessSiteById(response)
    }

    override fun onFailureGetRestaurantDetails(error: String) {
        view.showError(error)
    }

    override fun onFailureGetRestaurantDetails() {
        view.showGenericError()
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        view.onSuccessCreateBasket(response)
    }

    override fun onFailureCreateBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureCreateBasket() {
        view.showGenericError()
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        view.onSuccessAddDeliveryMode(response)
    }

    override fun onFailureAddDeliveryMode(error: String) {
        view.showError(error)
    }

    override fun onFailureAddDeliveryMode() {
        view.showGenericError()
    }

    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        view.onSuccessTransferBasket(response)
    }

    override fun onFailureTransferBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureTransferBasket() {
        view.showGenericError()
    }

}
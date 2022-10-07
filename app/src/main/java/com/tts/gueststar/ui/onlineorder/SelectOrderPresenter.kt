package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.gueststar.BuildConfig
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.AddDeliveryModeInterface
import com.tts.olosdk.interfaces.CreatebasketInterface
import com.tts.olosdk.interfaces.OloTransferBasketInterface
import com.tts.olosdk.interfaces.UserDeliveryAddressesInterface
import com.tts.olosdk.models.*
import com.tts.gueststar.ui.BasePresenterView


class SelectOrderPresenter(var view: SelectOrderView) :
    com.tts.olosdk.interfaces.OloRestaurantDetailsInterface,
    com.tts.olosdk.interfaces.VncrOloSettingsInterface, CreatebasketInterface,
    AddDeliveryModeInterface, UserDeliveryAddressesInterface, OloTransferBasketInterface {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        view.onSuccessTransferBasket(response)
    }

    override fun onFailureTransferBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureTransferBasket() {
        view.showGenericError()
    }

    override fun onSuccessUserDeliveryAddresses(response: UserDeliveryAddressesResponse) {
        view.onSuccessGetUserDeliveryAddresses(response)
    }

    override fun onFailureUserDeliveryAddresses(error: String) {
        view.showError(error)
    }

    override fun onFailureUserDeliveryAddresses() {
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

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        view.onSuccessCreateBasket(response)
    }

    override fun onFailureCreateBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureCreateBasket() {
        view.showGenericError()
    }

    override fun onSuccessOloSettings(response: VncrOloSettings) {
        view.getSitebyIdafterCloudConnect()
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


    override fun onFailureOloSettings(error: String) {
        view.showError(error)
    }

    override fun onFailureOloSettings() {
        view.showGenericError()
    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettings(context, BuildConfig.APPKEY, this)
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

    fun getUserDeliveryAddresses(context: Context, oloAuthToken: String) {
        RxJavaOloApiHelper().getUserDeliveryAddresses(context, oloAuthToken, this)
    }

    fun transferBasket(context: Context, vendorId: Int, basketId: String) {
        RxJavaOloApiHelper().transferBasket(context, vendorId, basketId, this)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }

    interface SelectOrderView : BasePresenterView {
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
        fun onSuccessAddDeliveryMode(response: OLOBasketResponse)
        fun onSuccessGetUserDeliveryAddresses(response: UserDeliveryAddressesResponse)
        fun onSuccessCreateBasket(response: OLOBasketResponse)
        fun getSitebyIdafterCloudConnect()
        fun onFailureUnauthorized(error: String)
        fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse)
    }
}
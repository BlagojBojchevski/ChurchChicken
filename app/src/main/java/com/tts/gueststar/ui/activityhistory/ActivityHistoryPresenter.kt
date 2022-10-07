package com.tts.gueststar.ui.activityhistory

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.LocationsResponse
import app.com.relevantsdk.sdk.models.RewardsActivityResponse
import app.com.relevantsdk.sdk.models.UserActivityResponse
import com.tts.gueststar.BuildConfig
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.*
import com.tts.gueststar.ui.BasePresenterView

import rxjavasdk.api.RxJavaApiHandler

class ActivityHistoryPresenter(var view: ActivityHistoryView) : ApiListener.UserActivityInterface,
    ApiListener.RewardsActivityInterface, ApiListener.LocationsInterface, OrderHistoryInterface,
    VncrOloSettingsInterface, OrderHistoryFavesInterface, CreateOrderFromBaskketInterface,
    AddDeliveryModeInterface, OloRestaurantDetailsInterface, AddCustomFieldsInterface,
    DeleteFaveOrderInterface, CreateOrderFromFavesBaskketInterface, OloTransferBasketInterface,
    CreatebasketInterface {
    override fun onSuccessCreatebasketFromFaveOrder(response: OLOBasketResponse) {
        view.onSuccessCreateBasketFromFaveOrder(response)
    }

    override fun onFailureCreatebasketFromFaveOrder(error: String) {
        view.showError(error)
    }

    override fun onFailureCreatebasketFromFaveOrder() {
        view.showGenericError()
    }

    override fun onSuccessDeleteFaveOrder() {
        view.onSuccessDeleteFavOrder()
    }

    override fun onFailureDeleteFaveOrder(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteFaveOrder() {
        view.showGenericError()
    }

    override fun onSuccessAddCustomFields(response: OLOBasketResponse) {
        view.onSuccessAddFields(response)
    }

    override fun onFailureAddCustomFields(error: String) {
        view.showError(error)
    }

    override fun onFailureAddCustomFields() {
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

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        view.onSuccessAddDeliveryMode(response)
    }

    override fun onFailureAddDeliveryMode(error: String) {
        view.showError(error)
    }

    override fun onFailureAddDeliveryMode() {
        view.showGenericError()
    }

    override fun onSuccessCreatebasketFromOrder(response: OLOBasketResponse) {
        view.onSuccessCreateBasketFromOrder(response)
    }

    override fun onFailureCreatebasketFromOrder(error: String) {
        view.showError(error)
    }

    override fun onFailureCreatebasketFromOrder() {
        view.showGenericError()
    }

    override fun onSuccessGetOrderHistoryFaves(response: OLOUserFavoriteBasketsResponse) {
        view.onSuccessgetOrderHistoryFaves(response)
    }

    override fun onFailureGetOrderHistoryFaves(error: String) {
        view.showError(error)
    }

    override fun onFailureGetOrderHistoryFaves() {
        view.showGenericError()
    }

    override fun onSuccessOloSettings(response: VncrOloSettings) {
        view.getOrderHistory()
    }

    override fun onFailureOloSettings(error: String) {
        view.showError(error)
    }

    override fun onFailureOloSettings() {
        view.showGenericError()
    }

    override fun onSuccessGetOrderHistory(response: OLOgetRecentOrdersResponse) {
        view.onSuccessgetOrderHistory(response)
    }

    override fun onFailureGetOrderHistory(error: String) {
        view.showError(error)
    }

    override fun onFailureGetOrderHistory() {
        view.showGenericError()
    }

    override fun onSuccess(response: LocationsResponse?) {
        view.onLocationDetails(response)
    }

    override fun onSuccess(response: RewardsActivityResponse?) {
        view.onSuccessGetRewardsActivity(response)
    }

    override fun onSuccess(response: UserActivityResponse?) {
        view.onSuccessGetUserActivity(response)
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    fun getSiteById(context: Context, siteId: String) {
        RxJavaOloApiHelper().getRestaurantDetails(context, siteId, this)
    }

    fun addCustomFieldsToBasket(context: Context, id: Int, value: String, basketId: String) {
        RxJavaOloApiHelper().addCustomFieldsToBasket(context, id, value, basketId, this)
    }


    fun addDeliveryModeToBasket(context: Context, deliveryMode: String, basketId: String) {
        RxJavaOloApiHelper().addDeliveryModeToBasket(context, deliveryMode, basketId, this)
    }

    fun getLocations(context: Context, appkey: String, lat: Double, lng: Double, search: String) {
        RxJavaApiHandler().getLocations(context, appkey, lat, lng, search, this)
    }

    fun getUserActivity(context: Context, auth_token: String) {
        RxJavaApiHandler().getUserActivity(context, BuildConfig.APPKEY, auth_token, this)
    }

    fun getRewardsActivity(context: Context, auth_token: String) {
        RxJavaApiHandler().getRewardsActivity(context, BuildConfig.APPKEY, auth_token, this)
    }

    fun getUserRecentOrders(context: Context, oloAuthToken: String) {
        RxJavaOloApiHelper().getUserRecentOrders(context, oloAuthToken, this)
    }

    fun getUserFavoriteBaskets(context: Context, oloAuthToken: String) {
        RxJavaOloApiHelper().getUserFavoriteBaskets(context, oloAuthToken, this)
    }

    fun createBasketFromOrder(context: Context, id: String, oloAuthToken: String) {
        RxJavaOloApiHelper().createBasketFromOrder(context, id, oloAuthToken, this)
    }

    fun createBasketFromFavoriteOrder(context: Context, id: String, oloAuthToken: String) {
        RxJavaOloApiHelper().createBasketFromFavoriteOrder(context, id, oloAuthToken, this)
    }

    fun deleteUserFavoriteBasket(context: Context, oloAuthToken: String, id: Long) {
        RxJavaOloApiHelper().deleteUserFavoriteBasket(context, oloAuthToken, id, this)
    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettings(context, BuildConfig.APPKEY, this)
    }

    fun transferBasket(context: Context, vendorId: Int, basketId: String) {
        RxJavaOloApiHelper().transferBasket(context, vendorId, basketId, this)
    }
    fun createBasket(context: Context, vendorId: Int, oloAuthToken: String) {
        RxJavaOloApiHelper().createBasket(context, vendorId, oloAuthToken, this)
    }

    interface ActivityHistoryView : BasePresenterView {
        fun onSuccessDeleteFavOrder()
        fun onSuccessAddFields(response: OLOBasketResponse)
        fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse)
        fun onSuccessAddDeliveryMode(response: OLOBasketResponse)
        fun onSuccessCreateBasketFromOrder(response: OLOBasketResponse)
        fun onSuccessCreateBasketFromFaveOrder(response: OLOBasketResponse)
        fun getOrderHistory()
        fun onSuccessgetOrderHistoryFaves(response: OLOUserFavoriteBasketsResponse)
        fun onSuccessgetOrderHistory(response: OLOgetRecentOrdersResponse)
        fun onLocationDetails(response: LocationsResponse?)
        fun onFailureUnauthorized(error: String)
        fun onSuccessGetUserActivity(response: UserActivityResponse?)
        fun onSuccessGetRewardsActivity(response: RewardsActivityResponse?)
        fun onSuccessCreateBasket(response: OLOBasketResponse)
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
    }

    fun onDestroy() {
        RxJavaApiHandler().onDestroy()
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

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        view.onSuccessCreateBasket(response)
    }

    override fun onFailureCreateBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureCreateBasket() {
        view.showGenericError()
    }
}
package com.tts.gueststar.ui.favlocation

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.LocationsResponse
import app.com.relevantsdk.sdk.models.UpdateUserProfileRequest
import app.com.relevantsdk.sdk.models.UpdateUserProfileResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.gueststar.BuildConfig
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.OloTransferBasketInterface
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class FavoriteLocationPresenter(var view: FavoriteLocationView) : ApiListener.LocationsInterface,
    ApiListener.UpdateUserProfileInterface, OloTransferBasketInterface,
    ApiListener.UserProfileInterface {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
       view.onSuccessTransferBasket(response)
    }

    override fun onFailureTransferBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureTransferBasket() {
        view.showGenericError()
    }



    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailureUnauthorized(error: String) {
        view.showGenericError()
    }

    override fun onSuccess(response: UpdateUserProfileResponse?) {
        if (response!!.status)
            view.onSuccessUpdateProfile(response)
        else {
            if (response.message.isNotEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }

    override fun onSuccess(response: UserProfileResponse?) {
     view.onSuccessGetUserInfo(response!!)
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onSuccess(response: LocationsResponse?) {
        view.onSuccessGetLocations(response)
    }

    fun transferBasket(context: Context, vendorId: Int, basketId: String) {
        RxJavaOloApiHelper().transferBasket(context, vendorId, basketId, this)
    }

    fun getLocations(context: Context, appkey: String, lat: Double, lng: Double, search: String) {
        RxJavaApiHandler().getLocations(context, appkey, lat, lng, search, this)
    }

    fun getLocationsNoSearch(
        context: Context,
        appkey: String,
        lat: Double,
        lng: Double
    ) {
        RxJavaApiHandler().getLocationsNoSearch(context, appkey, lat, lng, this)
    }


    fun updateProfile(
        context: Context,
        auth_token: String,
        body: UpdateUserProfileRequest
    ) {
        RxJavaApiHandler().updateUserProfile(context, body, BuildConfig.APPKEY, auth_token, this)
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaApiHandler().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )

    }


    interface FavoriteLocationView : BasePresenterView {
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
        fun onSuccessUpdateProfile(response: UpdateUserProfileResponse?)
        fun onSuccessGetLocations(response: LocationsResponse?)
        fun onSuccessGetUserInfo(response:UserProfileResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
        RxJavaApiHandler().onDestroy()
    }

}
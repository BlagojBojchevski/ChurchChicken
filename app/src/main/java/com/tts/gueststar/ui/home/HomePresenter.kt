package com.tts.gueststar.ui.home

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.GalleryStreamResponse
import app.com.relevantsdk.sdk.models.HomeModulesResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.GetUserPayCodeInterface
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.NCRGetUserCodeInterface
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.UserPaymentInfoInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.GetNCRUserPaymentInfoResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.OloGetRestaurantDetailsResponse
import com.tts.olosdk.models.VncrOloSettings
import ncruser.models.NCRGetUserCodeResponse
import ncruser.models.NCRGetUserPayCodeResponse
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class HomePresenter(var view: HomeView) : ApiListener.GaleryStreamInterface, ApiListener.HomeModulesInterface,
    NCRGetUserCodeInterface, UserPaymentInfoInterface, GetUserPayCodeInterface,
    VncrOloSettingsInterface, OloRestaurantDetailsInterface, CreatebasketInterface,
    AddDeliveryModeInterface, OloTransferBasketInterface, ApiListener.UserProfileInterface {
    override fun onFailureGetUserPayCode() {
        view.showGenericError()
    }

    override fun onFailureGetUserPayCode(error: String) {
        view.showError(error)
    }

    override fun onFailureGetUserPayCodeUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccessGetUserPayCode(response: NCRGetUserPayCodeResponse) {
        if (response.status)
            view.onSuccessGetUserCode(response.usercode)
        else {
            if (!response.notice.isEmpty())
                view.showError(response.notice)
            else
                view.showGenericError()
        }
    }


    override fun onFailurePaymentInfo() {
        view.showGenericError()
    }

    override fun onFailurePaymentInfo(error: String) {
        view.onShowAddPaymentPopup()
    }

    override fun onFailurePaymentInfoUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccessPaymentInfo(response: GetNCRUserPaymentInfoResponse) {
        if (response.status) {
            if (response.response.Payments.isNotEmpty()) {
                view.getUserPayCode()
            } else {
                view.onShowAddPaymentPopup()
            }
        } else {
            view.onShowAddPaymentPopup()
        }
    }

    override fun onFailureGetUserCode() {
        view.showGenericError()
    }

    override fun onFailureGetUserCode(error: String) {
        view.showError(error)
    }

    override fun onFailureGetUserCodeUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccessGetUserCode(response: NCRGetUserCodeResponse) {
        if (response.status)
            view.onSuccessGetUserCode(response.usercode)
        else {
            if (!response.notice.isEmpty())
                view.showError(response.notice)
            else
                view.showGenericError()
        }
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccess(response: HomeModulesResponse) {
        if (response.status)
            view.onSuccessGetHomeModules(response)
        else {
            if (!response.message.isEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }


    override fun onFailure() {
        view.showGenericError()
    }

    override fun onSuccess(response: UserProfileResponse?) {
        view.successGetUserProfile(response)
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onSuccess(response: GalleryStreamResponse?) {
        if (response!!.status)
            view.onSuccessGetImages(response)
        else {
            if (!response.message.isEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }

    fun getUserPaymentInfo(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserPaymentInfo(
            context, BuildConfig.APPKEY, auth_token, "en"
            , this
        )
    }

    fun getUserPayCode(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserPayCode(
            context, BuildConfig.APPKEY, auth_token, 0.0, 0.0
            , this
        )
    }

    fun getUserCode(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserCode(
            context, BuildConfig.APPKEY, auth_token
            , this
        )
    }

    fun getImages(context: Context) {
        RxJavaApiHandler().getImageGallery(
            context, BuildConfig.APPKEY,
            this
        )
    }

    fun getHomeModules(context: Context, auth_token: String) {
        RxJavaApiHandler().getHomeModules(
            context, BuildConfig.APPKEY, auth_token,
            this
        )
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaApiHandler().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )

    }

    fun onDestroy(){
        RxJavaApiHandler().onDestroy()
    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettingsNew(context, BuildConfig.APPKEY, this)
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

    interface HomeView : BasePresenterView {
        fun getUserPayCode()
        fun onShowAddPaymentPopup()
        fun onFailureUnauthorized(error: String)
        fun onSuccessGetImages(response: GalleryStreamResponse)
        fun onSuccessGetUserCode(code: String)
        fun onSuccessGetHomeModules(response: HomeModulesResponse)
        fun getSitebyIdafterCloudConnect()
        fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse)
        fun onSuccessCreateBasket(response: OLOBasketResponse)
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
        fun onSuccessAddDeliveryMode(response: OLOBasketResponse)
        fun successGetUserProfile(response: UserProfileResponse?)
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
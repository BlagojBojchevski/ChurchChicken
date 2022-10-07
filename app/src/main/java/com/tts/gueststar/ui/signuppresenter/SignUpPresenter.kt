package com.tts.gueststar.ui.signuppresenter

import android.content.Context
import android.util.Patterns
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.interfaces.OauthTokenInterface
import app.com.relevantsdk.sdk.models.OauthTokenResponse
import app.com.relevantsdk.sdk.models.SignUpRequest
import app.com.relevantsdk.sdk.models.SignUpResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.GetOrCreateInterface
import com.tts.olosdk.interfaces.VncrOloSettingsInterface
import com.tts.olosdk.models.OLOGetOrCreateResponse
import com.tts.olosdk.models.VncrOloSettings
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class SignUpPresenter(var context: Context, private val view: SignUpInterface) :
     OauthTokenInterface, VncrOloSettingsInterface, GetOrCreateInterface,
    ApiListener.SignUpInterface, ApiListener.UserProfileInterface {

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        androidId: String,
        lat: Double,
        lng: Double,
        registerDeviceType: String,
        registerType: String,
        referalCode: String,
        signInDeviceType: String,
        deviceToken: String,
        phoneModel: String,
        os: String,
        marketingOptin: Boolean,
        favoriteLocationId: String,
        zipcode: String,
        birthday: Long?
    ) {
        val request = SignUpRequest(
            first_name = firstName,
            last_name = lastName,
            phone_number = phoneNumber,
            email = email,
            password = password,
            birthday = birthday,
            register_type = registerType.toInt(),
            sing_in_device_type = signInDeviceType,
            register_device_type = registerDeviceType,
            referral_code = referalCode,
            device_token = deviceToken,
            android_id = androidId,
            phone_model = phoneModel,
            os = os,
            zipcode = zipcode,
            latitude = lat,
            longitude = lng,
            favorite_location_id = favoriteLocationId.toInt(),
            marketing = marketingOptin
        )


        //  RxJavaCloudConnectApiHelper().signUpVncrOlo(context, request, this)
        RxJavaApiHandler().signUp(context, request, BuildConfig.APPKEY, this)
    }

    fun getOauthToken(context: Context, auth_token: String) {
        RxJavaApiHandler().getOauthToken(context, BuildConfig.APPKEY, auth_token, this)
    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettingsNew(context, BuildConfig.APPKEY, this)
    }

    fun checkRequirements(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        zipCode: String,
        favLocationId: Int,
        termsChecked: Boolean,
        birthday: Long?
    ) {
        if (checkValidMail(email) && password.length >= AppConstants.minPasswordCharacters && firstName.length > 1 && lastName.length > 1 && phoneNumber.length == 12 && zipCode.length == 5 &&
            favLocationId != 0 && termsChecked && birthday != null
        ) {
            view.enableButton()
        } else {
            view.disableButton()
        }

    }


    private fun checkValidMail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaApiHandler().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )

    }

    interface SignUpInterface : BasePresenterView {
        fun onSuccessSignUp(response: SignUpResponse?)
        fun enableButton()
        fun disableButton()
        fun successGetUserProfile(response: UserProfileResponse?)
        fun failedGetUserProfile()
        fun onSuccessGetOuathToken(response: OauthTokenResponse)
        fun onFailedOauthToken(error: String)
        fun onFailedOauthToken()
        fun onFailedOauthTokenUnauthorized(error: String)
        fun onSuccessOloSettings()
        fun onSuccessGetOrcreate(response: OLOGetOrCreateResponse)

    }

    fun getOrCreate(context: Context, oloAuthToken: String, phoneNumber: String) {
        RxJavaOloApiHelper().getOrCreate(
            context,
            oloAuthToken, phoneNumber, this
        )
    }


    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }

    override fun onSuccessOauthToken(response: OauthTokenResponse) {
        view.onSuccessGetOuathToken(response)
    }

    override fun onFailureOauthToken(error: String) {
        view.onFailedOauthToken(error)
    }

    override fun onFailureOauthToken() {
        view.onFailedOauthToken()
    }

    override fun onFailedOuathTokenUnauthorized(error: String) {
        view.onFailedOauthTokenUnauthorized(error)
    }

    override fun onSuccessOloSettings(response: VncrOloSettings) {
        view.onSuccessOloSettings()
    }

    override fun onFailureOloSettings(error: String) {
        view.showError(error)
    }

    override fun onFailureOloSettings() {
        view.showGenericError()
    }

    override fun onSuccessGetOrCreate(response: OLOGetOrCreateResponse) {
        view.onSuccessGetOrcreate(response)
    }

    override fun onFailureGetOrCreate(error: String) {
        view.showError(error)
    }

    override fun onFailureGetOrCreate() {
        view.showGenericError()
    }

    override fun onSuccess(response: SignUpResponse?) {
        view.onSuccessSignUp(response)
    }

    override fun onSuccess(response: UserProfileResponse?) {
        view.successGetUserProfile(response)
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailedOauthTokenUnauthorized(error)
    }

}
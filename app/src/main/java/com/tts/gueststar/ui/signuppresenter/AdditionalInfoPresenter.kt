package com.tts.gueststar.ui.signuppresenter

import android.content.Context
import android.util.Patterns
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.NCRSignUpInterface
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.OauthTokenInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.NCRSignUpResponse
import com.tts.nsrsdkrelevant.cloudconnect.models.requests.NCRAdditionalInfo
import com.tts.nsrsdkrelevant.cloudconnect.models.responses.NCR2UserInfoResponse
import com.tts.nsrsdkrelevant.cloudconnect.models.responses.OauthTokenResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.GetOrCreateInterface
import com.tts.olosdk.interfaces.VncrOloSettingsInterface
import com.tts.olosdk.models.OLOGetOrCreateResponse
import com.tts.olosdk.models.VncrOloSettings
import ncruser.interfaces.GetUserInfoInterfaceVNCR2
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper

class AdditionalInfoPresenter(var context: Context, private val view: AdditionalInfoInterface) :
    NCRSignUpInterface,
    GetUserInfoInterfaceVNCR2, GetOrCreateInterface, OauthTokenInterface, VncrOloSettingsInterface {
    override fun onSuccessGetUserInfo(response: NCR2UserInfoResponse) {
        view.successGetUserProfile(response)
    }

    override fun onFailureGetUserInfo(error: String) {
        view.failedGetUserProfile()
    }

    override fun onFailureGetUserInfoUnauthorized(error: String) {
        view.failedGetUserProfile()
    }

    override fun onFailureGetUserInfo() {
        view.failedGetUserProfile()
    }

    override fun onFailureSignUp() {
        view.showGenericError()
    }

    override fun onFailureSignUp(error: String) {
        view.showError(error)
    }

    override fun onSuccessSignUp(response: NCRSignUpResponse) {
        view.onSuccessSignUp(response)
    }

    fun addtionalInfo(
        email: String,
        password: String,
        phoneNumber: String,
        androidId: String,
        lat: Double,
        lng: Double,
        registerDeviceType: String,
        registerType: String,
        appkey: String,
        referalCode: String,
        deviceToken: String,
        devideId: String,
        phoneModel: String,
        os: String,
        dobDay: String,
        dobMonth: String,
        dobYear: String,
        marketingOptin: String,
        connectType: String,
        favoriteLocationId: String,
        secQuestion: String,
        secAnswer: String,
        zipcode: String
    ) {
        val request = NCRAdditionalInfo(
            email, password, phoneNumber, androidId, lat, lng,
            registerDeviceType, registerType, appkey, referalCode, deviceToken, devideId,
            phoneModel, os, dobDay, dobMonth, dobYear, "", marketingOptin,
            connectType, favoriteLocationId, secAnswer, secQuestion, zipcode, "", 1
        )

        RxJavaCloudConnectApiHelper().additionalInfo(context, request, this)

    }

    fun checkRequirements(
        email: String,
        password: String,
        phoneNumber: String,
        zipCode: String,
        favLocationId: Int,
        secQuestion: String,
        secAnswer: String,
        termsChecked: Boolean
    ) {
        if (checkValidMail(email) && password.length == AppConstants.minPasswordCharacters && phoneNumber.length == 12 && zipCode.length == 5 &&
            favLocationId != 0 && secQuestion.length > 1 && secQuestion != "Security Question" && secAnswer.length > 1 && termsChecked
        ) {
            view.enableButton()
        } else {
            view.disableButton()
        }

    }

    fun checkRequirementsTest(
        email: String,
        password: String,
        phoneNumber: String,
        zipCode: String,
        favLocationId: Int,
        secQuestion: String,
        secAnswer: String,
        termsChecked: Boolean
    ): Boolean {
        return checkValidMail(email) && password.length == AppConstants.minPasswordCharacters && phoneNumber.length == 12 && zipCode.length == 5 &&
                favLocationId != 0 && secQuestion.length > 1 && secAnswer.length > 1 && termsChecked

    }

    private fun checkValidMail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaCloudConnectApiHelper().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )
    }

    interface AdditionalInfoInterface : BasePresenterView {
        fun onSuccessSignUp(response: NCRSignUpResponse?)
        fun enableButton()
        fun disableButton()
        fun successGetUserProfile(response: NCR2UserInfoResponse?)
        fun failedGetUserProfile()

        fun onSuccessGetOuathToken(response: OauthTokenResponse)
        fun onFailedOauthToken(error: String)
        fun onFailedOauthToken()
        fun onFailedOauthTokenUnauthorized(error: String)
        fun onSuccessOloSettings()
        fun onSuccessGetOrcreate(response: OLOGetOrCreateResponse)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }

    fun getOauthToken(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getOauthToken(context, BuildConfig.APPKEY, auth_token, this)
    }
    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettings(context, BuildConfig.APPKEY, this)
    }

    fun getOrCreate(context: Context, oloAuthToken: String, phoneNumber: String) {
        RxJavaOloApiHelper().getOrCreate(
            context,
            oloAuthToken, phoneNumber,this
        )
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
}
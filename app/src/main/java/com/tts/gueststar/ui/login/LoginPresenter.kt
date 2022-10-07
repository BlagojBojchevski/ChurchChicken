package com.tts.gueststar.ui.login

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.interfaces.OauthTokenInterface
import app.com.relevantsdk.sdk.models.OauthTokenResponse
import app.com.relevantsdk.sdk.models.SignInRequest
import app.com.relevantsdk.sdk.models.SignInResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.GetOrCreateInterface
import com.tts.olosdk.interfaces.VncrOloSettingsInterface
import com.tts.olosdk.models.OLOGetOrCreateResponse
import com.tts.olosdk.models.VncrOloSettings
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler


class LoginPresenter(val view: ILoginView) : ILoginPresenter,
     GetOrCreateInterface, VncrOloSettingsInterface, OauthTokenInterface,
    ApiListener.SignInInterface, ApiListener.UserProfileInterface{
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



    override fun onLogin(
        context: Context,
        email: String,
        password: String,
        android_id: String,
        notificationToken: String
    ) {
        val loginRequest = SignInRequest(
            email = email,
            password = password,
            sign_in_device_type = AppConstants.DEVICE_TYPE,
            register_type = AppConstants.REGISTER_TYPE.toInt(),
            phone_model = AppConstants.manufacturer + AppConstants.model,
            os = AppConstants.androidOS,
            device_token = notificationToken
        )

        RxJavaApiHandler().signIn(context, loginRequest, BuildConfig.APPKEY, this)
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaApiHandler().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )

    }

    fun getoloSettings(context: Context) {
        RxJavaOloApiHelper().getOLOCompanySettingsNew(context, BuildConfig.APPKEY, this)
    }

    fun getOauthToken(context: Context, auth_token: String) {
        RxJavaApiHandler().getOauthToken(context, BuildConfig.APPKEY, auth_token, this)
    }

    fun getOrCreate(context: Context, oloAuthToken: String, phoneNumber: String) {
        RxJavaOloApiHelper().getOrCreate(
            context,
            oloAuthToken, phoneNumber, this
        )
    }

    override fun onLoginValidation(email: String, password: String, isChecked: Boolean) {
        val user = UserLogin(email, password)
        if (TextUtils.isEmpty(user.email) || !Patterns.EMAIL_ADDRESS.matcher(user.email).matches() || TextUtils.isEmpty(
                user.password
            ) || user.password.length < AppConstants.minPasswordCharacters || !isChecked
        )
            view.disableLogin()
        else
            view.enableLogin()
    }


    override fun onLoginValidationTest(
        email: String,
        password: String,
        isChecked: Boolean
    ): Boolean {
        val user = UserLogin(email, password)
        return !(TextUtils.isEmpty(user.email) || !Patterns.EMAIL_ADDRESS.matcher(user.email).matches() || TextUtils.isEmpty(
            user.password
        ) || user.password.length < AppConstants.minPasswordCharacters || !isChecked)
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

    override fun onSuccess(response: SignInResponse?) {
        if (response != null) {
            if (response.status)
                view.successfulLogin(response)
            else {
                if (!response.message.isEmpty())
                    view.showError(response.message)
                else
                    view.showGenericError()
            }
        } else {
            view.showGenericError()
        }
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
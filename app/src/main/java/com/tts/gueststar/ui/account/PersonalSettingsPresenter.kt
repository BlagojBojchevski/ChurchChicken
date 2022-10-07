package com.tts.gueststar.ui.account

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.UpdateUserProfileRequest
import app.com.relevantsdk.sdk.models.UpdateUserProfileResponse
import app.com.relevantsdk.sdk.models.UploadUserProfilePictureResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class PersonalSettingsPresenter(var view: PersonalSettingsView) :
    ApiListener.UploadProfilePictureInterface,
    ApiListener.UserProfileInterface, ApiListener.UpdateUserProfileInterface {

    override fun onSuccess(response: UploadUserProfilePictureResponse?) {
        view.successUpdatePicture(response)
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }


    fun checkRequirements(
        phoneNumber: String,
        birthday: Long?
    ) {
        if (phoneNumber.length == 12 && birthday != null && birthday != -2208988793 //-2208988793 is added becouse when bithday is empty from BE we receive this default number
        ) {
            view.enableButton()
        } else {
            view.disableButton()
        }

    }

    fun checkRequirementsTest(
        phoneNumber: String,
        favLocationId: Int
    ): Boolean {
        return phoneNumber.length == 12 &&
                favLocationId != 0

    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onSuccess(response: UserProfileResponse?) {
        view.successGetUserProfile(response)
    }

    override fun onSuccess(response: UpdateUserProfileResponse?) {
        if (response!!.status)
            view.onSuccessUpdateProfile(response)
        else {
            if (!response.message.isEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    fun getUserProfile(auth_token: String, context: Context) {
        RxJavaApiHandler().getUserProfile(
            context, BuildConfig.APPKEY,
            auth_token, this
        )
    }


    fun updateProfile(
        context: Context,
        auth_token: String,
        body: UpdateUserProfileRequest
    ) {
        RxJavaApiHandler().updateUserProfile(context, body, BuildConfig.APPKEY, auth_token, this)
    }


    fun updateImage(
        context: Context,
        auth_token: String,
        imagePath: String,
        byteArray: ByteArray
    ) {
        RxJavaApiHandler().uploadProfilePicture(
            context, imagePath, byteArray, BuildConfig.APPKEY,
            auth_token, this
        )
    }


    interface PersonalSettingsView : BasePresenterView {
        fun onSuccessUpdateProfile(response: UpdateUserProfileResponse?)
        fun onFailureUnauthorized(error: String)
        fun disableButton()
        fun enableButton()
        fun successGetUserProfile(response: UserProfileResponse?)
        fun successUpdatePicture(response: UploadUserProfilePictureResponse?)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }
}
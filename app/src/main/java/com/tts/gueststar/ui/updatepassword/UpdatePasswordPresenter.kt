package com.tts.gueststar.ui.updatepassword

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.UpdateUserPasswordRequest
import app.com.relevantsdk.sdk.models.UpdateUserPasswordResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class UpdatePasswordPresenter(var view: UpdatePasswordView) :
    ApiListener.UpdatePasswordInterface {


    fun updatePassword(
        context: Context,
        oldPassword: String,
        newPassword: String,
        auth_token: String
    ) {

        val request = UpdateUserPasswordRequest(
            password_new = newPassword,
            password_old = oldPassword

        )

        RxJavaApiHandler().updateUserPassword(
            context, request, BuildConfig.APPKEY, auth_token, this
        )
    }

    fun updatePasswordalidation(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        if (oldPassword.length < AppConstants.minPasswordCharacters || newPassword.length < AppConstants.minPasswordCharacters || confirmPassword.length < AppConstants.minPasswordCharacters || (newPassword != confirmPassword))
            view.disableButton()
        else
            view.enableButton()
    }

    fun updatePasswordalidationTest(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        return !(oldPassword.length < AppConstants.minPasswordCharacters || newPassword.length < AppConstants.minPasswordCharacters || confirmPassword.length < AppConstants.minPasswordCharacters || (newPassword != confirmPassword))
    }

    interface UpdatePasswordView : BasePresenterView {
        fun disableButton()
        fun enableButton()
        fun onSuccessUpdatePassword(response: UpdateUserPasswordResponse?)
        fun unauthorizedRequest(messgae: String)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }

    override fun onSuccess(response: UpdateUserPasswordResponse?) {
        if (response!!.status)
            view.onSuccessUpdatePassword(response)
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

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailureUnauthorized(error: String) {
        view.unauthorizedRequest(error)
    }
}
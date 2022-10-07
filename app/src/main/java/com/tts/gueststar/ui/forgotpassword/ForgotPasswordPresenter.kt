package com.tts.gueststar.ui.forgotpassword

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.ForgotPasswordRequest
import app.com.relevantsdk.sdk.models.ForgotPasswordResponse
import app.com.relevantsdk.sdk.models.SignInRequest
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import ncruser.interfaces.ForgotPasswordInterface
import ncruser.models.NCRUserForgotPasswordRequestOlo
import ncruser.models.NCRUserForgotPasswordResponse
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class ForgotPasswordPresenter(var view: ForgotPasswordView) :
    ApiListener.ForgotPasswordInterface {

    fun userForgotPassword(email: String, context: Context) {
        val request = ForgotPasswordRequest(
            email
        )
        RxJavaApiHandler().forgotPassword(
            context, request, BuildConfig.APPKEY,
            this
        )
    }

    fun emailValidation(email: String) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            view.disableButton()
        else
            view.enableButton()
    }

    fun emailValidationTest(email: String): Boolean {
        return !(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }


    interface ForgotPasswordView : BasePresenterView {
        fun disableButton()
        fun enableButton()
        fun onSuccessResetPassword(response: ForgotPasswordResponse)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }

    override fun onSuccess(response: ForgotPasswordResponse?) {
        if (response!!.status)
            view.onSuccessResetPassword(response)
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
}
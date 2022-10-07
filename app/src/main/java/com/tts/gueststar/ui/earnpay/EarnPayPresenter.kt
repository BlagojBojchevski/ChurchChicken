package com.tts.gueststar.ui.earnpay

import android.content.Context
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.UserPaymentInfoInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.GetNCRUserPaymentInfoResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import ncruser.interfaces.SendCodeInterface
import ncruser.interfaces.SendNcrCodeInterface
import ncruser.models.PayCodeResponse
import ncruser.models.SendCodeResponse
import ncruser.models.SendCoodeRequest
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper

class EarnPayPresenter(var view: EarnPayView) : UserPaymentInfoInterface, SendNcrCodeInterface,
    SendCodeInterface {
    override fun onSendCodeSuccess(response: PayCodeResponse) {
       view.onSendCodeSuccess(response)
    }

    override fun onSuccessSendCode(response: SendCodeResponse) {
        view.onSuccessSendCode(response)
    }

    override fun onFailureSendCode(error: String) {
        view.showError(error)
    }

    override fun onFailureSendCodeUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onFailureSendCode() {
        view.showGenericError()
    }

    override fun onFailurePaymentInfo() {
        view.showGenericError()
    }

    override fun onFailurePaymentInfo(error: String) {
        view.onShowPaymentTutorial()
    }

    override fun onFailurePaymentInfoUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccessPaymentInfo(response: GetNCRUserPaymentInfoResponse) {
        if (response.status) {
            if (response.response.Payments.isNotEmpty()) {
                view.hasUserPayment()
            } else {
                view.onShowPaymentTutorial()
            }
        } else {
            view.onShowPaymentTutorial()
        }
    }


    fun getUserPaymentInfo(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserPaymentInfo(
            context, BuildConfig.APPKEY, auth_token, "en"
            , this
        )
    }

//    fun sendCode(context: Context, auth_token: String, code: String) {
//        val request =
//            SendCoodeRequest(auth_token = auth_token, appkey = BuildConfig.APPKEY, code = code)
//        RxJavaCloudConnectApiHelper().sendNcrCode(
//            context, request, this
//        )
//    }

    fun sendCode(context: Context, auth_token: String, code: String) {
        val request =
            SendCoodeRequest(auth_token = auth_token, appkey = BuildConfig.APPKEY, code = code)
        RxJavaCloudConnectApiHelper().sendCode(
            context, request, this
        )
    }

    fun onCodeValidateion(
        promoCode: String
    ) {
        if (promoCode.length < AppConstants.minAnswerCharacters)
            view.disableButton()
        else
            view.enableButton()
    }

    fun onCodeValidateionTest(
        promoCode: String
    ): Boolean {
        return promoCode.length >= AppConstants.minAnswerCharacters
    }


    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }

    interface EarnPayView : BasePresenterView {
        fun onSendCodeSuccess(response: PayCodeResponse)
        fun disableButton()
        fun enableButton()
        fun onSuccessSendCode(response: SendCodeResponse)
        fun hasUserPayment()
        fun onShowPaymentTutorial()
        fun onFailureUnauthorized(error: String)
    }
}
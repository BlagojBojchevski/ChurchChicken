package com.tts.gueststar.ui.earnpay

import android.content.Context
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.UserPaymentInfoInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.GetNCRUserPaymentInfoResponse
import com.tts.gueststar.BuildConfig

import com.tts.gueststar.ui.BasePresenterView
import ncruser.interfaces.PayWithSignatureInterface
import ncruser.interfaces.SendNcrCodeInterface
import ncruser.models.SendCodeResponse
import ncruser.models.SendCoodeRequest
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper


class PayBillPresenter(val view: PayBillView) : UserPaymentInfoInterface, SendNcrCodeInterface,
    PayWithSignatureInterface {
    override fun onSuccess(response: ncruser.models.PayWithSignatureResponse?) {
        if (response!!.status) {
            view.onSuccessPay(response.amount_billed)
        } else {
            if (response.message.isNotEmpty())
                view.showError(response.message)
            else view.showGenericError()
        }
    }

//    override fun onSuccess(response: PayWithSignatureResponse?) {
//        if (response!!.status) {
//            view.onSuccessPay(response.billed_price)
//        } else {
//            if (response.message.isNotEmpty())
//                view.showError(response.message)
//            else view.showGenericError()
//        }
//    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailureUnauthorized(error: String) {
        view.unauthorized(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onSuccessSendCode(response: SendCodeResponse) {
        view.onSuccessSendCode(response)
    }

    override fun onFailureSendCode(error: String) {
        view.showError(error)
    }

    override fun onFailureSendCodeUnauthorized(error: String) {
        view.unauthorized(error)
    }

    override fun onFailureSendCode() {
        view.showGenericError()
    }

    override fun onSuccessPaymentInfo(response: GetNCRUserPaymentInfoResponse) {
        when {
            response.status -> {
                response.response.let {
                    response.response.Payments.let {
                        if (response.response.Payments.isEmpty()) {
                            view.setUserHasCC(false)
                        } else {
                            view.setUserHasCC(true)
                        }
                    }
                }
            }
            else -> {
                view.setUserHasCC(false)
                view.showError(response.message.toString())
            }
        }
    }

    override fun onFailurePaymentInfo(error: String) {
        view.showError(error)
    }

    override fun onFailurePaymentInfo() {
        view.showGenericError()
    }

    override fun onFailurePaymentInfoUnauthorized(error: String) {
        view.unauthorized(error)
    }


    fun getUserPaymentInfo(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserPaymentInfo(
            context, BuildConfig.APPKEY, auth_token, "en"
            , this
        )
    }

    fun sendCode(context: Context, auth_token: String, code: String) {
        val request =
            SendCoodeRequest(auth_token = auth_token, appkey = BuildConfig.APPKEY, code = code)
        RxJavaCloudConnectApiHelper().sendNcrCode(
            context, request, this
        )
    }

    fun payWithSignature(
        context: Context,
        code: String,
        tip: Float,
        signature: ByteArray,
        auth_token: String
    ) {

        val bytes = signature
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
        val signature_ = MultipartBody.Part.createFormData("signature", "image.jpg", requestFile)
        val appKey = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            BuildConfig.APPKEY
        )
        val authToken = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), auth_token)
        val receiptCode = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), code)
        val tipForBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            tip.toString()
        )

        RxJavaCloudConnectApiHelper().payWithSignature(
            context, receiptCode, tipForBody, signature_, appKey, authToken
            , this
        )
    }

    fun onDestroy() {
        //    RxJavaApiHandler().onDestroy()
        RxJavaCloudConnectApiHelper().onDestroy()
    }


    interface PayBillView : BasePresenterView {
        fun onSuccessSendCode(response: SendCodeResponse)
        fun setUserHasCC(hasCC: Boolean)
        fun onSuccessPay(amountBilled: Double)
        fun unauthorized(message: String)
    }
}
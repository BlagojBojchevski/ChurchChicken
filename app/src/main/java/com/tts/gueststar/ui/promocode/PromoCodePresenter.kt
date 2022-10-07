package com.tts.gueststar.ui.promocode

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.SubmitPromoCodeRequest
import app.com.relevantsdk.sdk.models.SubmitPromoCodeResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class PromoCodePresenter(var view: PromoCodeView) : ApiListener.SubmitPromoCodeInterface {
    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccess(response: SubmitPromoCodeResponse?) {
        if (response!!.status)
            view.onSuccessSubmitPromo(response)
        else {
            if (!response.message.isEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }


    fun submitPromoCode(
        context: Context,
        promoCode: String,
        auth_token: String
    ) {

        val request = SubmitPromoCodeRequest(
            code = promoCode
        )

        RxJavaApiHandler().submitPromoCode(
            context, request, BuildConfig.APPKEY, auth_token, this
        )
    }

    fun onPromoCodeValidateion(
        promoCode: String
    ) {
        if (promoCode.length < AppConstants.minAnswerCharacters)
            view.disableButton()
        else
            view.enableButton()
    }

    fun onPromoCodeValidateionTest(
        promoCode: String
    ): Boolean {
        return promoCode.length >= AppConstants.minAnswerCharacters
    }

    interface PromoCodeView : BasePresenterView {
        fun disableButton()
        fun enableButton()
        fun onFailureUnauthorized(error: String)
        fun onSuccessSubmitPromo(response: SubmitPromoCodeResponse)
    }

    fun onDestroy() {
        RxJavaApiHandler().onDestroy()
    }
}
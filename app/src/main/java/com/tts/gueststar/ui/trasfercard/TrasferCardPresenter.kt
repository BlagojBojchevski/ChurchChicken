package com.tts.gueststar.ui.trasfercard

import android.content.Context
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.ui.BasePresenterView
import ncruser.interfaces.SendPhysicalCardInterface
import ncruser.models.SendPhisicalCardResponse
import ncruser.models.SendPhysicalCardRequest
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper

class TrasferCardPresenter(var view: TransferCardView) :
    SendPhysicalCardInterface {
    override fun onSuccessSendCard(response: SendPhisicalCardResponse) {
        if (response.status)
            view.onSuccesstransferCard(response)
        else {
            if (response.notice.isNotEmpty())
                view.showError(response.notice)
            else
                view.showGenericError()
        }
    }

    override fun onFailureSendCard(error: String) {
        view.showError(error)
    }

    override fun onFailureSendCardUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onFailureSendCard() {
        view.showGenericError()
    }


    fun addPhysicalCard(
        context: Context,
        card_number: String,
        auth_token: String
    ) {

        val request = SendPhysicalCardRequest(
            appkey = BuildConfig.APPKEY,
            auth_token = auth_token,
            card_number = card_number
        )

        RxJavaCloudConnectApiHelper().addPhysicalCard(
            context, request, this
        )
    }

    fun onTransferCardValidateion(
        card_number: String, repeat_number: String
    ) {
        if ((card_number.length < AppConstants.minTransferCard || repeat_number.length < AppConstants.minTransferCard) || repeat_number != card_number)
            view.disableButton()
        else
            view.enableButton()
    }

    fun onTransferCardValidateionTest(
        card_number: String, repeat_number: String
    ): Boolean {
        return !(card_number.length < AppConstants.minTransferCard || repeat_number.length < AppConstants.minTransferCard || repeat_number != card_number)
    }


    interface TransferCardView : BasePresenterView {
        fun disableButton()
        fun enableButton()
        fun onFailureUnauthorized(error: String)
        fun onSuccesstransferCard(response: SendPhisicalCardResponse)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }
}
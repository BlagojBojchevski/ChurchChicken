package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.RetriveBalanceInterface
import com.tts.olosdk.models.OLOBillingSchemeBalanceResponse
import com.tts.gueststar.ui.BasePresenterView

class AddGiftCardPresenter(var view: AddGiftCardView) : RetriveBalanceInterface {
    override fun onSuccessRetriveBalance(response: OLOBillingSchemeBalanceResponse) {
        view.onSuccessRetriveBalance(response)
    }

    override fun onFailureRetriveBalance(error: String) {
        view.showError(error)
    }

    override fun onFailureRetriveBalance() {
        view.showGenericError()
    }

    fun checkRequirements(
        card: String
    ) {
        if (card.length == 14) {
            view.enableButton()
        } else {
            view.disableButton()
        }
    }


    fun retrieveBalance(
        context: Context,
        schemeId: Long,
        cardnumber: String,
        basketId: String
    ) {
        RxJavaOloApiHelper().retrieveBalance(
            context,
            schemeId,
            cardnumber,
            basketId, this
        )
    }

    interface AddGiftCardView : BasePresenterView {
        fun onSuccessRetriveBalance(response: OLOBillingSchemeBalanceResponse)
        fun disableButton()
        fun enableButton()
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }
}
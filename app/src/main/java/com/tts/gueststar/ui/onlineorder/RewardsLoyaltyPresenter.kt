package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.LoaltyRewardsInterface
import com.tts.olosdk.interfaces.LoaltySchemeInterface
import com.tts.olosdk.models.OLOgetLoyaltyRewardsResponse
import com.tts.olosdk.models.OLOloyalySchemesResponse
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.interfaces.ApplyLoaltyRewardsInterface
import com.tts.olosdk.models.OLOBasketResponse

class RewardsLoyaltyPresenter(var view: RewardsLoyaltyView) : LoaltySchemeInterface,
    LoaltyRewardsInterface, ApplyLoaltyRewardsInterface {
    override fun onSuccessApplyLoyaltyRewards(response: OLOBasketResponse) {
        view.onSuccessApplyRewardToBasket(response)
    }

    override fun onFailureApplyLoyaltyRewards(error: String) {
        view.showError(error)
    }

    override fun onFailureApplyLoyaltyRewards() {
        view.showGenericError()
    }

    override fun onSuccessGetLoyaltyRewards(response: OLOgetLoyaltyRewardsResponse) {
        view.onSuccessGetLoyaltyRewards(response)
    }

    override fun onFailureGetLoyaltyRewards(error: String) {
        view.showError(error)
    }

    override fun onFailureGetLoyaltyRewards() {
        view.showGenericError()
    }

    override fun onSuccessGetLoyaltySchemes(response: OLOloyalySchemesResponse) {
        view.onSuccessGetLoyaltySchemes(response)
    }

    override fun onFailureGetLoyaltySchemes(error: String) {
        view.showError(error)
    }

    override fun onFailureGetLoyaltySchemes() {
        view.showGenericError()
    }


    fun getUserLoyaltySchemes(context: Context, authToken: String, basketId: String) {
        RxJavaOloApiHelper().getUserLoyaltySchemes(context, authToken, basketId, this)
    }

    fun getLoyaltyQualifiedRewards(
        context: Context,
        basketId: String,
        membershipid: Long,
        oloAuthToken: String
    ) {
        RxJavaOloApiHelper().getLoyaltyQualifiedRewards(
            context,
            basketId,
            membershipid,
            oloAuthToken,
            this
        )
    }

    fun applyLoyaltyRewardToBasket(
        context: Context,
        basketId: String,
        membershipid: Long,
        references: List<String>
    ) {
        RxJavaOloApiHelper().applyLoyaltyRewardToBasket(
            context,
            basketId,
            membershipid,
            references,
            this
        )
    }


    interface RewardsLoyaltyView : BasePresenterView {
        fun onSuccessApplyRewardToBasket(response: OLOBasketResponse)
        fun onSuccessGetLoyaltySchemes(response: OLOloyalySchemesResponse)
        fun onSuccessGetLoyaltyRewards(response: OLOgetLoyaltyRewardsResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }
}
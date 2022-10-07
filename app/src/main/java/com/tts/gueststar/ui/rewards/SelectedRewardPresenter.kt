package com.tts.gueststar.ui.rewards

import android.content.Context
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.BonsPlanRedeemRewardInterface
import com.tts.gueststar.ui.BasePresenterView
import ncruser.models.*
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper

class SelectedRewardPresenter(private var context: Context, private var view: SelectedRewardView) :
    BonsPlanRedeemRewardInterface {
    override fun onSuccessdRedeemReward(response: BonusPlanRedeemRewardResponse) {
        view.onSuccessRedeemReward(response)
    }

    override fun onFailureRedeemReward(error: String) {
        view.showError(error)
    }

    override fun onFailureRedeemReward() {
        view.showGenericError()
    }

    override fun onFailureRedeemRewardUnauthorized(error: String) {
        view.unauthorized(error)
    }


    fun redeemReward(ctx: Context, request: BonusPlanRedeemRewardRequest) {
        RxJavaCloudConnectApiHelper().bonusPlanRedeemReward(ctx, request, this)
    }

    interface SelectedRewardView : BasePresenterView {
        fun onSuccessRedeemReward(response: BonusPlanRedeemRewardResponse)
        fun unauthorized(error: String)
    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }
}
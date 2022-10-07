package com.tts.gueststar.ui.rewards

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.RewardsResponse
import com.tts.gueststar.ui.BasePresenterView
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler

class RewardsPresenter(val context: Context, val view: RewardsView) :
    ApiListener.GetRewardsInterface {

    fun getRewards(appkey: String, auth_token: String) {
        RxJavaApiHandler().getRewards(context, appkey, auth_token, this)
    }


    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }


    interface RewardsView : BasePresenterView {
        fun onSuccessGetRewards(response: RewardsResponse?)
        fun onFailureUnauthorized(error: String)
    }

    override fun onSuccess(response: RewardsResponse?) {
        view.onSuccessGetRewards(response)
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

}
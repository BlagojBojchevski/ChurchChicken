package com.tts.gueststar.ui.referfriend

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.UserReferralResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class ReferFriendPresenter(var view: ReferFriendView) : ApiListener.UserReferralInterface {
    override fun onSuccess(response: UserReferralResponse?) {
        if (response!!.status)
            view.onSuccessReferel(response)
        else {
            if (response.message.isNotEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    fun getUserReferral(
        context: Context,
        auth_token: String
    ) {
        RxJavaApiHandler().getUserReferral(
            context, BuildConfig.APPKEY, auth_token, "en", this
        )
    }


    interface ReferFriendView : BasePresenterView {
        fun onFailureUnauthorized(error: String)
        fun onSuccessReferel(response: UserReferralResponse)
    }
    
    fun onDestroy(){
        RxJavaApiHandler().onDestroy()
    }

}
package com.tts.gueststar.ui.notificationcenter

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.NotificationsResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class NotificationCenterPresenter(var view: NotificationCenterView) : ApiListener.GetNotificationsInterface {
    override fun onSuccessGetNotifications(response: NotificationsResponse) {
     view.onSuccessGetNotifications(response)
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

    fun getNotifications(
        context: Context,
        auth_token: String,
        skip: Int,
        take: Int
    ) {
        RxJavaApiHandler().getNotifications(
            context, BuildConfig.APPKEY, auth_token, skip, take, this
        )
    }


    interface NotificationCenterView : BasePresenterView {
        fun onFailureUnauthorized(error: String)
        fun onSuccessGetNotifications(response: NotificationsResponse)
    }

    fun onDestroy(){
        RxJavaApiHandler().onDestroy()
    }
}
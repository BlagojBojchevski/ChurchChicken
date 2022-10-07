package com.tts.gueststar.ui.notificationcenter

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.NotificationReadResponse
import app.com.relevantsdk.sdk.models.ReadNotificationRequest
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class SingleNotificationPresenter(var view: SingleNotificationView) :
    ApiListener.ReadNotificationInterface {
    override fun onSuccessReadNotification(response: NotificationReadResponse) {
        view.onSuccessReadNotification(response)
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

    fun readNotification(
        context: Context,
        auth_token: String,
        notification_id: Int
    ) {

        val request = ReadNotificationRequest(notification_id = notification_id)

        RxJavaApiHandler().readNotification(
            context, request, BuildConfig.APPKEY, auth_token, this
        )
    }


    interface SingleNotificationView : BasePresenterView {
        fun onFailureUnauthorized(error: String)
        fun onSuccessReadNotification(response: NotificationReadResponse)
    }

    fun onDestroy() {
        RxJavaApiHandler().onDestroy()
    }
}
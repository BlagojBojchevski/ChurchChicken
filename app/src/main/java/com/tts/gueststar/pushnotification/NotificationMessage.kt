package com.tts.gueststar.pushnotification

import android.app.Activity
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.tts.gueststar.utility.Engine

class NotificationMessage(
    context: Context,
    bodyMessage: String,
    title: String,
    type: String,
    url: String
) : NotificationModel(context, bodyMessage, title, type, url),
    PushNotificationListener {

    override fun showDialog(activity: Activity, message: RemoteMessage.Notification) {
        val dialog: PushNotificationDialog =
            PushNotificationDialog().getInstance(title, privateMessage, url,type)
        // val homeActivity = context as MainActivity
        Engine().showDialog(Engine.context!!, dialog)
    }
}
package com.tts.gueststar.pushnotification

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

class NotificationFactory {
    fun getNotification(context: Context, message: RemoteMessage.Notification, type: Map<String, String>): PushNotificationListener{
        val pushNotificationListener: PushNotificationListener
        val getType = type["type"]
        val url = type["url"]

        pushNotificationListener = NotificationMessage(context, message.body!!, message.title!!, getType!!, url!!)
        return pushNotificationListener

    }


}
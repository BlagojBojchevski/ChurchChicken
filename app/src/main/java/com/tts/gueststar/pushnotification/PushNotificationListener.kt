package com.tts.gueststar.pushnotification

import android.app.Activity
import com.google.firebase.messaging.RemoteMessage


interface PushNotificationListener {
    fun showDialog(activity: Activity, message: RemoteMessage.Notification)
}
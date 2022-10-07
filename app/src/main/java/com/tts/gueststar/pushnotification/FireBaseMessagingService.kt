package com.tts.gueststar.pushnotification

import android.os.Handler
import android.os.Looper
import com.appboy.AppboyFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tts.gueststar.MainActivity
import com.tts.gueststar.utility.Engine

class FireBaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var title: String? = ""
        if (remoteMessage.notification != null) {
            if (remoteMessage.notification!!.title != null)
                title = remoteMessage.notification!!.title
            val message = remoteMessage.notification!!.body
            if (!title!!.isEmpty() || !message!!.isEmpty()) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    val activity = Engine.context as MainActivity
                    activity.setMessage(remoteMessage.notification, 0 , remoteMessage.data)
                }
            }
        }

        if (AppboyFirebaseMessagingService.handleBrazeRemoteMessage(this, remoteMessage)) {
            // This Remote Message originated from Braze and a push notification was displayed.
            // No further action is needed.
        } else {
            // This Remote Message did not originate from Braze.
            // No action was taken and you can safely pass this Remote Message to other handlers.
        }
    }
}
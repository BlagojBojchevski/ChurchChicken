package com.tts.gueststar.pushnotification

import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.tts.gueststar.interfaces.FirebaseTokenListener
import com.tts.gueststar.utility.Engine


class ActivatePushNotifications( var activity: Activity) {

    fun notificationToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(object :
            OnCompleteListener<String?> {
            override fun onComplete(@NonNull task: Task<String?>) {
                if (!task.isSuccessful) {
                    Log.e("Error: ", task.toString())
                    return
                }
                val token: String = task.result.toString()
                if (token.isNotEmpty()) {
                    Engine().setNotificationToken(token)
                    (activity as FirebaseTokenListener).onTokenReceived(token)
                }
            }
        }).addOnFailureListener { e ->
            Log.e("Error: ", e.toString())
        }
    }
}
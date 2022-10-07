package com.tts.gueststar.pushnotification

import android.content.Context


open class NotificationModel(
    var context: Context,
    var privateMessage: String,
    var title: String,
    var url: String,
    var type: String)
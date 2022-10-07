package com.tts.gueststar.interfaces

import app.com.relevantsdk.sdk.models.Notification_

interface OnMessageClicked {
    fun onMessageClick(notificaton: Notification_)
}
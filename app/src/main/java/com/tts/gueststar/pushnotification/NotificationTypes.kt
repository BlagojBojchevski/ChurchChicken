package com.tts.gueststar.pushnotification

enum class NotificationTypes(val type:String){
    UNDEFINED("0"),
    MESSAGE("1"),
    POINTS("2"),
    REWARDS("3"),
    URL("4"),
    SESSION_EXPIRED("5")
}
package com.tts.gueststar.utility

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object MySharedPreferences {

    private const val defaultSharedKey = "MySharedPreferences"
    const val keyIsTutorialShown = "keyIsTutorialShown"
    const val keyIsPayOn = "keyIsPayOn"
    const val rateAppShown = "rateAppShown"
    const val userProfile = "userProfile"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(defaultSharedKey, MODE_PRIVATE)
    }

    fun putBoolean(context: Context, key: String, data: Boolean) {
        getSharedPreferences(context).edit().putBoolean(key, data).apply()
    }

    fun getBoolean(context: Context, key: String): Boolean {
        return getSharedPreferences(context).getBoolean(key, false)
    }

    fun putString(context: Context, key: String, data: String) {
        getSharedPreferences(context).edit().putString(key, data).apply()
    }

    fun getString(context: Context, key: String): String? {
        return getSharedPreferences(context).getString(key, "")
    }
}
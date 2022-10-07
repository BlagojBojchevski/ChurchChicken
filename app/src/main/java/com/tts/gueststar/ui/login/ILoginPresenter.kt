package com.tts.gueststar.ui.login

import android.content.Context

interface ILoginPresenter {
    fun onLoginValidation(email: String, password: String, isChecked: Boolean)
    fun onLoginValidationTest(email: String, password: String, isChecked: Boolean): Boolean
    fun onLogin(
        context: Context,
        email: String,
        password: String,
        android_id: String,
        notificationToken: String
    )
}
package com.tts.gueststar

import android.app.Application
import android.util.Log
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.support.AppboyLogger
import com.appboy.ui.inappmessage.AppboyInAppMessageManager
import com.tts.gueststar.di.AppComponent
import com.tts.gueststar.di.DaggerAppComponent
import com.tts.gueststar.di.AppModule


class MyApplication : Application() {
    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppboyLifecycleCallbackListener())
        AppboyInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(this)
        component = DaggerAppComponent.builder().appModule(AppModule(applicationContext)).build()
    }

    fun getMyComponent(): AppComponent {
        return component
    }
}
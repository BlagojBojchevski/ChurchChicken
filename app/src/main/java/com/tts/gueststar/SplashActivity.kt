package com.tts.gueststar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tts.gueststar.utility.MySharedPreferences
import java.util.*
import kotlin.concurrent.timerTask


class SplashActivity : AppCompatActivity() {

    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handlePushNotifications()
        Timer().schedule(timerTask { nextScreen() }, 2000)
    }

    private fun nextScreen() {

        val isTutorialShown = MySharedPreferences.getBoolean(applicationContext, MySharedPreferences.keyIsTutorialShown)
        if (!isTutorialShown) {
            MySharedPreferences.putBoolean(applicationContext, MySharedPreferences.keyIsTutorialShown, true)
            val i = Intent(this@SplashActivity, TutorialActivity::class.java)
            i.putExtra("fromAccount", "")
            startActivity(i)
            finish()
        } else {
            val i = Intent(this@SplashActivity, MainActivity::class.java)
            if (bundle != null)
                i.putExtras(bundle!!)
            startActivity(i)
            finish()
        }
    }


    private fun handlePushNotifications() {
        val intent = intent
        bundle = intent.extras
    }
}

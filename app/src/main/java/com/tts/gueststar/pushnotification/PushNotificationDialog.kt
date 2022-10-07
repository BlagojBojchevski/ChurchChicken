package com.tts.gueststar.pushnotification

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.tts.gueststar.MainActivity
import com.tts.gueststar.fragments.*
import com.tts.gueststar.fragments.rewards.RewardsFragment
import com.tts.gueststar.fragments.userauth.ForgotPasswordFragment
import com.tts.gueststar.fragments.userauth.LogInFragment
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.fragments.userauth.SignUpFragment
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine

class PushNotificationDialog : DialogFragment() {
    private val bundleMessage = "bundleMessage"
    private val bundleTitle = "bundleTitle"
    private val bundleType = "bundleType"
    private val bundleUrl = "bundleUrl"

    private var mHomePage: MainActivity? = null


    private fun openUrlInBrowser(url: String) {
        val finalUrl: String = if (!url.startsWith("http://") && !url.startsWith("https://"))
            "http://$url"
        else url
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
        startActivity(browserIntent)
    }

    fun getInstance(
        title: String,
        message: String,
        type: String,
        url: String
    ): PushNotificationDialog {
        val info = PushNotificationDialog()
        val infoBundle = Bundle()
        infoBundle.putString(bundleTitle, title)
        infoBundle.putString(bundleMessage, message)
        infoBundle.putString(bundleType, type)
        infoBundle.putString(bundleUrl, url)
        info.arguments = infoBundle
        return info
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var message = arguments!!.getString(bundleMessage)
        val title = arguments!!.getString(bundleTitle)
        val type = arguments!!.getString(bundleType)
        val url = arguments!!.getString(bundleUrl)
        if (type != null && type == NotificationTypes.URL.type) {
            if (url != null && !url.isEmpty())
                message = message + "\n\n" + url
        }

        // mHomePage = activity as MainActivity?

        val alertDialogBuilder = AlertDialog.Builder(Engine.context)
        if (title != null)
            alertDialogBuilder.setTitle(title)
        else
            alertDialogBuilder.setTitle(AppConstants.PUSH_NOTIFICATION_TAG)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            if (dialog != null) {
                dialog.dismiss()
                if (type != null && !type.isEmpty()) {
                    if (type == NotificationTypes.URL.type)
                        openUrlInBrowser(url!!)
                    else if (type == NotificationTypes.SESSION_EXPIRED.type) {
                        mHomePage = Engine.context as MainActivity?
                        checkCurrentPage(mHomePage!!)
                    } else if (type == NotificationTypes.REWARDS.type || type == NotificationTypes.POINTS.type) {
                        mHomePage = Engine.context as MainActivity?
                        mHomePage!!.presenter.openFragment(
                            RewardsFragment(),
                            AppConstants.TAG_REWARDS
                        )
                    }
                }
            }
        }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }

        return alertDialogBuilder.create()
    }

    private fun checkCurrentPage(mHomePage: MainActivity) {
        val fragment = mHomePage.getCurrentFragment()
        if (fragment !is ForgotPasswordFragment &&
            fragment !is HomeFragment &&
            fragment !is LogInFragment &&
            fragment !is MainSignUpFragment &&
            fragment !is SignUpFragment &&
            fragment !is WebViewFragment
        ) {
            mHomePage.presenter.openFragmentUp(MainSignUpFragment(), AppConstants.TAG_REWARDS)
        }
    }
}
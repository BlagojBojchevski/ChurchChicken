package com.tts.gueststar.fragments.notificationcenter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.NotificationReadResponse
import app.com.relevantsdk.sdk.models.Notification_
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.notificationcenter.SingleNotificationPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_single_notification.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class SingleNotificationFragment : BaseFragment(),
    SingleNotificationPresenter.SingleNotificationView, OnClickListener,
    View.OnTouchListener {
    override fun onSuccessReadNotification(response: NotificationReadResponse) {
        homeActivity.presenter.dismissProgress()
    }

    private var notification: Notification_? = null
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: SingleNotificationPresenter
    private lateinit var app: MyApplication
    private var fromHome = false
    @Inject
    lateinit var mySharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            notification = arguments!!.getParcelable(AppConstants.SELECTED_NOTIFICATION)
            fromHome = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_single_notification, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = SingleNotificationPresenter(this)
        setListeners()

        val stamp = Timestamp(notification?.created_at!!.toLong())
        val d = Date(stamp.time * 1000)
        val f = SimpleDateFormat("MMM dd, yyyy")
        f.timeZone = TimeZone.getTimeZone("GMT")

        title.text = f.format(d).toString()
        full_message.text = notification?.notification_text


        if (notification?.type == 4) {
            if (notification?.message_url != null && notification?.message_url!!.isNotEmpty()) {
                url.visibility = View.VISIBLE
                url.text = notification?.message_url
                url.paintFlags =
                    url.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                url.setOnClickListener {
                    openHyperlink(notification?.message_url.toString())
                }
            }
        }

        if (Engine().isNetworkAvailable(homeActivity)) {
            if (!notification!!.notification_read_status) {
                homeActivity.presenter.showProgress()
                presenter.readNotification(
                    homeActivity,
                    Engine().getAuthToken(mySharedPreferences).toString(),
                    notification!!.notification_id
                )
            }
        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
        }

        if (fromHome) {
            btn_close.contentDescription = getString(R.string.close_button)
            btn_close.setImageResource(R.drawable.close_icon)
        } else {
            btn_close.contentDescription = getString(R.string.back_button)
            btn_close.setImageResource(R.drawable.arrow_white_left)
        }

    }

    private fun openHyperlink(urlpm: String) {
        var url = urlpm
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://$url"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        homeActivity.startActivity(browserIntent)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

            R.id.btn_done -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
        }
    }


    private fun setListeners() {
        btn_done.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
    }


    override fun showGenericError() {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(
            getString(R.string.app_name),
            getString(R.string.handle_blank_pop_up),
            homeActivity
        )
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p0?.id) {
            R.id.mainLayout -> {
                Engine().hideSoftKeyboard(homeActivity)
            }
        }
        return true
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_NOTIFICATIONS_CENTER
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

}
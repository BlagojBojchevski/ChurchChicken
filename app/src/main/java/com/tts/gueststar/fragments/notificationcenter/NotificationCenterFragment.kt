package com.tts.gueststar.fragments.notificationcenter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.Notification_
import app.com.relevantsdk.sdk.models.NotificationsResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.MessagesAdapter
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.OnMessageClicked
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.notificationcenter.NotificationCenterPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_notification_center.*
import javax.inject.Inject

class NotificationCenterFragment : BaseFragment(),
    NotificationCenterPresenter.NotificationCenterView, OnClickListener,
    View.OnTouchListener {
    override fun onSuccessGetNotifications(response: NotificationsResponse) {
        empmty.addAll(response.notifications)
        homeActivity.presenter.dismissProgress()
        adapter.notifyDataSetChanged()
    }

    private var empmty = mutableListOf<Notification_>()
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: NotificationCenterPresenter
    private lateinit var app: MyApplication
    private lateinit var adapter: MessagesAdapter
    private var skip = 0
    private var take = 10
    @Inject
    lateinit var mySharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)

        if (Engine().isNetworkAvailable(homeActivity)) {
            skip = 0
            take = 10
            empmty.clear()
            adapter.notifyDataSetChanged()
            homeActivity.presenter.showProgress()
            presenter.getNotifications(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString(),
                0,
                10
            )
        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = NotificationCenterPresenter(this)
        setListeners()
        adapter = MessagesAdapter(homeActivity, empmty, object : OnMessageClicked {
            override fun onMessageClick(notificaton: Notification_) {
                homeActivity.presenter.openFragmentRight(
                    getFragment(
                        SingleNotificationFragment(),
                        notificaton
                    ), AppConstants.TAG_ACCOUNT
                )
            }
        })
        rvNotifications.adapter = adapter
        val layoutManager =
            LinearLayoutManager(homeActivity.applicationContext)
        rvNotifications.layoutManager = layoutManager
        rvNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerViewPharmacies: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == 9 + skip) {
                    skip = take
                    take += 10
                    homeActivity.presenter.showProgress()
                    presenter.getNotifications(
                        homeActivity,
                        Engine().getAuthToken(mySharedPreferences).toString(),
                        skip,
                        take
                    )
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
        }
    }


    private fun setListeners() {
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

    private fun getFragment(fragment: BaseFragment, selected_notif: Notification_): BaseFragment {
        val bundle = Bundle()
        bundle.putParcelable(AppConstants.SELECTED_NOTIFICATION, selected_notif)
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

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

}
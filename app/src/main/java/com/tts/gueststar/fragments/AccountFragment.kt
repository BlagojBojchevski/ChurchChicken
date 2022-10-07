package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.com.relevantsdk.sdk.models.DeactivateAccountResponse
import app.com.relevantsdk.sdk.models.DeactivateAccountWarnMessageResponse
import app.com.relevantsdk.sdk.models.NotificationCountResponse
import app.com.relevantsdk.sdk.models.SignOutResponse
import com.squareup.picasso.Picasso
import com.tts.gueststar.*
import com.tts.gueststar.fragments.contact.ContactUsFragment
import com.tts.gueststar.fragments.notificationcenter.NotificationCenterFragment
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.fragments.userauth.PersonalSettingsFragment
import com.tts.gueststar.fragments.userauth.UpdatePasswordFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.account.AccountPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_account.*
import javax.inject.Inject

class AccountFragment : BaseFragment(), View.OnClickListener, AccountPresenter.AccountView {

    private lateinit var homeActivity: MainActivity

    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: AccountPresenter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(3)
        app.getMyComponent().inject(this)
        presenter = AccountPresenter(this)
        setListeners()
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(3)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)

        if (Engine().checkIfLogin(mySharedPreferences)) {
            btn_logout.visibility = View.VISIBLE
            profile_info.visibility = View.VISIBLE
            btn_delete.visibility = View.VISIBLE
            if (Engine().isNetworkAvailable(homeActivity)) {
                presenter.getNotificationCount(
                    homeActivity,
                    Engine().getAuthToken(mySharedPreferences).toString()
                )

                if (Engine().getUserProfileImage(mySharedPreferences) != "")
                    Picasso.get()
                        .load(Engine().getUserProfileImage(mySharedPreferences))
                        .placeholder(R.drawable.avatar)
                        .into(profile_image)
            }
            profile_first_name.text = "Hi " + Engine().getUserFirstName(mySharedPreferences) + "!"

        } else {
            btn_logout.visibility = View.GONE
            profile_info.visibility = View.GONE
            btn_delete.visibility = View.GONE
        }

    }


    private fun setListeners() {
        btn_promo_code.setOnClickListener(this)
        btn_logout.setOnClickListener(this)
        btn_delete.setOnClickListener(this)
        btn_update_password.setOnClickListener(this)
        btn_personal_settings.setOnClickListener(this)
//        btn_refer_friend.setOnClickListener(this)
        profile_image.setOnClickListener(this)
        btn_preferences.setOnClickListener(this)
        btn_facebook.setOnClickListener(this)
        btn_twitter.setOnClickListener(this)
        btn_instagram.setOnClickListener(this)
        btn_contact_us.setOnClickListener(this)
        btn_faq.setOnClickListener(this)
        btn_activity_history.setOnClickListener(this)
        btn_tutorial.setOnClickListener(this)
        btn_messages.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.btn_tutorial -> {
                val i = Intent(homeActivity, TutorialActivity::class.java)
                i.putExtra("fromAccount", "true")
                homeActivity.startActivity(i)
                homeActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.hold)
            }

            R.id.btn_faq -> {
                homeActivity.presenter.openFragmentUp(FaqFragment(), AppConstants.TAG_ACCOUNT)
            }

            R.id.btn_messages -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        NotificationCenterFragment(),
                        AppConstants.TAG_ACCOUNT
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_NOTIFICATIONS_CENTER
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_logout -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.logOut(
                        Engine().getAuthToken(mySharedPreferences).toString(),
                        homeActivity
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_delete -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.deactivateAccountWarnMessage(
                        Engine().getAuthToken(mySharedPreferences).toString(),
                        homeActivity
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }


            R.id.profile_image -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        PersonalSettingsFragment(),
                        AppConstants.TAG_PERSONAL_SETTINGS
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_PERSONAL_SETTINGS
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_activity_history -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        getFragmentActivityHistory(
                            ActivityHistoryFragment()
                        ), AppConstants.TAG_ACCOUNT
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_ACTIVITY_HISTORY
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_personal_settings -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        PersonalSettingsFragment(),
                        AppConstants.TAG_ACCOUNT
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_PERSONAL_SETTINGS
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

//            R.id.btn_refer_friend -> {
//                if (Engine().checkIfLogin(mySharedPreferences)) {
//                    homeActivity.presenter.openFragmentUp(
//                        ReferFriendFragment(),
//                        AppConstants.TAG_ACCOUNT
//                    )
//                } else {
//                    Engine.setNextPage = AppConstants.TAG_REFER_FRIEND
//                    homeActivity.presenter.openFragmentUp(
//                        getFragment(
//                            MainSignUpFragment()
//                        ), AppConstants.TAG_MAIN_SIGN_UP2
//                    )
//                }
//            }

            R.id.btn_promo_code -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        PromoCodeFragment(),
                        AppConstants.TAG_ACCOUNT
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_PROMO
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_update_password -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentUp(
                        UpdatePasswordFragment(),
                        AppConstants.TAG_ACCOUNT
                    )
                } else {
                    Engine.setNextPage = AppConstants.TAG_UPDATE_PASSWORD
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_preferences -> {
                val intent = Intent()
                intent.action = ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", homeActivity.packageName, null)
                intent.data = uri
                homeActivity.startActivity(intent)
            }

            R.id.btn_facebook -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            WebViewSocialFragment(),
                            BuildConfig.FACEBOOK_URL,
                            getString(R.string.title_facebook)
                        ), AppConstants.TAG_WEB_VIEW
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_twitter -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            WebViewSocialFragment(),
                            BuildConfig.TWITTER_URL,
                            getString(R.string.title_twitter)
                        ), AppConstants.TAG_WEB_VIEW
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_instagram -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.openFragmentUp(
                        getFragment(
                            WebViewSocialFragment(),
                            BuildConfig.INSTAGRAM_URL,
                            getString(R.string.title_instagram)
                        ), AppConstants.TAG_WEB_VIEW
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_contact_us -> {
                homeActivity.presenter.openFragmentUp(
                    ContactUsFragment(),
                    AppConstants.TAG_CONTACT_US
                )
            }

        }
    }

    private fun getFragmentActivityHistory(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_REWARDS_ACTIVITY_HISTORY, false)
        fragment.arguments = bundle
        return fragment
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }


    override fun onSuccessLogOut(response: SignOutResponse) {
        homeActivity.presenter.dismissProgress()
        homeActivity.clearOrderHelper()
        Engine().clearDataAfterLogOut(mySharedPreferences)

        if (response.message.isNotEmpty())
            Engine().showMsgDialog(getString(R.string.app_name), response.message, homeActivity)

        btn_logout.visibility = View.GONE
        profile_info.visibility = View.GONE
        btn_delete.visibility = View.GONE
    }

    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
    }

    override fun onSuccessNotificationCount(response: NotificationCountResponse) {
        try {
            if (response.notifications_count > 0) {
                notif_icon.visibility = View.VISIBLE
            } else {
                notif_icon.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
        }
    }

    override fun onSuccessGetWarning(response: DeactivateAccountWarnMessageResponse) {
        homeActivity.presenter.dismissProgress()

        if (response.message.isNotEmpty())
            Engine().showDeleteWarnMsgDialog(
                response.title,
                response.message,
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString(),
                presenter
            )


    }

    override fun onSuccessDeleteAccount(response: DeactivateAccountResponse) {
        homeActivity.clearOrderHelper()
        Engine().clearDataAfterLogOut(mySharedPreferences)

        if (response.message.isNotEmpty())
            Engine().showMsgDialog(
                "",
                response.message,
                homeActivity
            )

        btn_logout.visibility = View.GONE
        profile_info.visibility = View.GONE
        btn_delete.visibility = View.GONE
    }

    override fun showGenericError() {
        try {
            homeActivity.presenter.dismissProgress()
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.handle_blank_pop_up),
                homeActivity
            )
        } catch (er: IllegalStateException) {
        }
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragment(fragment: BaseFragment, url: String, title: String): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.WEB_VIEW_URL, url)
        bundle.putString(AppConstants.WEB_VIEW_TITLE, title)
        fragment.arguments = bundle
        return fragment
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

}
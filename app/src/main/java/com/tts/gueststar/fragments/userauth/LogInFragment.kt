package com.tts.gueststar.fragments.userauth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import app.com.relevantsdk.sdk.models.OauthTokenResponse
import app.com.relevantsdk.sdk.models.SignInResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import app.com.relevantsdk.sdk.utils.Utils
import com.appboy.Appboy
import com.appboy.enums.Month
import com.appboy.enums.NotificationSubscriptionType
import com.google.gson.Gson
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.WebViewFragment
import com.tts.gueststar.interfaces.OpenFragmentListener
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.login.ILoginView
import com.tts.gueststar.ui.login.LoginPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.MySharedPreferences
import com.tts.olosdk.models.OLOGetOrCreateResponse
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject


class LogInFragment : BaseFragment(), ILoginView, OnClickListener, View.OnTouchListener {
    override fun onSuccessGetOrcreate(response: OLOGetOrCreateResponse) {
        homeActivity.presenter.dismissProgress()
        Engine().putStringData(
            mySharedPreferences,
            AppConstants.PREFAUTH_OLO_TOKEN,
            response.authtoken
        )
        (homeActivity as OpenFragmentListener).openFragment()
    }

    override fun onSuccessOloSettings() {
        presenter.getOrCreate(
            homeActivity,
            Engine().getAccessToken(mySharedPreferences)!!,
            Engine().getUserMobilePhone(mySharedPreferences)!!
        )
    }

    private var isVisiblePassword = false
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: LoginPresenter
    private lateinit var app: MyApplication
    private lateinit var mHandler: Handler
    private var isFromBottomNavigation = true

    @Inject
    lateinit var mySharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        if (isFromBottomNavigation) {
            btn_close.setImageResource(R.drawable.close_icon)
            btn_close.contentDescription = getString(R.string.close_button)
        } else {
            btn_close.setImageResource(R.drawable.arrow_white_left)
            btn_close.contentDescription = getString(R.string.back_button)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromBottomNavigation = requireArguments().getBoolean(AppConstants.FROM_NAVIGATION)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = LoginPresenter(this)
        mHandler = Handler()

        setListeners()
        setTextWatcherForAmountEditView(etLoginEmail)
        setTextWatcherForAmountEditView(etLoginPassword)
        setSpannableText()
        Engine().setEnableButton(btn_login, false)

        cbTermsOfUse.setOnCheckedChangeListener { _, _ ->
            Engine().hideSoftKeyboard(homeActivity)
            presenter.onLoginValidation(
                etLoginEmail?.text.toString(),
                etLoginPassword.text.toString(),
                cbTermsOfUse.isChecked
            )
        }
        btn_forgot_passwoord.paintFlags =
            btn_forgot_passwoord.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.btn_login -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()

                    val notifToken = Engine().getNotificationToken()
                    Engine().saveDeviceToken(mySharedPreferences, notifToken)

                    if (notifToken.isNotEmpty()) {
                        presenter.onLogin(
                            homeActivity,
                            etLoginEmail?.text.toString(),
                            etLoginPassword.text.toString(),
                            Engine().getAndroidId(homeActivity),
                            notifToken
                        )
                    } else {
                        showGenericError()
                    }
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }
            R.id.btn_forgot_passwoord -> {
                homeActivity.presenter.openFragmentRight(
                    ForgotPasswordFragment(),
                    AppConstants.TAG_SIGN_UP
                )
            }

            R.id.spanable_2 -> {
                if (Engine().isNetworkAvailable(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            WebViewFragment(),
                            BuildConfig.TERMS_OF_USE_URL,
                            getString(R.string.title_terms_of_use)
                        ), AppConstants.TAG_WEB_VIEW
                    )
                else Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }

            R.id.spanable_4 -> {
                if (Engine().isNetworkAvailable(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            WebViewFragment(),
                            BuildConfig.PRIVACY_URL,
                            getString(R.string.title_privacy_policy)
                        ), AppConstants.TAG_WEB_VIEW
                    )
                else Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }

            R.id.btn_clear_email -> {
                btn_clear_email.visibility = View.GONE
                etLoginEmail?.setText("")
            }
            R.id.btn_hide_show -> {
                if (isVisiblePassword) {
                    isVisiblePassword = false
                    btn_hide_show.setBackgroundResource(R.drawable.eye_on)
                    etLoginPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etLoginPassword.setSelection(etLoginPassword.text!!.length)
                } else {
                    isVisiblePassword = true
                    btn_hide_show.setBackgroundResource(R.drawable.eye_off)
                    etLoginPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    etLoginPassword.setSelection(etLoginPassword.text!!.length)
                }
            }
        }
    }


    private fun setListeners() {
        btn_close.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        btn_forgot_passwoord.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
        spanable_2.setOnClickListener(this)
        spanable_4.setOnClickListener(this)
        btn_clear_email.setOnClickListener(this)
        btn_hide_show.setOnClickListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText?) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (etLoginEmail?.text.toString().isNotEmpty()) {
                    btn_clear_email.visibility = View.VISIBLE
                } else {
                    btn_clear_email.visibility = View.GONE
                }

                presenter.onLoginValidation(
                    etLoginEmail?.text.toString(),
                    etLoginPassword.text.toString(),
                    cbTermsOfUse.isChecked
                )
            }

        }
        amountEditText?.addTextChangedListener(fieldValidatorTextWatcher)
    }

    private fun setSpannableText() {
        spanable_2.underline()
        spanable_4.underline()
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun disableLogin() {
        Engine().setEnableButton(btn_login, false)
    }

    override fun enableLogin() {
        Engine().setEnableButton(btn_login, true)
    }

    override fun successfulLogin(response: SignInResponse) {
        Engine().saveData(
            mySharedPreferences,
            response.auth_token,
            "",
            "",
            "",
            etLoginEmail?.text.toString(),
            response.hashed_user_id,
            response.hashed_email
        )

        presenter.getUserProfile(
            Engine().getAuthToken(mySharedPreferences).toString(),
            homeActivity
        )

        if (response.message.isNotEmpty())
            Engine().showMsgDialog(getString(R.string.app_name), response.message, homeActivity)
    }

    private fun addAttributesToBraze(
        name: String,
        lastName: String,
        phone: String,
        birthday: Long?
    ) {
        val email = etLoginEmail.text.toString()
        Appboy.getInstance(context).currentUser?.setEmail(email)
        Appboy.getInstance(context).currentUser?.setFirstName(name)
        Appboy.getInstance(context).currentUser?.setLastName(lastName)
        Appboy.getInstance(context).currentUser?.setPhoneNumber(phone)
        val birthday = birthday
        val defaultBirthday =
            -2208988793 //for some reason we receive default date if user have not enter date of birth
        if (birthday != null && birthday != defaultBirthday) {
            val date = Utils.getDateCurrentTimeZone(birthday)
            if (date != null) {
                val year = Utils.getFromDate(date, 1)
                val month = Utils.getFromDate(date, 2)
                val day = Utils.getFromDate(date, 3)
                val monthO = Month.getMonth(month)
                Appboy.getInstance(context).currentUser?.setDateOfBirth(year, monthO, day)
            }
        }
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

    override fun successGetUserProfile(response: UserProfileResponse?) {
        // homeActivity.presenter.dismissProgress()
        val json = Gson().toJson(response)
        MySharedPreferences.putString(
            homeActivity,
            MySharedPreferences.userProfile,
            json
        )

        var birthday = AppConstants.defaultTimestamp
        if (response?.user_data?.birthday != null) {
            birthday = response.user_data.birthday!!
        }

        response?.user_data?.let {
            Engine().saveUserProfileData(
                mySharedPreferences,
                response.user_data.first_name,
                response.user_data.last_name,
                response.user_data.phone_number,
                response.user_data.favorite_location.app_display_text,
                response.user_data.favorite_location.id,
                birthday,
                response.user_data.profile_picture_url,
                response.user_data.hashed_user_id,
                response.user_data.hashed_email
            )

            when {
                response.user_data.marketing_info.marketing -> {
                    Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(
                        NotificationSubscriptionType.OPTED_IN
                    )
                }
                else -> {
                    Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(
                        NotificationSubscriptionType.UNSUBSCRIBED
                    )
                }
            }

            presenter.getOauthToken(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString()
            )

            val email = etLoginEmail.text.toString()
            if (response.user_data.hashed_email.isNotEmpty())
                Appboy.getInstance(context).changeUser(response.user_data.hashed_email)
//        if (response.hashed_email.isNotEmpty())
//        Appboy.getInstance(context).currentUser?.addAlias(email, "EMAIL")


            addAttributesToBraze(
                response.user_data.first_name,
                response.user_data.last_name,
                response.user_data.phone_number,
                response.user_data.birthday
            )

        }
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

    override fun failedGetUserProfile() {
        (homeActivity as OpenFragmentListener).openFragment()
    }

    override fun onSuccessGetOuathToken(response: OauthTokenResponse) {
        Engine().putStringData(
            mySharedPreferences,
            AppConstants.ACCESS_TOKEN,
            response.access_token
        )
        presenter.getoloSettings(homeActivity)
    }

    override fun onFailedOauthToken(error: String) {
        homeActivity.presenter.dismissProgress()
        showError(error)
    }

    override fun onFailedOauthToken() {
        homeActivity.presenter.dismissProgress()
        showGenericError()
    }

    override fun onFailedOauthTokenUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        showError(error)
    }

    private fun getFragment(fragment: BaseFragment, url: String, title: String): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.WEB_VIEW_URL, url)
        bundle.putString(AppConstants.WEB_VIEW_TITLE, title)
        fragment.arguments = bundle
        return fragment
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

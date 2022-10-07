package com.tts.gueststar.fragments.userauth

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import app.com.relevantsdk.sdk.models.OauthTokenResponse
import app.com.relevantsdk.sdk.models.SignUpResponse
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
import com.tts.gueststar.fragments.FavoriteLocationFragment
import com.tts.gueststar.fragments.WebViewFragment
import com.tts.gueststar.interfaces.OpenFragmentListener
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.signuppresenter.SignUpPresenter
import com.tts.gueststar.utility.*
import com.tts.olosdk.models.OLOGetOrCreateResponse
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class SignUpFragment : BaseFragment(), View.OnClickListener, SignUpPresenter.SignUpInterface,
    AdapterView.OnItemSelectedListener {

    private var isVisiblePassword = false
    private lateinit var homeActivity: MainActivity
    private var cal = Calendar.getInstance()
    private var favLocationId: Int = 0
    private lateinit var presenter: SignUpPresenter
    private var termsAgreed = false
    private var optInChecked = false

    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var birthday: Long? = null
    private var dobDay: Int = 0
    private var dobMonth: Int = 0
    private var dobYear: Int = 0
    private lateinit var app: MyApplication
    private var isFromBottomNavigation = true
    private lateinit var questionsList: ArrayList<String>
    private var selectedQuestion = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        if (isFromBottomNavigation) {
            btn_close.setBackgroundResource(R.drawable.close_icon)
            btn_close.contentDescription = getString(R.string.close_button)
        } else {
            btn_close.setBackgroundResource(R.drawable.arrow_white_left)
            btn_close.contentDescription = getString(R.string.back_button)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromBottomNavigation = requireArguments().getBoolean(AppConstants.FROM_NAVIGATION)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = SignUpPresenter(homeActivity, this)
        setListeners()
        setSpannableText()
        disableButton()
    }

    private fun setListeners() {
        tvBirthday.setOnClickListener(this)
        tvFavLocation.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        layoutFavLocation.setOnClickListener(this)
        layoutBirthday.setOnClickListener(this)
        tvBirthday.setOnClickListener(this)
        tvFavLocation.setOnClickListener(this)
        spanable_2.setOnClickListener(this)
        spanable_4.setOnClickListener(this)
        btn_clear_email.setOnClickListener(this)
        btn_hide_show.setOnClickListener(this)
        btn_clear_firstname.setOnClickListener(this)
        btn_clear_lastname.setOnClickListener(this)
        btn_clear_phone.setOnClickListener(this)
        btn_clear_zip.setOnClickListener(this)
        btn_clear_referral.setOnClickListener(this)

        etSignUpPhoneNumber.addTextChangedListener(PhoneNumberTextWatcher())
        etSignUpPhoneNumber.filters = arrayOf(PhoneNumberFilter())

        etSignUpFirstName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
        etSignUpLastName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))

        setTextWatcherForAmountEditView(etSignUpEmail)
        setTextWatcherForAmountEditView(etSignUpPassword)
        setTextWatcherForAmountEditView(etSignUpFirstName)
        setTextWatcherForAmountEditView(etSignUpLastName)
        setTextWatcherForAmountEditView(etSignUpPhoneNumber)
        setTextWatcherForAmountEditView(etSignUpZip)

        cbTouPp.setOnCheckedChangeListener { _, checked ->
            termsAgreed = checked

            checkRequiredFields()
        }

        cbOptIn.setOnCheckedChangeListener { _, checked ->
            optInChecked = checked
        }

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

            R.id.layoutBirthday -> {
                Engine().hideSoftKeyboard(homeActivity)
                openDatePicker()
            }

            R.id.tvBirthday -> {
                Engine().hideSoftKeyboard(homeActivity)
                openDatePicker()
            }

            R.id.layoutFavLocation -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        FavoriteLocationFragment(),
                        AppConstants.TAG_FAV_LOCATION
                    )
            }

            R.id.tvFavLocation -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        FavoriteLocationFragment(),
                        AppConstants.TAG_FAV_LOCATION
                    )
            }
            R.id.btnSignUp -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    val notifToken = Engine().getNotificationToken()
                    Engine().saveDeviceToken(mySharedPreferences, notifToken)

                    presenter.signUp(
                        etSignUpFirstName.text.toString(),
                        etSignUpLastName.text.toString(),
                        etSignUpEmail.text.toString(),
                        etSignUpPassword.text.toString(),
                        etSignUpPhoneNumber.text.toString().replace("-", ""),
                        Engine().getAndroidId(homeActivity),
                        homeActivity.getGPSController()!!.latitude,
                        homeActivity.getGPSController()!!.longitude,
                        "Android",
                        AppConstants.signUpConnectType,
                        etSignUpReferralCode.text.toString(),
                        "Android",
                        notifToken,
                        AppConstants.manufacturer + " " + AppConstants.model,
                        AppConstants.androidOS,
                        optInChecked,
                        favLocationId.toString(),
                        etSignUpZip.text.toString(),
                        birthday

                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btnBirthdayAction -> {
                tvBirthday.text = getString(R.string.hint_birthday_date)
                tvBirthday.setTextColor(
                    ContextCompat.getColor(
                        homeActivity,
                        R.color.edit_text_color_hint
                    )
                )
                dobDay = 0
                dobMonth = 0
                dobYear = 0
                birthday = null
                btnBirthdayAction.setOnClickListener(null)
                btnBirthdayAction.setBackgroundResource(R.drawable.arow_down)
                checkRequiredFields()
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
                etSignUpEmail.setText("")
            }

            R.id.btn_clear_firstname -> {
                btn_clear_firstname.visibility = View.GONE
                etSignUpFirstName.setText("")
            }
            R.id.btn_clear_lastname -> {
                btn_clear_lastname.visibility = View.GONE
                etSignUpLastName.setText("")
            }
            R.id.btn_clear_phone -> {
                btn_clear_phone.visibility = View.GONE
                etSignUpPhoneNumber.setText("")
            }
            R.id.btn_clear_zip -> {
                btn_clear_zip.visibility = View.GONE
                etSignUpZip.setText("")
            }
            R.id.btn_clear_referral -> {
                btn_clear_referral.visibility = View.GONE
                etSignUpReferralCode.setText("")
            }
            R.id.btn_hide_show -> {
                if (isVisiblePassword) {
                    isVisiblePassword = false
                    btn_hide_show.setBackgroundResource(R.drawable.eye_on)
                    etSignUpPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etSignUpPassword.setSelection(etSignUpPassword.text!!.length)
                } else {
                    isVisiblePassword = true
                    btn_hide_show.setBackgroundResource(R.drawable.eye_off)
                    etSignUpPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    etSignUpPassword.setSelection(etSignUpPassword.text!!.length)
                }
            }
        }
    }

    private fun openDatePicker() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateBirthday()
            }

        val datePickerDialog = DatePickerDialog(
            homeActivity,
            dateSetListener,

            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = cal.timeInMillis
        datePickerDialog.show()
    }

    private fun updateBirthday() {
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tvBirthday!!.text = sdf.format(cal.time)
        dobDay = cal.get(Calendar.DAY_OF_MONTH)
        dobMonth = cal.get(Calendar.MONTH)
        dobMonth += 1
        dobYear = cal.get(Calendar.YEAR)

        birthday = cal.timeInMillis / 1000L
        tvBirthday.setTextColor(ContextCompat.getColor(homeActivity, R.color.edit_text_color))
        btnBirthdayAction.setBackgroundResource(R.drawable.clear_icon)
        btnBirthdayAction.setOnClickListener(this)
        checkRequiredFields()
    }

    private fun setSpannableText() {
        spanable_2.underline()
        spanable_4.underline()
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun onFavLocationPicked(name: String, id: Int) {
        tvFavLocation.text = name
        Log.d("TAG", "onFavLocationPicked: ${name}")
        favLocationId = id
        tvFavLocation.setTextColor(ContextCompat.getColor(homeActivity, R.color.dark_blue))
        checkRequiredFields()
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (etSignUpEmail.text.toString().isNotEmpty()) {
                    btn_clear_email.visibility = View.VISIBLE
                } else {
                    btn_clear_email.visibility = View.GONE
                }
                if (etSignUpFirstName.text.toString().isNotEmpty()) {
                    btn_clear_firstname.visibility = View.VISIBLE
                } else {
                    btn_clear_firstname.visibility = View.GONE
                }
                if (etSignUpLastName.text.toString().isNotEmpty()) {
                    btn_clear_lastname.visibility = View.VISIBLE
                } else {
                    btn_clear_lastname.visibility = View.GONE
                }
                if (etSignUpPhoneNumber.text.toString().isNotEmpty()) {
                    btn_clear_phone.visibility = View.VISIBLE
                } else {
                    btn_clear_phone.visibility = View.GONE
                }
                if (etSignUpZip.text.toString().isNotEmpty()) {
                    btn_clear_zip.visibility = View.VISIBLE
                } else {
                    btn_clear_zip.visibility = View.GONE
                }
                if (etSignUpReferralCode.text.toString().isNotEmpty()) {
                    btn_clear_referral.visibility = View.VISIBLE
                } else {
                    btn_clear_referral.visibility = View.GONE
                }
                checkRequiredFields()
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }
        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    fun checkRequiredFields() {
        presenter.checkRequirements(
            etSignUpEmail.text.toString(),
            etSignUpPassword.text.toString(),
            etSignUpFirstName.text.toString(),
            etSignUpLastName.text.toString(),
            etSignUpPhoneNumber.text.toString(),
            etSignUpZip.text.toString(),
            favLocationId,
            termsAgreed,
            birthday
        )
    }

    override fun onSuccessSignUp(response: SignUpResponse?) {
        // homeActivity.presenter.dismissProgress()

        Engine().saveData(
            mySharedPreferences,
            response!!.auth_token,
            "", "", "",
            etSignUpEmail.text.toString(),
            response.hashed_user_id,
            response.hashed_email
        )

        presenter.getUserProfile(
            Engine().getAuthToken(mySharedPreferences).toString(),
            homeActivity
        )
        if (response.hashed_email.isNotEmpty())
            Appboy.getInstance(context).changeUser(response.hashed_email)
        if (etSignUpEmail.text.toString().isNotEmpty())
            Appboy.getInstance(context).currentUser?.addAlias(
                etSignUpEmail.text.toString(),
                "EMAIL"
            )
        addAttributesToBraze()

        if (response.message.isNotEmpty())
            Engine().showMsgDialog(getString(R.string.app_name), response.message, homeActivity)
    }

    private fun addAttributesToBraze() {
        val phoneNumber = etSignUpPhoneNumber.text.toString()
        val firstName = etSignUpFirstName.text.toString()
        val lastName = etSignUpLastName.text.toString()
        val email = etSignUpEmail.text.toString()
        val signUpFavoriteLocation = tvFavLocation.text.toString()
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val signUpDate = sdf.format(Date())
        Appboy.getInstance(context).currentUser?.setEmail(email)
        Appboy.getInstance(context).currentUser?.setFirstName(firstName)
        Appboy.getInstance(context).currentUser?.setLastName(lastName)
        Appboy.getInstance(context).currentUser?.setPhoneNumber(phoneNumber)
        val birthdayLocal = birthday
        val defaultBirthday =
            -2208988793 //for some reason we receive default date if user have not enter date of birth
        if (birthdayLocal != null && birthdayLocal != defaultBirthday) {
            val date = Utils.getDateCurrentTimeZone(birthdayLocal)
            if (date != null) {
                val year = Utils.getFromDate(date, 1)
                val month = Utils.getFromDate(date, 2)
                val day = Utils.getFromDate(date, 3)
                val monthO = Month.getMonth(month)
                Appboy.getInstance(context).currentUser?.setDateOfBirth(year, monthO, day)
            }
        }
        when {
            optInChecked -> {
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
    }

    override fun disableButton() {
        Engine().setEnableButton(btnSignUp, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btnSignUp, true)
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

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        try {
            selectedQuestion = questionsList[position]
            val selectedText = parent?.getChildAt(0) as TextView
            if (position == 0) {
                TextViewCompat.setTextAppearance(selectedText, R.style.TextViewSignUp)
            } else {
                TextViewCompat.setTextAppearance(selectedText, R.style.EditText)
            }
            checkRequiredFields()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun successGetUserProfile(response: UserProfileResponse?) {

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
            presenter.getOauthToken(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString()
            )
        }

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

    override fun onSuccessOloSettings() {
        presenter.getOrCreate(
            homeActivity,
            Engine().getAccessToken(mySharedPreferences)!!,
            Engine().getUserMobilePhone(mySharedPreferences)!!
        )
    }

    override fun onSuccessGetOrcreate(response: OLOGetOrCreateResponse) {
        homeActivity.presenter.dismissProgress()
        Engine().putStringData(
            mySharedPreferences,
            AppConstants.PREFAUTH_OLO_TOKEN,
            response.authtoken
        )
        (homeActivity as OpenFragmentListener).openFragment()
    }

}

package com.tts.gueststar.fragments.contact

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import app.com.relevantsdk.sdk.models.GetSurveyResponse
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.AppSupportLocationsAdapter
import com.tts.gueststar.adapters.StoreFeedbackLocationsAdapter
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.AppSupportListener
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.interfaces.StoreFeedbackInterface
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.support.SupportLocationsPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_support_locations.*
import javax.inject.Inject

class SupportLocationsFragment : BaseFragment(), SupportLocationsPresenter.SupportLocationsView,
    View.OnClickListener {

    private var isFromAppSupport: Boolean = false
    private lateinit var presenter: SupportLocationsPresenter
    private lateinit var homeActivity: MainActivity
    private lateinit var appSupportAdapter: AppSupportLocationsAdapter
    private lateinit var storeFeedbackLocationsAdapter: StoreFeedbackLocationsAdapter
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private lateinit var getSurveyResponse: GetSurveyResponse
    private var restaurantId: Int = 0
    private var offerId: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromAppSupport = arguments!!.getBoolean(AppConstants.EXTRA_IS_APP_SUPPORT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_support_locations, container, false)
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)

        if (Engine.supportFromLogin) {
            if (Engine().checkIfLogin(mySharedPreferences)) {
                homeActivity.presenter.showProgress()
                presenter.getSurveyId(
                    homeActivity,
                    BuildConfig.APPKEY,
                    Engine().getAuthToken(mySharedPreferences).toString()
                )
            } else {
                Engine.supportFromLogin = true
                Engine.setNextPage = AppConstants.TAG_SURVEY
                homeActivity.presenter.openFragmentRight(
                    getFragment(
                        MainSignUpFragment(),
                        isFromBottomNavigation = false, isFromContactUs = true
                    ), AppConstants.TAG_MAIN_SIGN_UP2
                )
            }
        } else {
            loadLocations()
            if (isFromAppSupport) {
                fragmentTitle.text = getString(R.string.title_app_support)
            } else {
                fragmentTitle.text = getString(R.string.title_store_feedback)
            }
        }
    }

    private fun loadLocations() {
        if (Engine().isNetworkAvailable(homeActivity)) {
           homeActivity.showProgress()
            if (etLocationSearch.text.toString().isNotEmpty()) {
                presenter.getLocations(
                    homeActivity,
                    BuildConfig.APPKEY,
                    homeActivity.getGPSController()!!.latitude,
                    homeActivity.getGPSController()!!.longitude,
                    etLocationSearch.text.toString()
                )
            } else {
                presenter.getLocationsNoSearch(
                    homeActivity,
                    BuildConfig.APPKEY,
                    homeActivity.getGPSController()!!.latitude,
                    homeActivity.getGPSController()!!.longitude
                )
            }
        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = SupportLocationsPresenter(homeActivity, this, isFromAppSupport)
        setListeners()
    }

    private fun setListeners() {

        etLocationSearch.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event!!.action == MotionEvent.ACTION_UP) {
                when {
                    event.x <= (etLocationSearch.totalPaddingLeft) -> {
                        loadLocations()
                        true
                    }
                    event.rawX >= etLocationSearch.right - etLocationSearch.compoundDrawables[drawableRight].bounds.width() -> {
                        etLocationSearch.setText("")
                        true
                    }
                    else -> false
                }
            } else false
        }

        etLocationSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Engine().hideKeyboard(homeActivity, etLocationSearch)
                loadLocations()
                true
            } else {
                false
            }
        }

        setTextWatcherForAmountEditView(etLocationSearch)
        btnContinue.setOnClickListener(this)
        btn_back.setOnClickListener(this)
    }

    override fun loadAppSupportLocations(response: LocationsResponse?) {
        homeActivity.presenter.dismissProgress()
        try {
            if (response!!.locations.size > 0) {
                tvNoLocationsFound.visibility = View.GONE
                rvLocationsSupport.visibility = View.VISIBLE

                appSupportAdapter = AppSupportLocationsAdapter(
                    homeActivity,
                    response.locations,
                    object : AppSupportListener {
                        override fun sendEmail(locationName: String) {
                            sendEmailToSupport(locationName)
                        }
                    })
                rvLocationsSupport.layoutManager =
                    LinearLayoutManager(homeActivity.applicationContext)
                rvLocationsSupport.adapter = appSupportAdapter
            } else {
                tvNoLocationsFound.visibility = View.VISIBLE
                rvLocationsSupport.visibility = View.GONE
            }
        } catch (er: IllegalStateException) {
        }
    }

    override fun loadSendFeedbackLocations(response: LocationsResponse?) {
        homeActivity.presenter.dismissProgress()
        try {
            val list = mutableListOf<LocationsResponse.Location>()
            for (location in response!!.locations) {
                val offer: LocationsResponse.Location.AvailableOffer = location.available_offer!!
                if (offer.survey_id != 0) {
                    list.add(location)
                }
            }

            if (list.size > 0) {
                tvNoLocationsFound.visibility = View.GONE
                rvLocationsSupport.visibility = View.VISIBLE

                storeFeedbackLocationsAdapter =
                    StoreFeedbackLocationsAdapter(
                        homeActivity,
                        list,
                        object : StoreFeedbackInterface {
                            override fun getSurvey(surveyId: Int, locationId: Int, offerId: Int) {
                                if (Engine().isNetworkAvailable(homeActivity)) {

                                    if (Engine().checkIfLogin(mySharedPreferences)) {
                                        homeActivity.presenter.showProgress()
                                        getSurvey(surveyId)
                                        restaurantId = locationId
                                        this@SupportLocationsFragment.offerId = offerId
                                    } else {
                                        Engine.supportFromLogin = true
                                        Engine.setNextPage = AppConstants.TAG_SURVEY
                                        homeActivity.presenter.openFragmentRight(
                                            getFragment(
                                                MainSignUpFragment(),
                                                false, true
                                            ), AppConstants.TAG_MAIN_SIGN_UP2
                                        )
                                    }
                                } else Engine().showMsgDialog(
                                    "",
                                    getString(R.string.error_network_connection),
                                    homeActivity
                                )
                            }
                        })
                rvLocationsSupport.layoutManager =
                    LinearLayoutManager(homeActivity.applicationContext)
                rvLocationsSupport.adapter = storeFeedbackLocationsAdapter
            } else {
                tvNoLocationsFound.visibility = View.VISIBLE
                rvLocationsSupport.visibility = View.GONE
            }
        } catch (er: IllegalStateException) {
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

    override fun unauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_SUPPORT_LOCATIONS
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragment(
        fragment: BaseFragment,
        isFromBottomNavigation: Boolean,
        isFromContactUs: Boolean
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, isFromBottomNavigation)
        bundle.putBoolean(AppConstants.FROM_CONTACT_US, isFromContactUs)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentForSurvey(
        fragment: BaseFragment,
        survey: GetSurveyResponse,
        locationId: Int,
        offerId: Int
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putParcelable(AppConstants.EXTRA_SURVEY, survey)
        bundle.putInt(AppConstants.EXTRA_LOCATION_ID_SURVEY, locationId)
        bundle.putInt(AppConstants.EXTRA_OFFER_ID, offerId)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessGetSurvey(response: GetSurveyResponse) {
        homeActivity.dismissProgress()
        getSurveyResponse = response
        openSurveyFragment(getSurveyResponse)

    }

    private fun sendEmailToSupport(locationName: String) {
        val uriText =
            "mailto:" + getString(R.string.app_support_email) + "?subject=" + getString(R.string.app_support_subject) + "&body=" + Engine().appSupportEmailBody(
                mySharedPreferences, locationName
            )
        val uri = Uri.parse(uriText)
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = uri
        startActivity(Intent.createChooser(emailIntent, "Email"))
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length >= 5)
                    loadLocations()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    private fun openSurveyFragment(response: GetSurveyResponse) {
        Engine.supportFromLogin = false
        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.openFragmentRight(
            getFragmentForSurvey(
                SurveyFragment(),
                response,
                restaurantId,
                offerId
            ), AppConstants.TAG_SURVEY
        )
    }

    override fun getSurvey(surveyId: Int) {
        homeActivity.presenter.dismissProgress()
        presenter.getSurvey(
            homeActivity,
            BuildConfig.APPKEY,
            Engine().getAuthToken(mySharedPreferences).toString(),
            surveyId
        )
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnContinue -> {
                if (isFromAppSupport) {
                    sendEmailToSupport("")
                } else {
                    if (Engine().checkIfLogin(mySharedPreferences)) {
                        homeActivity.presenter.showProgress()
                        presenter.getSurveyId(
                            homeActivity,
                            BuildConfig.APPKEY,
                            Engine().getAuthToken(mySharedPreferences).toString()
                        )
                    } else {
                        Engine.supportFromLogin = true
                        Engine.setNextPage = AppConstants.TAG_SURVEY
                        homeActivity.presenter.openFragmentUp(
                            getFragment(
                                MainSignUpFragment()
                            ), AppConstants.TAG_MAIN_SIGN_UP2
                        )
                    }
                }
            }

            R.id.btn_back -> {
                homeActivity.onBackPressed()
            }
        }
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

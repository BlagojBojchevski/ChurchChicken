package com.tts.gueststar.fragments.onlineorder


import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.google.gson.Gson
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.adapters.FavoriteLocationsAdapter
import com.tts.gueststar.interfaces.OnFavLocationPicked
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.favlocation.FavoriteLocationPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.MySharedPreferences
import kotlinx.android.synthetic.main.fragment_favorite_location.*
import android.content.SharedPreferences
import app.com.relevantsdk.sdk.models.UpdateUserProfileResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.gueststar.MyApplication
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.OloBasketTrasnfer
import javax.inject.Inject


class ChangeOrderLocationFragment : BaseFragment(), FavoriteLocationPresenter.FavoriteLocationView,
    View.OnClickListener {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        homeActivity.presenter.dismissProgress()
        homeActivity.clearStackOnlineOrder()
    }

    override fun onSuccessUpdateProfile(response: UpdateUserProfileResponse?) {
        val json = Gson().toJson(response)
        MySharedPreferences.putString(
            homeActivity,
            MySharedPreferences.userProfile,
            json
        )
        homeActivity.onBackPressed()
    }


    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences

    private var isFromHome: Boolean = false
    private lateinit var homeActivity: MainActivity
    private lateinit var adapter: FavoriteLocationsAdapter
    private lateinit var presenter: FavoriteLocationPresenter

    override fun onSuccessGetLocations(response: LocationsResponse?) {
        homeActivity.presenter.dismissProgress()
        try {
            layoutLocationsOff.visibility = View.GONE
            layoutLocationsOn.visibility = View.VISIBLE
        } catch (er: IllegalStateException) {
        }

        if (response!!.locations.size == 0) {
            rvLocations.visibility = View.GONE
            tvNoLocationsFound.visibility = View.VISIBLE

        } else {
            try {
                rvLocations.layoutManager =
                    LinearLayoutManager(homeActivity.applicationContext)
                response.locations.removeAll { !it.online_order_support_status }
                adapter = FavoriteLocationsAdapter(
                    homeActivity,
                    response.locations,
                    object : OnFavLocationPicked {
                        override fun onFavLocationPicked(
                            location: LocationsResponse.Location,
                            locationName: String,
                            locationId: Int
                        ) {

                            if (location.external_partner_id.isNotEmpty()) {
                                OrderHelper.location = location
                                OrderHelper.locationAddress = location.address
                                homeActivity.presenter.showProgress()
                                presenter.transferBasket(
                                    homeActivity,
                                    location.external_partner_id.toInt(),
                                    OrderHelper.basketId!!
                                )
                            } else {
                                Engine().showMsgDialog(
                                    getString(R.string.app_name),
                                    getString(R.string.handle_blank_pop_up),
                                    homeActivity
                                )
                            }
                        }
                    })

                rvLocations.adapter = adapter

                rvLocations.visibility = View.VISIBLE
                tvNoLocationsFound.visibility = View.GONE
                //rvLocations.addItemDecoration(DividerItemDecoration(homeActivity, DividerItemDecoration.VERTICAL))
            } catch (er: IllegalStateException) {
            }

        }
    }

    override fun onSuccessGetUserInfo(response: UserProfileResponse) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromHome = requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = FavoriteLocationPresenter(this)

        tvNoLocations2.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        setListeners()

        etLocationSearch.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event!!.action == MotionEvent.ACTION_UP) {
                when {
                    event.x <= (etLocationSearch.totalPaddingLeft) -> {
                        getLocations()
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

    }

    private fun setListeners() {

        tvNoLocations2.setOnClickListener {
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }

        etLocationSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Engine().hideKeyboard(homeActivity, etLocationSearch)
                getLocations()
                true
            } else {
                false
            }
        }

        btn_back.setOnClickListener(this)
        setTextWatcherForAmountEditView(etLocationSearch)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length >= 5)
                    presenter.getLocations(
                        homeActivity,
                        BuildConfig.APPKEY,
                        homeActivity.getGPSController()!!.latitude,
                        homeActivity.getGPSController()!!.longitude,
                        etLocationSearch.text.toString()
                    )
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun onResume() {
        super.onResume()

        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        btn_back.setImageResource(R.drawable.arrow_white_left)

        if (Engine().isNetworkAvailable(homeActivity)) {
            if (Engine().checkEnableGPS(homeActivity)) {
                layoutLocationsOff.visibility = View.GONE
                layoutLocationsOn.visibility = View.VISIBLE
                getLocations()
            } else {
                layoutLocationsOff.visibility = View.VISIBLE
                layoutLocationsOn.visibility = View.GONE
            }
        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
        }
    }

    private fun getLocations() {
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            if (etLocationSearch.text.toString().isNotEmpty())
                presenter.getLocations(
                    homeActivity, BuildConfig.APPKEY, homeActivity.getGPSController()!!.latitude,
                    homeActivity.getGPSController()!!.longitude, etLocationSearch.text.toString()
                )
            else
                presenter.getLocationsNoSearch(
                    homeActivity, BuildConfig.APPKEY, homeActivity.getGPSController()!!.latitude,
                    homeActivity.getGPSController()!!.longitude
                )


        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
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

    override fun onClick(v: View?) {
        when (v!!.id) {
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

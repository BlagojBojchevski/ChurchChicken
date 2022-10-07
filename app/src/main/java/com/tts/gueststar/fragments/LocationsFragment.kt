package com.tts.gueststar.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.LocationsAdapter
import com.tts.gueststar.adapters.LocationsViewPagerAdapter
import com.tts.gueststar.databinding.FragmentLocationsBinding
import com.tts.gueststar.fragments.onlineorder.OrderMenuFragment
import com.tts.gueststar.fragments.onlineorder.SelectOrderModeFragment
import com.tts.gueststar.interfaces.LocationActionsInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.favlocation.LocationsPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.OloGetRestaurantDetailsResponse
import kotlinx.android.synthetic.main.fragment_locations.*
import javax.inject.Inject


class LocationsFragment : BaseFragment(), LocationsPresenter.LocationsView, View.OnClickListener,
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var homeActivity: MainActivity
    private lateinit var adapter: LocationsAdapter
    private lateinit var presenter: LocationsPresenter
    private lateinit var locationsList: ArrayList<LocationsResponse.Location>
    private var selectedLocation: LocationsResponse.Location? = null
    private var map: GoogleMap? = null
    private var alertDialogBuilder: AlertDialog.Builder? = null
    private var areMapsSelected = false
    private lateinit var viewPagerAdapter: LocationsViewPagerAdapter
    private lateinit var app: MyApplication

    override fun onSuccessGetLocations(response: LocationsResponse?) {
        homeActivity.presenter.dismissProgress()
        try {
            binding.layoutLocationsOff.visibility = View.GONE
            binding.layoutLocationsOn.visibility = View.VISIBLE
            if (response != null){
                locationsList = response!!.locations


            if (response!!.locations.size == 0) {
                setNoLocationsFound()
            } else {
                binding.tvNoLocationsFound.visibility = View.GONE
                val locationPermission =
                    ContextCompat.checkSelfPermission(
                        homeActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                if (locationPermission == PackageManager.PERMISSION_GRANTED) {
                    enableMapButton()
                } else disableMapButton()


                if (areMapsSelected) {
                    binding.mapLayout.visibility = View.VISIBLE
                    binding.rvLocations.visibility = View.INVISIBLE
                    binding.btnMap.contentDescription = getString(R.string.list_button)
                    binding.btnMap.setBackgroundResource(R.mipmap.icon_location_right)
                    setViewPager()
                    areMapsSelected = true
                } else {
                    binding.mapLayout.visibility = View.INVISIBLE
                    binding.rvLocations.visibility = View.VISIBLE
                    binding.btnMap.contentDescription = getString(R.string.map_button)
                    binding.btnMap.setBackgroundResource(R.mipmap.map)
                    areMapsSelected = false
                }
                binding.rvLocations.layoutManager = LinearLayoutManager(homeActivity.applicationContext)
                adapter = LocationsAdapter(
                    homeActivity,
                    response.locations,
                    object : LocationActionsInterface {
                        override fun callRestaurant(phoneNumber: String) {
                            try {
                                var urlPhoneNumber = phoneNumber
                                urlPhoneNumber = "tel:$urlPhoneNumber"

                                val intentCall = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse(urlPhoneNumber)
                                )
                                startActivity(intentCall)
                            } catch (activityException: Exception) {
                            }
                        }

                        override fun directions(latitude: Double, longitude: Double) {
                            try {
                                val offerLatLong =
                                    (homeActivity.getGPSController()!!.latitude.toString() + "," + homeActivity.getGPSController()!!.longitude.toString() + "&daddr=" + latitude + "," + longitude)
                                val searchAddress = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr=$offerLatLong")
                                )
                                startActivity(searchAddress)
                            } catch (activityException: Exception) {
                            }
                        }

                        override fun openWebView(url: String, name: String) {
                            if (Engine().isNetworkAvailable(homeActivity))
                                homeActivity.presenter.openFragmentRight(
                                    getFragmentWebView(
                                        WebViewFragment(),
                                        url,
                                        name
                                    ), AppConstants.TAG_WEB_VIEW
                                )
                            else Engine().showMsgDialog(
                                "",
                                getString(R.string.error_network_connection),
                                homeActivity
                            )
                        }

                        override fun orderOnline(restaurant: LocationsResponse.Location) {
//                            OrderHelper.fromHome = false
//                            OrderHelper.fromHistory = false
//                            OrderHelper.open_time = restaurant.today_open_hour!!.open_at
//                            OrderHelper.location = null
//                            selectedLocation = restaurant
//                            OrderHelper.location = restaurant
//                            OrderHelper.locationAddress = restaurant.address
//                            if (restaurant.external_partner_id.isEmpty()) {
//                                Engine().showMsgDialogNoviewPagerLocations(
//                                    getString(R.string.cannot_acceps_orders_error),
//                                    homeActivity
//                                )
//                            } else {
//                                if (Engine().isNetworkAvailable(homeActivity)) {
//                                    homeActivity.presenter.showProgress()
//                                    presenter.getoloSettings(homeActivity)
//                                } else {
//                                    Engine().showMsgDialog(
//                                        getString(R.string.app_name),
//                                        getString(R.string.error_network_connection),
//                                        homeActivity
//                                    )
//                                }
//                            }
                            OrderHelper.fromHome = false
                            OrderHelper.fromHistory = false
                            OrderHelper.location = null
                            homeActivity.presenter.openFragmentRight(
                                getFragmentOrder(
                                    SelectOrderModeFragment(),
                                    restaurant
                                ), AppConstants.TAG_ONLINE_ORDER
                            )
                        }
                    })

                binding.rvLocations.adapter = adapter
                val myMapFragment = childFragmentManager.findFragmentById(com.tts.gueststar.R.id.googleMap) as SupportMapFragment?
                myMapFragment?.getMapAsync(this)

                homeActivity.presenter.dismissProgress()
                setViewPager()
            }
            }
        } catch (er: IllegalStateException) {
        }
    }

    private fun getFragmentOrder(
        fragment: BaseFragment,
        location: LocationsResponse.Location
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, false)
        bundle.putParcelable(AppConstants.SELECTED_LOCATION, location)
        fragment.arguments = bundle
        return fragment
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(2)

        presenter = LocationsPresenter(this)

        binding.tvNoLocations2.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        setListeners()

        binding.etLocationSearch.setOnTouchListener { _, event ->
            if (event!!.action == MotionEvent.ACTION_UP) {
                when {
                    event.x <= (etLocationSearch.totalPaddingLeft) -> {
                        getLocations()
                        true
                    }
                    else -> false
                }
            } else false
        }

    }

    private fun setListeners() {
        binding.btnMap.setOnClickListener(this)
        binding.btnClearSearch.setOnClickListener(this)

        tvNoLocations2.setOnClickListener {
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }

        binding.etLocationSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Engine().hideKeyboard(homeActivity, etLocationSearch)
                getLocations()
                true
            } else {
                false
            }
        }

        binding.btnBack.setOnClickListener(this)
        setTextWatcherForAmountEditView(etLocationSearch)

    }

    override fun onResume() {
        super.onResume()
        if (Engine().checkEnableGPS(homeActivity)) {
            layoutLocationsOff?.visibility = View.GONE
            layoutLocationsOn?.visibility = View.VISIBLE
            getLocations()
            //enableMapButton()
        } else {
            if (::locationsList.isInitialized && locationsList.size > 0) {
                if (!areMapsSelected) {
                    binding.mapLayout.visibility = View.VISIBLE
                    binding.rvLocations.visibility = View.INVISIBLE
                    binding.btnMap.contentDescription = getString(R.string.list_button)
                    binding.btnMap.setBackgroundResource(R.mipmap.icon_location_right)
                    setViewPager()
                    areMapsSelected = true
                } else {
                    binding.mapLayout.visibility = View.INVISIBLE
                    binding.rvLocations.visibility = View.VISIBLE
                    binding.btnMap.contentDescription = getString(R.string.map_button)
                    binding.btnMap.setBackgroundResource(R.mipmap.map)
                    areMapsSelected = false
                }
            }
            binding.tvNoLocationsFound.visibility = View.GONE
            binding.layoutLocationsOff.visibility = View.VISIBLE
            binding.layoutLocationsOn.visibility = View.GONE
            disableMapButton()
        }

    }

    private fun setViewPager() {
        viewPagerAdapter = LocationsViewPagerAdapter(
            homeActivity,
            locationsList,
            object : LocationActionsInterface {
                override fun callRestaurant(phoneNumber: String) {
                    try {
                        var urlPhoneNumber = phoneNumber
                        urlPhoneNumber = "tel:$urlPhoneNumber"

                        val intentCall = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(urlPhoneNumber)
                        )
                        startActivity(intentCall)
                    } catch (activityException: Exception) {
                    }
                }

                override fun directions(latitude: Double, longitude: Double) {
                    try {
                        val offerLatLong =
                            (homeActivity.getGPSController()!!.latitude.toString() + "," + homeActivity.getGPSController()!!.longitude.toString() + "&daddr=" + latitude + "," + longitude)
                        val searchAddress = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=$offerLatLong")
                        )
                        startActivity(searchAddress)
                    } catch (activityException: Exception) {
                    }
                }

                override fun openWebView(url: String, name: String) {
                    if (Engine().isNetworkAvailable(homeActivity))
                        homeActivity.presenter.openFragmentRight(
                            getFragmentWebView(
                                WebViewFragment(),
                                url,
                                name
                            ), AppConstants.TAG_WEB_VIEW
                        )
                    else Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }

                override fun orderOnline(restaurant: LocationsResponse.Location) {
                    OrderHelper.fromHome = false
                    OrderHelper.fromHistory = false
                    OrderHelper.open_time = restaurant.today_open_hour!!.open_at
                    OrderHelper.location = null
                    selectedLocation = restaurant
                    OrderHelper.location = restaurant
                    OrderHelper.locationAddress = restaurant.address

                    if (restaurant.external_partner_id.isEmpty()) {
                        Engine().showMsgDialogNoTitle(
                            getString(R.string.cannot_acceps_orders_error),
                            homeActivity
                        )
                    } else {
                        if (Engine().isNetworkAvailable(homeActivity)) {
                            homeActivity.presenter.showProgress()
                            presenter.getoloSettings(homeActivity)
                        } else {
                            Engine().showMsgDialog(
                                getString(R.string.app_name),
                                getString(R.string.error_network_connection),
                                homeActivity
                            )
                        }
                    }
                }
            })

        binding.viewPagerLocations.adapter = viewPagerAdapter
        binding.viewPagerLocations.clipToPadding = false
        binding.viewPagerLocations.pageMargin = 30
        binding.viewPagerLocations.setPadding(70, 5, 70, 5)



        binding.viewPagerLocations.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                moveCameraToMarker(
                    locationsList[position].latitude,
                    locationsList[position].longitude
                )
            }

        })
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnMap -> {
                if (::locationsList.isInitialized && locationsList.size > 0) {
                    if (!areMapsSelected) {
                        binding.mapLayout.visibility = View.VISIBLE
                        binding.rvLocations.visibility = View.INVISIBLE
                        binding.btnMap.contentDescription = getString(R.string.list_button)
                        binding.btnMap.setBackgroundResource(R.mipmap.icon_location_right)
                        setViewPager()
                        areMapsSelected = true
                    } else {
                        binding.mapLayout.visibility = View.INVISIBLE
                        binding.rvLocations.visibility = View.VISIBLE
                        binding.btnMap.contentDescription = getString(R.string.map_button)
                        binding.btnMap.setBackgroundResource(R.mipmap.map)
                        areMapsSelected = false
                    }
                }
            }
            R.id.btn_back -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

            R.id.btn_clear_search -> {
                binding.etLocationSearch.setText("")
            }
        }
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length >= 5) {
                    if (Engine().isNetworkAvailable(homeActivity)) {
                        try {
                            presenter.getLocations(
                                homeActivity,
                                BuildConfig.APPKEY,
                                homeActivity.getGPSController()!!.latitude,
                                homeActivity.getGPSController()!!.longitude,
                                binding.etLocationSearch.text.toString()
                            )
                        } catch (er: KotlinNullPointerException) {
                        }
                    } else {
                        Engine().showMsgDialog(
                            "",
                            getString(R.string.error_network_connection),
                            homeActivity
                        )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }


    private fun getLocations() {
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()

            if (etLocationSearch.text.toString().isNotEmpty())
                presenter.getLocations(
                    homeActivity,
                    BuildConfig.APPKEY,
                    homeActivity.getGPSController()!!.latitude,
                    homeActivity.getGPSController()!!.longitude,
                    binding.etLocationSearch.text.toString()
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        map!!.clear()
        val location = LatLng(locationsList[0].latitude, locationsList[0].longitude)
        map!!.moveCamera(CameraUpdateFactory.newLatLng(location))
        map!!.animateCamera(CameraUpdateFactory.zoomTo(10f))
        for (i in 0 until locationsList.size) {
            val list = locationsList[i]
            // Drawing marker on the map
            drawMarker(LatLng(list.latitude, list.longitude), i)
        }

        map!!.setOnMarkerClickListener(this)

    }

    fun moveCameraToMarker(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)
        map?.moveCamera(CameraUpdateFactory.newLatLng(location))
        map?.animateCamera(CameraUpdateFactory.zoomTo(10f))
    }

    private fun drawMarker(point: LatLng, positionInList: Int) {
        val marker = map?.addMarker(
            MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_pin))
        )

        marker?.tag = positionInList

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        binding.viewPagerLocations.currentItem = marker.tag as Int
        return true
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

    override fun setNoLocationsFound() {
        binding.tvNoLocationsFound.visibility = View.VISIBLE
        binding.mapLayout.visibility = View.GONE
        binding.rvLocations.visibility = View.GONE
        disableMapButton()
    }

    override fun getSitebyIdafterCloudConnect() {
        presenter.getSiteById(homeActivity, selectedLocation!!.external_partner_id)
    }

    override fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse) {
        OrderHelper.restaurantDetails = response
        OrderHelper.advanceonly = response.advanceonly
        OrderHelper.isavailable = response.isavailable
        OrderHelper.advanceorderdays = response.advanceorderdays
        OrderHelper.showCalories = response.showcalories
        OrderHelper.supportsbaskettransfers = response.supportsbaskettransfers
        OrderHelper.supportsSpecialCaracters = response.supportsspecialinstructions
        OrderHelper.specialInstrutionsQuantity = response.specialinstructionsmaxlength
        OrderHelper.acceptsordersbeforeopening = response.acceptsordersbeforeopening
        OrderHelper.acceptsordersuntilclosing = response.acceptsordersuntilclosing

        try {
            if (response.supportsonlineordering) {
                if (response.isavailable) {
                    OrderHelper.deliveryMode = getString(R.string.mode_pickup)
                    if (OrderHelper.basket != null) {
                        if (OrderHelper.basket!!.vendorid == selectedLocation!!.external_partner_id.toInt()) {
                            presenter.addDeliveryModeToBasket(
                                homeActivity,
                                getString(R.string.mode_pickup),
                                OrderHelper.basketId!!
                            )
                        } else {
                            if (OrderHelper.supportsbaskettransfers) {
                                //transfer
                                try {
                                    if (alertDialogBuilder == null) {
                                        alertDialogBuilder = AlertDialog.Builder(context)
                                        alertDialogBuilder!!.setMessage(getString(R.string.transfer_text))
                                            .setCancelable(false).setPositiveButton(
                                                "YES"
                                            ) { dialog, _ ->
                                                presenter.transferBasket(
                                                    homeActivity,
                                                    selectedLocation!!.external_partner_id.toInt(),
                                                    OrderHelper.basketId!!
                                                )
                                                alertDialogBuilder = null
                                                dialog.cancel()
                                            }
                                            .setNegativeButton(
                                                "NO"
                                            ) { dialog, _ ->
                                                alertDialogBuilder = null
                                                presenter.createBasket(
                                                    homeActivity,
                                                    selectedLocation?.external_partner_id!!.toInt(),
                                                    Engine().getOloAuthToken(mySharedPreferences)
                                                        .toString()
                                                )
                                                dialog.cancel()
                                            }
                                        val alert = alertDialogBuilder!!.create()
                                        alert.setTitle("")
                                        alert.show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                presenter.createBasket(
                                    homeActivity,
                                    selectedLocation?.external_partner_id!!.toInt(),
                                    Engine().getOloAuthToken(mySharedPreferences).toString()
                                )
                            }
                        }
                    } else {
                        presenter.createBasket(
                            homeActivity,
                            selectedLocation?.external_partner_id!!.toInt(),
                            Engine().getOloAuthToken(mySharedPreferences).toString()
                        )
                    }

                } else {
                    homeActivity.presenter.dismissProgress()
                    Engine().showMsgDialogNoTitle(
                        getString(R.string.cannot_acceps_orders_error),
                        homeActivity
                    )
                }

            } else {
                homeActivity.presenter.dismissProgress()
                Engine().showMsgDialogNoTitle(
                    getString(R.string.cannot_acceps_orders_error),
                    homeActivity
                )
            }
        } catch (ex: IllegalStateException) {
            homeActivity.presenter.dismissProgress()
        }
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id
        presenter.addDeliveryModeToBasket(
            homeActivity,
            getString(R.string.mode_pickup),
            response.id
        )
    }

    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        presenter.addDeliveryModeToBasket(
            homeActivity,
            getString(R.string.mode_pickup),
            response.basket.id
        )
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        homeActivity.presenter.openFragmentUp(
            getFragmentMenu(
                OrderMenuFragment(), selectedLocation!!.external_partner_id
            ),
            AppConstants.TAG_ONLINE_ORDER
        )

    }

    private fun getFragmentMenu(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, false)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentWebView(
        fragment: BaseFragment,
        url: String,
        title: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.WEB_VIEW_URL, url)
        bundle.putString(AppConstants.WEB_VIEW_TITLE, title)
        fragment.arguments = bundle
        return fragment
    }

    override fun enableMapButton() {
        binding.btnMap.isClickable = true
        binding.btnMap.alpha = 1.toFloat()
    }

    override fun disableMapButton() {
        binding.btnMap.isClickable = false
        binding.btnMap.alpha = 0.5.toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }
}

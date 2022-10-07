package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.LocationsResponse
import app.com.relevantsdk.sdk.models.RewardsActivityResponse
import app.com.relevantsdk.sdk.models.UserActivityResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.LoyaltyHistoryAdapter
import com.tts.gueststar.adapters.OrderHistoryAdapter
import com.tts.gueststar.adapters.OrderHistoryAdapterFaves
import com.tts.gueststar.fragments.onlineorder.OrderMenuFragment
import com.tts.gueststar.fragments.onlineorder.OrderSummaryFragment
import com.tts.gueststar.fragments.onlineorder.SelectOrderModeFragment
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.OrderHistoryItemsFavesInterface
import com.tts.gueststar.interfaces.OrderHistoryItemsInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.models.LoyaltyHistory
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.activityhistory.ActivityHistoryPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.*
import kotlinx.android.synthetic.main.fragment_activity_history.*
import javax.inject.Inject

@Suppress("DEPRECATION")
class ActivityHistoryFragment : BaseFragment(), ActivityHistoryPresenter.ActivityHistoryView,
    OnClickListener,
    View.OnTouchListener {

    private var alertDialogBuilder: AlertDialog.Builder? = null
    private var selectedLocation: LocationsResponse.Location? = null
    private var fromFavOrder = false
    private var fromAll: Boolean? = null
    private var numberFieldsSend = 0
    private var externalPartnerId = ""
    private var loyaltyHistoty: MutableList<LoyaltyHistory> = arrayListOf()
    private var loyaltyPayment: MutableList<LoyaltyHistory> = arrayListOf()
    private var addedLocation: MutableList<LoyaltyHistory> = arrayListOf()
    private lateinit var adapter: LoyaltyHistoryAdapter
    private lateinit var adapterOrderHistory: OrderHistoryAdapter
    private lateinit var adapterOrderHistoryFaves: OrderHistoryAdapterFaves
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: ActivityHistoryPresenter
    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var isFromRewards = false

    override fun onSuccessCreateBasketFromFaveOrder(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id
        externalPartnerId = response.vendorid.toString()
        presenter.getSiteById(homeActivity, externalPartnerId)
    }

    override fun onSuccessDeleteFavOrder() {
        rvActivityHistory.adapter = null
        if (Engine().isNetworkAvailable(homeActivity)) {
            presenter.getUserFavoriteBaskets(
                homeActivity,
                Engine().getOloAuthToken(mySharedPreferences)!!
            )
        } else {
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

//    override fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse) {
//        homeActivity.presenter.dismissProgress()
//        OrderHelper.restaurantDetails = response
//        OrderHelper.advanceonly = response.advanceonly
//        OrderHelper.isavailable = response.isavailable
//        OrderHelper.advanceorderdays = response.advanceorderdays
//        OrderHelper.showCalories = response.showcalories
//        OrderHelper.supportsbaskettransfers = response.supportsbaskettransfers
//        OrderHelper.supportsSpecialCaracters = response.supportsspecialinstructions
//        OrderHelper.specialInstrutionsQuantity = response.specialinstructionsmaxlength
//        OrderHelper.acceptsordersbeforeopening = response.acceptsordersbeforeopening
//        OrderHelper.acceptsordersuntilclosing = response.acceptsordersuntilclosing
//
//        val selectedLocationL = LocationsResponse.Location(
//            address = response.streetaddress,
//            city_label = response.city,
//            name = response.name,
//            phone_number = response.telephone,
//            id = 0,
//            zipcode = response.zip,
//            agm_support = false,
//            aloha_online_id = "",
//            app_display_text = "",
//            available_offer = null,
//            beacon_serial_number = "",
//            beacon_uuid = "",
//            chain_id = 0,
//            cloud_connect_site_id = "",
//            connected_payment = false,
//            contact = "",
//            country_label = "",
//            dashboard_display_text = "",
//            delivery_radius_miles = "",
//            external_id = "",
//            external_location_store_id = "",
//            external_partner_id = externalPartnerId,
//            fishbowl_restaurant_identifier = "",
//            is_open = false,
//            latitude = response.latitude,
//            ldap_identity = "",
//            location_qrcode_identifier = "",
//            longitude = response.longitude,
//            ncr_aloha_loyalty_store_id = "",
//            ncr_aoo_menu_level = "",
//            ncr_aoo_price_level = "",
//            ncr_provisioned_loyalty = false,
//            ncr_provisioned_rewards = false,
//            ncr_store_id = "",
//            online_order_link = "",
//            online_order_support_status = false,
//            online_partner = 0,
//            operating_hours = null,
//            order_priority = 0,
//            rest_tax = 0F,
//            restaurant_distance = response.distance.toDouble(),
//            social_link = "",
//            state_label = response.state,
//            status = false,
//            today_open_hour = null,
//            ts_pay_support = false,
//            web_portal_agm_support = false,
//            web_portal_online_order_support = false
//        )
//        selectedLocation = selectedLocationL
//        OrderHelper.location = selectedLocationL
//        OrderHelper.locationAddress = selectedLocationL.address
//        OrderHelper.fromHistory = true
//        if (!fromFavOrder) {
//            OrderHelper.flagChechForRewards = false
//            if (OrderHelper.deliveryMode == getString(R.string.mode_curbside)) {
//                if (Engine().isNetworkAvailable(homeActivity)) {
//                    homeActivity.presenter.showProgress()
//
//                    presenter.addCustomFieldsToBasket(
//                        homeActivity,
//                        120,
//                        OrderHelper.carInfo.split(";")[0],
//                        OrderHelper.basketId.toString()
//                    )
//                    presenter.addCustomFieldsToBasket(
//                        homeActivity,
//                        121,
//                        OrderHelper.carInfo.split(";")[1],
//                        OrderHelper.basketId.toString()
//                    )
//                    presenter.addCustomFieldsToBasket(
//                        homeActivity,
//                        122,
//                        OrderHelper.carInfo.split(";")[2],
//                        OrderHelper.basketId.toString()
//                    )
//                } else {
//                    Engine().showMsgDialog(
//                        getString(R.string.app_name),
//                        getString(R.string.error_network_connection),
//                        homeActivity
//                    )
//                }
//            } else {
//                homeActivity.presenter.openFragmentRight(
//                    getFragmentSummary(
//                        OrderSummaryFragment(), externalPartnerId
//                    ),
//                    AppConstants.TAG_ONLINE_ORDER
//                )
//            }
//        } else {
//            OrderHelper.fromHome = false
//            try {
//                if (response.supportsonlineordering) {
//                    if (response.isavailable) {
//                        OrderHelper.deliveryMode = getString(R.string.mode_pickup)
//                        if (OrderHelper.basket != null) {
//                            if (OrderHelper.basket!!.vendorid == selectedLocationL.external_partner_id.toInt()) {
//                                presenter.addDeliveryModeToBasket(
//                                    homeActivity,
//                                    getString(R.string.mode_pickup),
//                                    OrderHelper.basketId!!
//                                )
//                            } else {
//                                if (OrderHelper.supportsbaskettransfers) {
//                                    //transfer
//                                    try {
//                                        if (alertDialogBuilder == null) {
//                                            alertDialogBuilder = AlertDialog.Builder(context)
//                                            alertDialogBuilder!!.setMessage(getString(R.string.transfer_text))
//                                                .setCancelable(false).setPositiveButton(
//                                                    "YES"
//                                                ) { dialog, _ ->
//                                                    presenter.transferBasket(
//                                                        homeActivity,
//                                                        selectedLocationL.external_partner_id.toInt(),
//                                                        OrderHelper.basketId!!
//                                                    )
//                                                    alertDialogBuilder = null
//                                                    dialog.cancel()
//                                                }
//                                                .setNegativeButton(
//                                                    "NO"
//                                                ) { dialog, _ ->
//                                                    alertDialogBuilder = null
//                                                    presenter.createBasket(
//                                                        homeActivity,
//                                                        selectedLocationL.external_partner_id.toInt(),
//                                                        Engine().getOloAuthToken(mySharedPreferences).toString()
//                                                    )
//                                                    dialog.cancel()
//                                                }
//                                            val alert = alertDialogBuilder!!.create()
//                                            alert.setTitle("")
//                                            alert.show()
//                                        }
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                    }
//                                } else {
//                                    presenter.createBasket(
//                                        homeActivity,
//                                        selectedLocationL.external_partner_id.toInt(),
//                                        Engine().getOloAuthToken(mySharedPreferences).toString()
//                                    )
//                                }
//                            }
//                        } else {
//                            presenter.createBasket(
//                                homeActivity,
//                                selectedLocationL.external_partner_id.toInt(),
//                                Engine().getOloAuthToken(mySharedPreferences).toString()
//                            )
//                        }
//
//                    } else {
//                        Engine().showMsgDialogNoTitleClose(
//                            getString(R.string.cannot_acceps_orders_error),
//                            homeActivity
//                        )
//                    }
//
//                } else {
//                    Engine().showMsgDialogNoTitleClose(
//                        getString(R.string.cannot_acceps_orders_error),
//                        homeActivity
//                    )
//                }
//            } catch (ex: IllegalStateException) {
//                homeActivity.presenter.dismissProgress()
//            }
//
//        }
//    }

    override fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse) {
        homeActivity.presenter.dismissProgress()
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

        val selectedLocation = LocationsResponse.Location(
            address = response.streetaddress,
            city_label = response.city,
            name = response.name,
            phone_number = response.telephone,
            id = 0,
            zipcode = response.zip,
            agm_support = false,
            aloha_online_id = "",
            app_display_text = "",
            available_offer = null,
            beacon_serial_number = "",
            beacon_uuid = "",
            chain_id = 0,
            cloud_connect_site_id = "",
            connected_payment = false,
            contact = "",
            country_label = "",
            dashboard_display_text = "",
            delivery_radius_miles = "",
            external_id = "",
            external_location_store_id = "",
            external_partner_id = externalPartnerId,
            fishbowl_restaurant_identifier = "",
            is_open = false,
            latitude = response.latitude,
            ldap_identity = "",
            location_qrcode_identifier = "",
            longitude = response.longitude,
            ncr_aloha_loyalty_store_id = "",
            ncr_aoo_menu_level = "",
            ncr_aoo_price_level = "",
            ncr_provisioned_loyalty = false,
            ncr_provisioned_rewards = false,
            ncr_store_id = "",
            online_order_link = "",
            online_order_support_status = false,
            online_partner = 0,
            operating_hours = null,
            order_priority = 0,
            rest_tax = 0F,
            restaurant_distance = response.distance.toDouble(),
            social_link = "",
            state_label = response.state,
            status = false,
            today_open_hour = null,
            ts_pay_support = false,
            web_portal_agm_support = false,
            web_portal_online_order_support = false
        )
        OrderHelper.location = selectedLocation
        OrderHelper.locationAddress = selectedLocation.address
        OrderHelper.fromHistory = true
        if (!fromFavOrder) {
            if (OrderHelper.deliveryMode == getString(R.string.mode_curbside)) {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()


                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_make,
                        OrderHelper.carInfo.split(";")[0],
                        OrderHelper.basketId.toString()
                    )
                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_model,
                        OrderHelper.carInfo.split(";")[1],
                        OrderHelper.basketId.toString()
                    )
                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_color,
                        OrderHelper.carInfo.split(";")[2],
                        OrderHelper.basketId.toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            } else {
                homeActivity.presenter.openFragmentRight(
                    getFragmentSummary(
                        OrderSummaryFragment(), externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }
        } else {
            OrderHelper.fromHome = false
            homeActivity.presenter.openFragmentRight(
                getFragmentOrder(
                    SelectOrderModeFragment(),
                    OrderHelper.location!!
                ), AppConstants.TAG_LOCATIONS
            )
        }
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        externalPartnerId = response.vendorid.toString()
//        presenter.getSiteById(homeActivity, externalPartnerId)
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
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentSummary(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessCreateBasketFromOrder(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id

        if (OrderHelper.deliveryMode == getString(R.string.mode_pickup) || OrderHelper.deliveryMode == getString(
                R.string.mode_curbside
            )
        ) {
            externalPartnerId = response.vendorid.toString()
            presenter.getSiteById(homeActivity, externalPartnerId)
//            presenter.addDeliveryModeToBasket(homeActivity, OrderHelper.deliveryMode, response.id)
        }
    }

    override fun onSuccessgetOrderHistoryFaves(response: OLOUserFavoriteBasketsResponse) {
        homeActivity.presenter.dismissProgress()

        adapterOrderHistoryFaves = OrderHistoryAdapterFaves(
            homeActivity,
            response.faves,
            object : OrderHistoryItemsFavesInterface {
                override fun remove(order: Fave) {
                    if (Engine().isNetworkAvailable(homeActivity)) {
                        homeActivity.presenter.showProgress()
                        presenter.deleteUserFavoriteBasket(
                            homeActivity,
                            Engine().getOloAuthToken(mySharedPreferences)!!, order.id.toLong()
                        )
                    } else {
                        Engine().showMsgDialog(
                            getString(R.string.app_name),
                            getString(R.string.error_network_connection),
                            homeActivity
                        )
                    }
                }

                override fun reorder(order: Fave) {
                    fromAll = false
                    fromFavOrder = true
                    homeActivity.clearOrderHelper()
                    if (Engine().isNetworkAvailable(homeActivity)) {
                        homeActivity.presenter.showProgress()
                        presenter.createBasketFromFavoriteOrder(
                            homeActivity, order.id.toString(),
                            Engine().getOloAuthToken(mySharedPreferences)!!
                        )


                    } else {
                        Engine().showMsgDialog(
                            getString(R.string.app_name),
                            getString(R.string.error_network_connection),
                            homeActivity
                        )
                    }
                }

            })
        rvActivityHistory?.adapter = adapterOrderHistoryFaves
    }

    override fun getOrderHistory() {
        presenter.getUserFavoriteBaskets(
            homeActivity,
            Engine().getOloAuthToken(mySharedPreferences)!!
        )
    }

    override fun onSuccessgetOrderHistory(response: OLOgetRecentOrdersResponse) {
        homeActivity.presenter.dismissProgress()

        adapterOrderHistory =
            OrderHistoryAdapter(homeActivity, response.orders, object : OrderHistoryItemsInterface {
                override fun reorder(order: Order) {
                    fromAll = true
                    fromFavOrder = true
                    homeActivity.clearOrderHelper()
                    OrderHelper.deliveryMode = order.deliverymode
                    if (order.deliverymode == getString(R.string.mode_curbside)) {
                        for (item in order.customfields) {
                            OrderHelper.carInfo =
                                order.customfields[0].value + ";" + order.customfields[1].value + ";" + order.customfields[2].value
                        }
                    }

                    if (Engine().isNetworkAvailable(homeActivity)) {
                        homeActivity.presenter.showProgress()
                        presenter.createBasketFromOrder(
                            homeActivity, order.id,
                            Engine().getOloAuthToken(mySharedPreferences)!!
                        )
                    } else {
                        Engine().showMsgDialog(
                            getString(R.string.app_name),
                            getString(R.string.error_network_connection),
                            homeActivity
                        )
                    }
                }

            })
        rvActivityHistory.adapter = adapterOrderHistory

    }

    override fun onLocationDetails(response: LocationsResponse?) {
        if (response != null) {
            for (item in response.locations) {
                addedLocation.add(
                    LoyaltyHistory(
                        name = item.name,
                        restaurant_id = item.id,
                        activity_type = "",
                        date = 0L,
                        points = 0
                    )
                )
            }
            presenter.getUserActivity(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString()
            )
        }
    }


    override fun onSuccessGetUserActivity(response: UserActivityResponse?) {
        loyaltyHistoty.clear()
        if (response?.receipts!!.isNotEmpty()) {
            for (item in response.receipts) {
                var date = 0L
                var points = 0
                var subtotal = 0.0
                if (item.activity_type == "receipts") {
                    date = item.last_transaction.created_at
                    points = item.last_transaction.total_points_earned
                    subtotal = item.last_transaction.subtotal
                } else if (item.activity_type == "points") {
                    date = item.created_at
                    points = item.point
                }

                if (item.activity_type == "payments") {
                    loyaltyPayment.add(
                        LoyaltyHistory(
                            date = date,
                            restaurant_id = item.restaurant_id,
                            name = "",
                            amount = item.amount,
                            activity_type = item.activity_type
                        )
                    )
                }
                if (item.activity_type == "payment") {
                } else if (item.activity_type == "receipts" && item.last_transaction.status == 3) {
                    loyaltyHistoty.add(
                        LoyaltyHistory(
                            date = date,
                            points = points,
                            subtotal = subtotal,
                            restaurant_id = item.restaurant_id,
                            name = "",
                            activity_type = item.activity_type
                        )
                    )
                } else if (item.activity_type != "receipts" && item.activity_type != "payment") {
                    loyaltyHistoty.add(
                        LoyaltyHistory(
                            date = date,
                            points = points,
                            subtotal = subtotal,
                            restaurant_id = item.restaurant_id,
                            name = "",
                            activity_type = item.activity_type
                        )
                    )
                }
            }
        }

        for (item in loyaltyHistoty) {
            if (item.restaurant_id != 0) {
                for (itm in addedLocation) {
                    if (item.restaurant_id == itm.restaurant_id) {
                        item.name = itm.name
                    }
                }
            }
        }

        for (item in loyaltyPayment) {
            if (item.restaurant_id != 0) {
                for (itm in addedLocation) {
                    if (item.restaurant_id == itm.restaurant_id) {
                        item.name = itm.name
                    }
                }
            }
        }

        presenter.getRewardsActivity(
            homeActivity,
            Engine().getAuthToken(mySharedPreferences).toString()
        )
    }

    override fun onSuccessGetRewardsActivity(response: RewardsActivityResponse?) {
        try {
            if (response?.activities!!.isNotEmpty()) {
                for (item in response.activities) {
                    loyaltyHistoty.add(
                        LoyaltyHistory(
                            date = item.claim_date,
                            points = item.reward.points,
                            name = item.reward.name,
                            activity_type = "rewards"
                        )
                    )
                }
            }
            homeActivity.presenter.dismissProgress()
            loyaltyHistoty.sortBy { it.date }
            adapter = LoyaltyHistoryAdapter(homeActivity, loyaltyHistoty.asReversed())
            rvActivityHistory.adapter = adapter
        } catch (er: IllegalStateException) {
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

    override fun onSuccessAddFields(response: OLOBasketResponse) {
        OrderHelper.basket = response
        numberFieldsSend++
        if (numberFieldsSend == 3) {
            homeActivity.presenter.dismissProgress()
            homeActivity.presenter.openFragmentRight(
                getFragmentSummary(
                    OrderSummaryFragment(), externalPartnerId
                ),
                AppConstants.TAG_ONLINE_ORDER
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromRewards =
                requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_REWARDS_ACTIVITY_HISTORY)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFromRewards) {
            btn_close.setImageResource(R.drawable.arrow_white_left)
            btn_close.contentDescription = getString(R.string.back_button)
        } else {
            btn_close.setImageResource(R.drawable.close_icon)
            btn_close.contentDescription = getString(R.string.close_button)
        }
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_history, container, false)
    }

    private fun getFragmentOrder(
        fragment: BaseFragment,
        location: LocationsResponse.Location
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        bundle.putParcelable(AppConstants.SELECTED_LOCATION, location)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HISTORY, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = ActivityHistoryPresenter(this)
        txt_loyalty_text.visibility = View.GONE
        setListeners()

        rvActivityHistory.layoutManager =
            LinearLayoutManager(homeActivity.applicationContext)

        initOrderHistory()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

            R.id.btn_loyalty_history -> {
                line_loyalty.visibility = View.VISIBLE
                line_order.visibility = View.GONE
                line_payment.visibility = View.GONE

                if (Build.VERSION.SDK_INT < 23) {
                    txt_loyalty.setTextAppearance(homeActivity, R.style.TextViewRedBold)
                    txt_order.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                    txt_payment.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                } else {
                    txt_loyalty.setTextAppearance(R.style.TextViewRedBold)
                    txt_order.setTextAppearance(R.style.TextViewDarkBoldHistory)
                    txt_payment.setTextAppearance(R.style.TextViewDarkBoldHistory)
                }
                initLoyaltyHistory()
            }

            R.id.btn_order_history -> {
                line_loyalty.visibility = View.GONE
                line_order.visibility = View.VISIBLE
                line_payment.visibility = View.GONE

                if (Build.VERSION.SDK_INT < 23) {
                    txt_loyalty.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                    txt_order.setTextAppearance(homeActivity, R.style.TextViewRedBold)
                    txt_payment.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                } else {
                    txt_loyalty.setTextAppearance(R.style.TextViewDarkBoldHistory)
                    txt_order.setTextAppearance(R.style.TextViewRedBold)
                    txt_payment.setTextAppearance(R.style.TextViewDarkBoldHistory)
                }
                initOrderHistory()
            }
            R.id.btn_payment_history -> {
                line_loyalty.visibility = View.GONE
                line_order.visibility = View.GONE
                line_payment.visibility = View.VISIBLE

                if (Build.VERSION.SDK_INT < 23) {
                    txt_loyalty.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                    txt_order.setTextAppearance(homeActivity, R.style.TextViewDarkBoldHistory)
                    txt_payment.setTextAppearance(homeActivity, R.style.TextViewRedBold)
                } else {
                    txt_loyalty.setTextAppearance(R.style.TextViewDarkBoldHistory)
                    txt_order.setTextAppearance(R.style.TextViewDarkBoldHistory)
                    txt_payment.setTextAppearance(R.style.TextViewRedBold)
                }
                initPaymentHistory()
            }

            R.id.btn_all_orders -> {
                line_all.visibility = View.VISIBLE
                line_fav.visibility = View.GONE
                if (Build.VERSION.SDK_INT < 23) {
                    txt_fav_orders.setTextAppearance(homeActivity, R.style.TextViewDarkHintBold)
                    txt_all_orders.setTextAppearance(homeActivity, R.style.TextViewWhite20)
                } else {
                    txt_fav_orders.setTextAppearance(homeActivity, R.style.TextViewDarkHintBold)
                    txt_all_orders.setTextAppearance(homeActivity, R.style.TextViewWhite20)
                }
                rvActivityHistory.adapter = null
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.getUserRecentOrders(
                        homeActivity,
                        Engine().getOloAuthToken(mySharedPreferences)!!
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_fav_orders -> {
                line_all.visibility = View.GONE
                line_fav.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT < 23) {
                    txt_fav_orders.setTextAppearance(homeActivity, R.style.TextViewWhite20)
                    txt_all_orders.setTextAppearance(homeActivity, R.style.TextViewDarkHintBold)
                } else {
                    txt_fav_orders.setTextAppearance(homeActivity, R.style.TextViewWhite20)
                    txt_all_orders.setTextAppearance(homeActivity, R.style.TextViewDarkHintBold)
                }

                rvActivityHistory.adapter = null
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.getUserFavoriteBaskets(
                        homeActivity,
                        Engine().getOloAuthToken(mySharedPreferences)!!
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }
        }
    }

    private fun initPaymentHistory() {
        layout_orders.visibility = View.GONE
        txt_loyalty_text.visibility = View.GONE
        rvActivityHistory.adapter = null
        loyaltyPayment.sortBy { it.date }
        adapter = LoyaltyHistoryAdapter(homeActivity, loyaltyPayment.asReversed())
        rvActivityHistory.adapter = adapter
    }

    private fun initOrderHistory() {
        txt_loyalty_text.visibility = View.GONE
        layout_orders.visibility = View.VISIBLE
        rvActivityHistory.adapter = null
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

    private fun initLoyaltyHistory() {

        layout_orders.visibility = View.GONE
        txt_loyalty_text.visibility = View.VISIBLE
        rvActivityHistory.adapter = null
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getLocations(
                homeActivity, BuildConfig.APPKEY, homeActivity.getGPSController()!!.latitude,
                homeActivity.getGPSController()!!.longitude, ""
            )
        } else {
            Engine().showMsgDialog("", getString(R.string.error_network_connection), homeActivity)
        }
    }


    private fun setListeners() {
        btn_payment_history.setOnClickListener(this)
        btn_loyalty_history.setOnClickListener(this)
        btn_order_history.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        btn_all_orders.setOnClickListener(this)
        btn_fav_orders.setOnClickListener(this)
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
        Engine.setNextPage = AppConstants.TAG_ACTIVITY_HISTORY
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

    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        presenter.addDeliveryModeToBasket(
            homeActivity,
            getString(R.string.mode_pickup),
            response.basket.id
        )
    }
}
package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.SelectOrderPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.*
import kotlinx.android.synthetic.main.fragment_select_order.*
import java.io.Serializable
import javax.inject.Inject


class SelectOrderModeFragment : BaseFragment(), SelectOrderPresenter.SelectOrderView,
    OnClickListener,
    View.OnTouchListener {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        try {
            if (response.basket.customfields.isNotEmpty()) {
                OrderHelper.filed_make = response.basket.customfields[0].id
                OrderHelper.filed_model = response.basket.customfields[1].id
                OrderHelper.filed_color = response.basket.customfields[2].id
            }
        } catch (er: IndexOutOfBoundsException) {
        }

        presenter.addDeliveryModeToBasket(homeActivity, delyveryMode, response.basket.id)
    }

    override fun onSuccessGetUserDeliveryAddresses(response: UserDeliveryAddressesResponse) {
        homeActivity.presenter.dismissProgress()
//        if (response.deliveryaddresses.isEmpty()) {
//            if (isFromHome!!) {
//                homeActivity.openFragmentRight(
//                    getFragmentDelivery(
//                        EnterDeliveryAddressFragment(),
//                        delyveryMode,
//                        selectedLocation!!.external_partner_id
//                    ),
//                    AppConstants.TAG_ONLINE_ORDER
//                )
//            } else {
//                homeActivity.presenter.openFragmentUp(
//                    getFragmentDelivery(
//                        EnterDeliveryAddressFragment(),
//                        delyveryMode,
//                        selectedLocation!!.external_partner_id
//                    ),
//                    AppConstants.TAG_ONLINE_ORDER
//                )
//            }
//        } else {
//            if (isFromHome!!) {
//                homeActivity.presenter.openFragmentRight(
//                    getFragmentDelivery(
//                        DeliveryAddresesFragment(),
//                        delyveryMode,
//                        selectedLocation!!.external_partner_id, response.deliveryaddresses
//                    ),
//                    AppConstants.TAG_ONLINE_ORDER
//                )
//            } else {
//                homeActivity.presenter.openFragmentUp(
//                    getFragmentDelivery(
//                        DeliveryAddresesFragment(),
//                        delyveryMode,
//                        selectedLocation!!.external_partner_id, response.deliveryaddresses
//                    ),
//                    AppConstants.TAG_ONLINE_ORDER
//                )
//            }
//        }
    }

    private fun getFragmentDelivery(
        fragment: BaseFragment,
        deliveryMode: String,
        external_partner_id: String,
        deliveryaddresses: List<Deliveryaddresse>
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.DELIVERY_TYPE, deliveryMode)
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putSerializable(AppConstants.DELIVERY_ADDRESSES, deliveryaddresses as Serializable)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        if (delyveryMode == getString(R.string.mode_curbside)) {
            if (isFromHome!!) {
                homeActivity.presenter.openFragmentRight(
                    getFragmentCarDetails(
                        EnterCarDetailsFragment(), selectedLocation!!.external_partner_id
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            } else {
                homeActivity.presenter.openFragmentUp(
                    getFragmentCarDetails(
                        EnterCarDetailsFragment(), selectedLocation!!.external_partner_id
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }
        } else if (delyveryMode == getString(R.string.mode_pickup)) {
            if (isFromHome!!) {
                homeActivity.presenter.openFragmentRight(
                    getFragmentMenu(
                        OrderMenuFragment(), selectedLocation!!.external_partner_id
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            } else {
                homeActivity.presenter.openFragmentUp(
                    getFragmentMenu(
                        OrderMenuFragment(), selectedLocation!!.external_partner_id
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }
        }
    }

    private fun getFragmentMenu(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentCarDetails(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id
        try {
            if (response.customfields.isNotEmpty()) {
                OrderHelper.filed_make = response.customfields[0].id
                OrderHelper.filed_model = response.customfields[1].id
                OrderHelper.filed_color = response.customfields[2].id
            }
        } catch (er: IndexOutOfBoundsException) {
        }
        presenter.addDeliveryModeToBasket(homeActivity, delyveryMode, response.id)
    }

    private var alertDialogBuilder: AlertDialog.Builder? = null
    private var selectedLocation: LocationsResponse.Location? = null
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: SelectOrderPresenter
    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var slideUp: Animation
    private lateinit var slideDown: Animation
    private var delyveryMode = ""
    private var dispatch = true
    private var isFromHome: Boolean? = null
    private var isFromHistory: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromHome = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            selectedLocation = arguments!!.getParcelable(AppConstants.SELECTED_LOCATION)
            isFromHistory = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HISTORY)
        }
    }

    override fun onResume() {
        super.onResume()

        if (isFromHome!!) {
            btn_close.setImageResource(R.drawable.close_icon)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        } else {
            btn_close.setImageResource(R.drawable.arrow_white_left)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)
        }

        if (isFromHistory) {
            btn_close.setImageResource(R.drawable.arrow_white_left)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        }

        if (OrderHelper.location == null) {
            OrderHelper.location = selectedLocation
            OrderHelper.locationAddress = selectedLocation!!.address
        } else {
            selectedLocation = OrderHelper.location

        }

        if (selectedLocation!!.external_partner_id.isEmpty()) {
            Engine().showMsgDialogNoTitleClose(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = SelectOrderPresenter(this)

        setListeners()
        slideUp = AnimationUtils.loadAnimation(homeActivity, R.anim.slide_in_up)
        slideDown = AnimationUtils.loadAnimation(homeActivity, R.anim.slide_down)

        btn_pickup.alpha = 0.5F
        btn_pickup.isClickable = false
        btn_pickup.isEnabled = false

        btn_catering.alpha = 0.5F
        btn_catering.isClickable = false
        btn_catering.isEnabled = false

        btn_pickup.visibility = View.GONE
        btn_catering.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_catering -> {
                Engine().hideSoftKeyboard(homeActivity)
                OrderHelper.carInfo = ""

                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    delyveryMode = getString(R.string.mode_curbside)
                    OrderHelper.deliveryMode = delyveryMode

                    if (OrderHelper.basket != null) {
                        if (OrderHelper.basket!!.vendorid == selectedLocation!!.external_partner_id.toInt()) {
                            presenter.addDeliveryModeToBasket(
                                homeActivity,
                                delyveryMode,
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
                                                    Engine().getOloAuthToken(mySharedPreferences).toString()
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
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_pickup -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    Engine().hideSoftKeyboard(homeActivity)
                    homeActivity.presenter.showProgress()
                    delyveryMode = getString(R.string.mode_pickup)
                    OrderHelper.deliveryMode = delyveryMode
                    if (OrderHelper.basket != null) {
                        if (OrderHelper.basket!!.vendorid == selectedLocation!!.external_partner_id.toInt()) {
                            presenter.addDeliveryModeToBasket(
                                homeActivity,
                                delyveryMode,
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
                                                    Engine().getOloAuthToken(mySharedPreferences).toString()
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
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }


            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

        }
    }


    private fun setListeners() {
        btn_pickup.setOnClickListener(this)
        btn_catering.setOnClickListener(this)
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
        Engine.setNextPage = AppConstants.TAG_PROMO
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    override fun getSitebyIdafterCloudConnect() {
        presenter.getSiteById(homeActivity, selectedLocation!!.external_partner_id)
    }

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

        try {
            if (response.supportsonlineordering) {
                if (response.isavailable) {
                    if (response.canpickup) {
                        btn_pickup?.alpha = 1.0F
                        btn_pickup?.isClickable = true
                        btn_pickup?.isEnabled = true
                        btn_pickup?.visibility = View.VISIBLE
                    }

                    if (response.supportscurbside) {
                        btn_catering.alpha = 1.0F
                        btn_catering.isClickable = true
                        btn_catering.isEnabled = true
                        btn_catering.visibility = View.VISIBLE
                    }

                    if (response.candeliver) {
                        dispatch = false
                    } else if (response.supportsdispatch) {
                        dispatch = true
                    }
                } else {
                    Engine().showMsgDialogNoTitleClose(
                        getString(R.string.cannot_acceps_orders_error),
                        homeActivity
                    )
                }

            } else {
                Engine().showMsgDialogNoTitleClose(
                    getString(R.string.cannot_acceps_orders_error),
                    homeActivity
                )
            }
        } catch (ex: IllegalStateException) {
            homeActivity.presenter.dismissProgress()
        }
    }

    override fun onPause() {
        super.onPause()
        homeActivity.presenter.dismissProgress()
        presenter.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
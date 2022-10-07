package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.DeliveryAddressesAdapter
import com.tts.gueststar.interfaces.ChooseDeliveryAddressInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.DeliveryAddressPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.*
import kotlinx.android.synthetic.main.fragment_delivery_addresses.*
import kotlinx.android.synthetic.main.fragment_delivery_addresses.btn_close
import kotlinx.android.synthetic.main.fragment_delivery_addresses.mainLayout
import javax.inject.Inject

class DeliveryAddresesFragment : BaseFragment(),
    OnClickListener,
    View.OnTouchListener, DeliveryAddressPresenter.DeliveryAddressView {

    private lateinit var homeActivity: MainActivity
    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var slideUp: Animation
    private lateinit var slideDown: Animation
    private var deliveryMode = ""
    private var externalPartnerId = ""
    private var deliveryAddresses: MutableList<Deliveryaddresse>? = null
    private lateinit var adapter: DeliveryAddressesAdapter
    lateinit var presenter: DeliveryAddressPresenter
    private var selectedAddress: Deliveryaddresse? = null
    private var isFromHome: Boolean? = null
    private var isFromSummary: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromHome = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            deliveryMode = arguments!!.getString(AppConstants.DELIVERY_TYPE)!!
            externalPartnerId = arguments!!.getString(AppConstants.EXTERNAL_ID)!!
            isFromSummary = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY)
            try {
                deliveryAddresses =
                    arguments!!.getSerializable(AppConstants.DELIVERY_ADDRESSES) as MutableList<Deliveryaddresse>
            } catch (er: TypeCastException) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)

        if (isFromHome!!) {
            btn_close.setImageResource(R.drawable.arrow_left_black)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        } else {
            btn_close.setImageResource(R.drawable.close_icon)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        }

        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getUserDeliveryAddresses(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delivery_addresses, container, false)
    }

    override fun onPause() {
        super.onPause()
        homeActivity.presenter.dismissProgress()
    }

    override fun onStop() {
        super.onStop()
        homeActivity.presenter.dismissProgress()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = DeliveryAddressPresenter(this)
        setListeners()
        slideUp = AnimationUtils.loadAnimation(homeActivity, R.anim.slide_in_up_new)
        slideDown = AnimationUtils.loadAnimation(homeActivity, R.anim.slide_down_new)

        if (deliveryAddresses != null) {
            rvDeliveryAddresses.layoutManager =
                LinearLayoutManager(homeActivity.applicationContext)
            adapter = DeliveryAddressesAdapter(
                homeActivity,
                deliveryAddresses!!,
                object : ChooseDeliveryAddressInterface {
                    override fun deleteDeliveryAddress(address: Deliveryaddresse) {
                        if (Engine().isNetworkAvailable(homeActivity)) {
                            homeActivity.presenter.showProgress()
                            presenter.deleteUserDeliveryAddresses(
                                homeActivity,
                                Engine().getOloAuthToken(mySharedPreferences)!!, address.id
                            )
                        } else {
                            Engine().showMsgDialog(
                                getString(R.string.app_name),
                                getString(R.string.error_network_connection),
                                homeActivity
                            )
                        }

                    }

                    override fun chooseDeliveryAddress(address: Deliveryaddresse) {
                        selectedAddress = address
                        txt_saved_addresses.text = address.streetaddress
                        OrderHelper.deliveryAddress = address.streetaddress + "\n" + address.city
                        btn_continue.alpha = 1.0F
                        btn_continue.isEnabled = true
                        btn_continue.isClickable = true

                        layout_addresses.clearAnimation()
                        layout_addresses.startAnimation(slideUp)
                        layout_addresses.visibility = View.GONE
                        line4.visibility = View.INVISIBLE
                    }
                })

            rvDeliveryAddresses.adapter = adapter
        }
        btn_continue.alpha = 0.5F
        btn_continue.isEnabled = false
        btn_continue.isClickable = false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.add_addresses -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.presenter.openFragmentRight(
                    getFragmentDelivery(
                        EnterDeliveryAddressFragment(),
                        deliveryMode,
                        externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }

            R.id.btn_continue -> {
                Engine().hideSoftKeyboard(homeActivity)

                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    if (isFromSummary) {
                        homeActivity.onBackPressed()
                    } else {
                        if (OrderHelper.basket != null) {
                            if (OrderHelper.basket!!.vendorid == externalPartnerId.toInt()) {
                                val isDispatch = deliveryMode == getString(R.string.mode_dispatch)
                                presenter.addDeliveryAddressWithId(
                                    homeActivity,
                                    isDispatch,
                                    selectedAddress!!.id,
                                    selectedAddress!!.streetaddress,
                                    selectedAddress!!.building,
                                    selectedAddress!!.city,
                                    selectedAddress!!.zipcode,
                                    selectedAddress!!.specialinstructions,
                                    OrderHelper.basketId!!
                                )
                            } else {
                                if (OrderHelper.supportsbaskettransfers) {
                                    //transfer
                                    presenter.transferBasket(
                                        homeActivity,
                                        externalPartnerId.toInt(),
                                        OrderHelper.basketId!!
                                    )
                                } else {
                                    presenter.createBasket(
                                        homeActivity,
                                        externalPartnerId.toInt(),
                                        Engine().getOloAuthToken(mySharedPreferences).toString()
                                    )
                                }
                            }
                        } else {
                            presenter.createBasket(
                                homeActivity,
                                externalPartnerId.toInt(),
                                Engine().getOloAuthToken(mySharedPreferences).toString()
                            )
                        }
                    }
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.saved_addresses -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (deliveryAddresses!!.isNotEmpty()) {
                    if (layout_addresses.visibility == View.GONE) {
                        layout_addresses.clearAnimation()
                        layout_addresses.startAnimation(slideDown)
                        Handler().postDelayed({
                            layout_addresses.visibility = View.VISIBLE
                            line4.visibility = View.VISIBLE
                        }, 400)
                        layout_addresses.bringToFront()
                    } else {
                        layout_addresses.clearAnimation()
                        layout_addresses.startAnimation(slideUp)
                        Handler().postDelayed({
                            layout_addresses.visibility = View.GONE
                            line4.visibility = View.INVISIBLE
                        }, 400)

                    }
                }
            }
        }
    }

    private fun getFragmentDelivery(
        fragment: BaseFragment,
        deliveryMode: String,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.DELIVERY_TYPE, deliveryMode)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_DELIVERY_ADDRESSES, true)
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY, isFromSummary)
        fragment.arguments = bundle
        return fragment
    }

    private fun setListeners() {
        btn_close.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
        add_addresses.setOnClickListener(this)
        saved_addresses.setOnClickListener(this)
        btn_continue.setOnClickListener(this)
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

    private fun getFragmentMenu(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_CURBSIDE_DELIVERY, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.openFragmentRight(
            getFragmentMenu(
                OrderMenuFragment(), externalPartnerId
            ),
            AppConstants.TAG_ONLINE_ORDER
        )
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id
        val isDispatch = deliveryMode == getString(R.string.mode_dispatch)
        presenter.addDeliveryAddressWithId(
            homeActivity,
            isDispatch,
            selectedAddress!!.id,
            selectedAddress!!.streetaddress,
            selectedAddress!!.building,
            selectedAddress!!.city,
            selectedAddress!!.zipcode,
            selectedAddress!!.specialinstructions,
            OrderHelper.basketId!!
        )
    }

    override fun onSuccessAddDeliveryAddress(response: AddDeliveryAddressResponse) {
        presenter.addDeliveryModeToBasket(homeActivity, deliveryMode, OrderHelper.basketId!!)
    }

    override fun disableButton() {
    }

    override fun enableButton() {
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

    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        presenter.addDeliveryModeToBasket(homeActivity, deliveryMode, response.basket.id)
    }

    override fun onSuccessDeleteAddress() {
        presenter.getUserDeliveryAddresses(
            homeActivity,
            Engine().getOloAuthToken(mySharedPreferences)!!
        )
    }

    override fun onSuccessGetUserDeliveryAddresses(response: UserDeliveryAddressesResponse) {
        homeActivity.presenter.dismissProgress()
        if (response.deliveryaddresses.isEmpty()) {
            layout_addresses.clearAnimation()
            layout_addresses.startAnimation(slideUp)
            layout_addresses.visibility = View.GONE
            line4.visibility = View.INVISIBLE

            btn_continue.alpha = 0.5F
            btn_continue.isEnabled = false
            btn_continue.isClickable = false
            txt_saved_addresses.text = getString(R.string.saved_addresses)
            deliveryAddresses!!.clear()

        } else {
            deliveryAddresses = response.deliveryaddresses as MutableList<Deliveryaddresse>
            try {
                rvDeliveryAddresses.layoutManager =
                    LinearLayoutManager(homeActivity.applicationContext)
                adapter = DeliveryAddressesAdapter(
                    homeActivity,
                    deliveryAddresses!!,
                    object : ChooseDeliveryAddressInterface {
                        override fun deleteDeliveryAddress(address: Deliveryaddresse) {
                            if (Engine().isNetworkAvailable(homeActivity)) {
                                homeActivity.presenter.showProgress()
                                presenter.deleteUserDeliveryAddresses(
                                    homeActivity,
                                    Engine().getOloAuthToken(mySharedPreferences)!!, address.id
                                )
                            } else {
                                Engine().showMsgDialog(
                                    getString(R.string.app_name),
                                    getString(R.string.error_network_connection),
                                    homeActivity
                                )
                            }
                        }

                        override fun chooseDeliveryAddress(address: Deliveryaddresse) {
                            selectedAddress = address

                            TextViewCompat.setTextAppearance(txt_saved_addresses, R.style.TextView)
                            txt_saved_addresses.text = address.streetaddress
                            OrderHelper.deliveryAddress =
                                address.streetaddress + "\n" + address.city
                            btn_continue.alpha = 1.0F
                            btn_continue.isEnabled = true
                            btn_continue.isClickable = true

                            layout_addresses.clearAnimation()
                            layout_addresses.startAnimation(slideUp)
                            layout_addresses.visibility = View.GONE
                            line4.visibility = View.INVISIBLE
                        }
                    })

                rvDeliveryAddresses.adapter = adapter
            } catch (er: IllegalStateException) {
                homeActivity.presenter.dismissProgress()
            }
        }
    }
}
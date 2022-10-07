package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.DeliveryAddressPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.AddDeliveryAddressResponse
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.UserDeliveryAddressesResponse
import kotlinx.android.synthetic.main.fragment_enter_delivery_address.*
import javax.inject.Inject

class EnterDeliveryAddressFragment : BaseFragment(), View.OnClickListener,
    DeliveryAddressPresenter.DeliveryAddressView {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        presenter.addDeliveryModeToBasket(homeActivity, deliveryMode, response.basket.id)
    }

    override fun onSuccessDeleteAddress() {
    }

    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: DeliveryAddressPresenter
    private var deliveryMode = ""
    private var externalPartnerId = ""
    private var isFromHome: Boolean? = null
    private var isFromSummary: Boolean = false

    private var isFromDeliveryAddresses: Boolean = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            deliveryMode = arguments!!.getString(AppConstants.DELIVERY_TYPE)!!
            externalPartnerId = arguments!!.getString(AppConstants.EXTERNAL_ID)!!
            isFromHome = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            isFromDeliveryAddresses =
                arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_DELIVERY_ADDRESSES)
            isFromSummary = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enter_delivery_address, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = DeliveryAddressPresenter(this)

        setTextWatcherForAmountEditView(etStreet)
        setTextWatcherForAmountEditView(etBuildingName)
        setTextWatcherForAmountEditView(etCity)
        setTextWatcherForAmountEditView(etZipCode)
        setTextWatcherForAmountEditView(etOtherIns)

        Engine().setEnableButton(btn_proceed_to_order, false)
        btn_proceed_to_order.setOnClickListener(this)
        btn_close.setOnClickListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter.addressFieldsValidateion(
                    etStreet.text.toString(),
                    etBuildingName.text.toString(),
                    etCity.text.toString(),
                    etZipCode.text.toString()
                )
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun onPause() {
        super.onPause()
        homeActivity.presenter.dismissProgress()
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

        if (isFromDeliveryAddresses) {
            btn_close.setImageResource(R.drawable.arrow_left_black)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }

            R.id.btn_proceed_to_order -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()

                    if (isFromSummary) {
                        val isDispatch = deliveryMode == getString(R.string.mode_dispatch)
                        presenter.addDeliveryAddress(
                            homeActivity,
                            isDispatch,
                            etStreet.text.toString(),
                            etBuildingName.text.toString(),
                            etCity.text.toString(),
                            etZipCode.text.toString(),
                            etOtherIns.text.toString(),
                            OrderHelper.basketId!!
                        )
                    } else {
                        if (OrderHelper.basket != null) {
                            if (OrderHelper.basket!!.vendorid == externalPartnerId.toInt()) {

                                val isDispatch = deliveryMode == getString(R.string.mode_dispatch)
                                presenter.addDeliveryAddress(
                                    homeActivity,
                                    isDispatch,
                                    etStreet.text.toString(),
                                    etBuildingName.text.toString(),
                                    etCity.text.toString(),
                                    etZipCode.text.toString(),
                                    etOtherIns.text.toString(),
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
        }
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_proceed_to_order, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_proceed_to_order, true)
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

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        OrderHelper.basketId = response.id
        OrderHelper.basket = response
        val isDispatch = deliveryMode == getString(R.string.mode_dispatch)
        presenter.addDeliveryAddress(
            homeActivity,
            isDispatch,
            etStreet.text.toString(),
            etBuildingName.text.toString(),
            etCity.text.toString(),
            etZipCode.text.toString(),
            etOtherIns.text.toString(),
            OrderHelper.basketId!!
        )
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

    override fun onSuccessAddDeliveryAddress(response: AddDeliveryAddressResponse) {
        presenter.addDeliveryModeToBasket(
            homeActivity,
            deliveryMode,
            OrderHelper.basketId!!
        )

    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        OrderHelper.deliveryAddress = etStreet.text.toString() + "\n" + etCity.text.toString()
        homeActivity.onBackPressed()
        if (!isFromSummary) {
            homeActivity.presenter.openFragmentRight(
                getFragmentMenu(
                    OrderMenuFragment(), externalPartnerId
                ),
                AppConstants.TAG_ONLINE_ORDER
            )
        }else{
            homeActivity.onBackPressed()
        }

    }

    override fun onSuccessGetUserDeliveryAddresses(response: UserDeliveryAddressesResponse) {
    }
}

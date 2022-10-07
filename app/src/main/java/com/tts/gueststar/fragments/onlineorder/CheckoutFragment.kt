package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.TextViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.OrderCheckoutPresenter
import javax.inject.Inject
import com.tts.gueststar.utility.*
import com.tts.olosdk.models.Billingscheme
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OLOUserFavoriteBasketsResponse
import com.tts.olosdk.models.OLOgetBillingSchemesResponse
import kotlinx.android.synthetic.main.discount_item.view.*
import kotlinx.android.synthetic.main.favorite_order_dialog.view.*
import kotlinx.android.synthetic.main.fragment_order_checkout.*
import kotlinx.android.synthetic.main.fragment_order_checkout.btn15Percentage
import kotlinx.android.synthetic.main.fragment_order_checkout.btn15Value
import kotlinx.android.synthetic.main.fragment_order_checkout.btn18Percentage
import kotlinx.android.synthetic.main.fragment_order_checkout.btn18Value
import kotlinx.android.synthetic.main.fragment_order_checkout.btn20Percentage
import kotlinx.android.synthetic.main.fragment_order_checkout.btn20Value
import kotlinx.android.synthetic.main.fragment_order_checkout.btnOtherText
import kotlinx.android.synthetic.main.fragment_order_checkout.btn_close
import kotlinx.android.synthetic.main.fragment_order_checkout.softKeyTrigger
import kotlinx.android.synthetic.main.fragment_order_checkout.txt_subtotal
import kotlinx.android.synthetic.main.fragment_order_checkout.txt_tax
import kotlinx.android.synthetic.main.fragment_order_checkout.txt_total
import java.math.RoundingMode
import java.text.DecimalFormat

class CheckoutFragment : BaseFragment(), View.OnClickListener,
    OrderCheckoutPresenter.OrderCheckoutView {
    private var billingMethod: String = ""
    private var creditcardBillingSchemes: Billingscheme? = null
    private var giftcardBillingSchemes: Billingscheme? = null
    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: OrderCheckoutPresenter
    private var flagAMAN = true
    private var flagDeleteCoupon = false
    private var flagBtnHomeRemoveCoupon = false
    private var giftCard = false
    private var flagOther = false

    override fun onSuccessSubmitBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.openFragmentRightDown(
            OrderThankYouFragment(),
            AppConstants.TAG_ADD_CREDIT_CARD
        )
    }

    override fun onSuccessGetBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        homeActivity.presenter.dismissProgress()
        initView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_checkout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            externalPartnerId = requireArguments().getString(AppConstants.EXTERNAL_ID)!!
        }
    }


    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = OrderCheckoutPresenter(this)
        btn_close.setOnClickListener(this)
        btn15.setOnClickListener(this)
        btn20.setOnClickListener(this)
        btn18.setOnClickListener(this)
        txt_save_to_favorite.setOnClickListener(this)
        layout_resraunt.setOnClickListener(this)
        layout_gift.setOnClickListener(this)
        change_giftt_card.setOnClickListener(this)
        layout_credit_card.setOnClickListener(this)
        btn_apply.setOnClickListener(this)
        btn_remove_coupon.setOnClickListener(this)
        btn_place_order.setOnClickListener(this)
        btn_home.setOnClickListener(this)
        change_credit_card.setOnClickListener(this)
        btnOtherText.setOnClickListener(this)

        change_credit_card.paintFlags =
            change_credit_card.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        txt_save_to_favorite.paintFlags =
            txt_save_to_favorite.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        btn_apply.isClickable = false
        btn_apply.isEnabled = false
        btn_apply.alpha = 0.5F
        //    setNoTip()
        softKeyTrigger.setOnFocusChangeListener { v, hasFocus ->
            when (v.id) {
                R.id.softKeyTrigger -> {
                    if (hasFocus)
                        setTipOther()
                }
            }
        }
        setTextWatcherForAmountEditViewDiscount(etDiscountCode)
        softKeyTrigger.filters = arrayOf(DigitsInputFilter(3, 2), InputFilterMinMax(0.0, 100.0))
        setTextWatcherForAmountEditView(softKeyTrigger)

        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getBillingSchemes(homeActivity, OrderHelper.basketId!!)
            presenter.getBasket(homeActivity, OrderHelper.basketId!!)
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }

        btn_place_order.isClickable = false
        btn_place_order.isEnabled = false
        btn_place_order.alpha = 0.5F

        if (OrderHelper.restaurantDetails!!.supportstip) {
            presenter.applyTipToBasket(
                homeActivity,
                OrderHelper.basketId!!,
                0.toDouble()
            )
        }


        btnOtherText.bringToFront()

//        view.viewTreeObserver.addOnGlobalLayoutListener {
//            val r = Rect()
//            //r will be populated with the coordinates of your view that area still visible.
//            view.getWindowVisibleDisplayFrame(r)
//
//            val heightDiff = view.rootView.height - (r.bottom - r.top)
//            if (heightDiff > 450) {
//                try {
//                    btn_place_order.visibility = View.GONE
//                } catch (er: IllegalStateException) {
//
//                }
//
//
//                if (softKeyTrigger.isFocused) {
// //                 flagAMAN = false
//                    if (flagAMAN) {
//                        flagAMAN = false
//
//
//
//                        softKeyTrigger.setText("")
//                        presenter.applyTipToBasket(
//                            homeActivity,
//                            OrderHelper.basketId!!,
//                            0.toDouble()
//                        )
//                        setTipOther()
//
//                        Handler().postDelayed({
//                            tip_layout.visibility = View.VISIBLE
//                            txt_tip.text = "$" + String.format("%.2f", 0.00)
//                        }, 400)
//                          }
//                    }
//                } else {
//                    try {
//                        btn_place_order.visibility = View.VISIBLE
//                    } catch (er: IllegalStateException) {
//
//                    }
//
//                }
//            }
    }

    @SuppressLint("InflateParams")
    fun openFavoriteOrderDialog() {
        val mDialogView =
            LayoutInflater.from(homeActivity).inflate(R.layout.favorite_order_dialog, null)
        val mBuilder = AlertDialog.Builder(homeActivity)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialogView.btn_save.isClickable = false
        mDialogView.btn_save.isEnabled = false
        mDialogView.btn_save.alpha = 0.5F
        mDialogView.txt_cancel.paintFlags =
            mDialogView.txt_cancel.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        mDialogView.save_as_default.paintFlags =
            mDialogView.save_as_default.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        mDialogView.etDiscountCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    mDialogView.btn_save.isClickable = true
                    mDialogView.btn_save.isEnabled = true
                    mDialogView.btn_save.alpha = 1F
                } else {
                    mDialogView.btn_save.isClickable = false
                    mDialogView.btn_save.isEnabled = false
                    mDialogView.btn_save.alpha = 0.5F
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    mDialogView.btn_save.isClickable = true
                    mDialogView.btn_save.isEnabled = true
                    mDialogView.btn_save.alpha = 1F
                } else {
                    mDialogView.btn_save.isClickable = false
                    mDialogView.btn_save.isEnabled = false
                    mDialogView.btn_save.alpha = 0.5F
                }
            }
        })

        mDialogView.btn_save.setOnClickListener {
            val name = mDialogView.etDiscountCode.text.toString()
            if (name.isNotEmpty()) {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.addFavoriteBasket(
                        homeActivity,
                        OrderHelper.basketId!!,
                        name,
                        Engine().getOloAuthToken(mySharedPreferences)!!,
                        mDialogView.cbSaveAsDefault.isChecked
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
                mAlertDialog.dismiss()
            }
        }
        mDialogView.btn_cancel.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }


    private fun setTip15() {
        OrderHelper.tip = "15"
        flagAMAN = true
        softKeyTrigger.clearFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(softKeyTrigger.windowToken, 0)
        if (Engine().isNetworkAvailable(homeActivity)) {
            //  softKeyTrigger.setText("")
            Engine().hideSoftKeyboard(homeActivity)
            TextViewCompat.setTextAppearance(btn15Percentage, R.style.TextViewRed15)
            TextViewCompat.setTextAppearance(btn15Value, R.style.TextViewRed11)
            TextViewCompat.setTextAppearance(btn18Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn18Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btn20Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn20Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btnOtherText, R.style.TextViewWhite15)
            //      homeActivity.presenter.showProgress()
            presenter.applyTipToBasket(
                homeActivity,
                OrderHelper.basketId!!,
                (OrderHelper.basket!!.subtotal) * 0.15
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    private fun setTip18() {
        OrderHelper.tip = "25"
        flagAMAN = true
        if (Engine().isNetworkAvailable(homeActivity)) {
            ////        softKeyTrigger.setText("")
            Engine().hideSoftKeyboard(homeActivity)
            TextViewCompat.setTextAppearance(btn15Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn15Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btn18Percentage, R.style.TextViewRed15)
            TextViewCompat.setTextAppearance(btn18Value, R.style.TextViewRed11)
            TextViewCompat.setTextAppearance(btn20Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn20Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btnOtherText, R.style.TextViewWhite15)
            homeActivity.presenter.showProgress()
            presenter.applyTipToBasket(
                homeActivity,
                OrderHelper.basketId!!,
                (OrderHelper.basket!!.subtotal) * 0.25
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    private fun setTip20() {
        OrderHelper.tip = "20"
        flagAMAN = true
        if (Engine().isNetworkAvailable(homeActivity)) {
            //    softKeyTrigger.setText("")
            Engine().hideSoftKeyboard(homeActivity)
            TextViewCompat.setTextAppearance(btn15Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn15Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btn18Percentage, R.style.TextViewWhite15)
            TextViewCompat.setTextAppearance(btn18Value, R.style.TextViewWhite11)
            TextViewCompat.setTextAppearance(btn20Percentage, R.style.TextViewRed15)
            TextViewCompat.setTextAppearance(btn20Value, R.style.TextViewRed11)
            TextViewCompat.setTextAppearance(btnOtherText, R.style.TextViewWhite15)
            homeActivity.presenter.showProgress()
            presenter.applyTipToBasket(
                homeActivity,
                OrderHelper.basketId!!,
                (OrderHelper.basket!!.subtotal) * 0.20
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    private fun setTipOther() {
        OrderHelper.tip = "other"
        TextViewCompat.setTextAppearance(btnOtherText, R.style.TextViewRed15)
        TextViewCompat.setTextAppearance(btn15Percentage, R.style.TextViewWhite15)
        TextViewCompat.setTextAppearance(btn15Value, R.style.TextViewWhite11)
        TextViewCompat.setTextAppearance(btn18Percentage, R.style.TextViewWhite15)
        TextViewCompat.setTextAppearance(btn18Value, R.style.TextViewWhite11)
        TextViewCompat.setTextAppearance(btn20Percentage, R.style.TextViewWhite15)
        TextViewCompat.setTextAppearance(btn20Value, R.style.TextViewWhite11)
    }

//    private fun setNoTip() {
//        TextViewCompat.setTextAppearance(btn15Percentage, R.style.TextViewWhite15)
//        TextViewCompat.setTextAppearance(btn15Value, R.style.TextViewWhite11)
//        TextViewCompat.setTextAppearance(btn18Percentage, R.style.TextViewWhite15)
//        TextViewCompat.setTextAppearance(btn18Value, R.style.TextViewWhite11)
//        TextViewCompat.setTextAppearance(btn20Percentage, R.style.TextViewWhite15)
//        TextViewCompat.setTextAppearance(btn20Value, R.style.TextViewWhite11)
//        TextViewCompat.setTextAppearance(btnOtherText, R.style.TextViewWhite15)
//    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        OrderHelper.vendorId = externalPartnerId.toInt()

        layout_credit_card.requestFocus()
        initView()
        if (OrderHelper.location != null) {
            tvLocationName.text = OrderHelper.location!!.name
            tvLocationAddress.text = OrderHelper.location!!.address
            tvLocationState.text = getString(
                R.string.location_state_text,
                OrderHelper.location!!.city_label,
                Engine().convertState(OrderHelper.location!!.state_label),
                OrderHelper.location!!.zipcode
            ) + "\n" + OrderHelper.location!!.phone_number.replace("(", "").replace(
                ")",
                ""
            ).replace(" ", "-")

            if (OrderHelper.location!!.restaurant_distance == 0.0) {
                tvLocationDistance.visibility = View.INVISIBLE
            } else {
                tvLocationDistance.visibility = View.VISIBLE
                tvLocationDistance.text = getString(
                    R.string.location_distance_text,
                    OrderHelper.location!!.restaurant_distance.toString()
                )
            }

        }

        when (OrderHelper.deliveryMode) {
            getString(R.string.mode_pickup) -> {
                order_method.text = getString(R.string.pickup)
                layout_car.visibility = View.GONE
                layout_delivery.visibility = View.GONE
            }
            getString(R.string.mode_curbside) -> {
                order_method.text = getString(R.string.catering)
                layout_car.visibility = View.VISIBLE
                layout_delivery.visibility = View.GONE
                car_info.text = OrderHelper.carInfo.replace(";", " ")
            }
            else -> {
                order_method.text = getString(R.string.delivery)
                layout_car.visibility = View.GONE
                layout_delivery.visibility = View.VISIBLE
                delyvery_info.text = OrderHelper.deliveryAddress
                line1.visibility = View.GONE
            }
        }

        if (OrderHelper.pickupTime != null) {
            txt_pickup_time.text = OrderHelper.pickupTime
        } else {
            txt_pickup_time.text =
                getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                    R.string.minutes
                ) + ")"
        }

        when (OrderHelper.tip) {
            "other" -> {
                setTipOther()
            }
            "15" -> {
                setTip15()
            }
            "20" -> {
                setTip20()
            }
            "25" -> {
                setTip18()
            }
        }


        if (OrderHelper.currentAddedGiftCard != null) {
            change_giftt_card.visibility = View.VISIBLE
            val length = OrderHelper.currentAddedGiftCard!!.value
            txt_gift_card.text =
                getString(
                    R.string.ending_with_field_summary,
                    length?.substring(length.length - 4, length.length)
                ) + " $" + String.format("%.2f", OrderHelper.currentAddedGiftCardBalance)
        }
        if (giftCard) {
            if (OrderHelper.fromSummary) {
                if (OrderHelper.currentAddedGiftCard != null) {
                    change_giftt_card.visibility = View.VISIBLE
                    val length = OrderHelper.currentAddedGiftCard!!.value
                    txt_gift_card.text =
                        getString(
                            R.string.ending_with_field_summary,
                            length?.substring(length.length - 4, length.length)
                        ) + " $" + String.format("%.2f", OrderHelper.currentAddedGiftCardBalance)
                }
            } else {
                if (OrderHelper.currentAddedGiftCard != null) {
                    billingMethod = getString(R.string.type_stored_value)
                    btn_place_order.isClickable = true
                    btn_place_order.isEnabled = true
                    btn_place_order.alpha = 1F
                    cbRestaurant.setImageResource(R.drawable.radio_check_off)
                    cbGiftCard.setImageResource(R.drawable.ic_check_circle)
                    cbCreditCard.setImageResource(R.drawable.radio_check_off)

                    if (OrderHelper.restaurantDetails!!.supportstip) {
                        if (OrderHelper.basket!!.allowstip) {
                            tip_section.visibility = View.VISIBLE
                            if (tip_layout.visibility == View.GONE) {
                                Handler().postDelayed({
                                    setTip15()
                                }, 300)

                            }
                        }
                    }

                    change_giftt_card.visibility = View.VISIBLE
                    val length = OrderHelper.currentAddedGiftCard!!.value
                    txt_gift_card.text =
                        getString(
                            R.string.ending_with_field_summary,
                            length?.substring(length.length - 4, length.length)
                        ) + " $" + String.format("%.2f", OrderHelper.currentAddedGiftCardBalance)
                }
            }
        }

        if (OrderHelper.currentAddedAccount != null) {
            val length = OrderHelper.currentAddedAccount!!.cardsuffix
            txt_credit_card.text =
                getString(
                    R.string.ending_with_field_summary,
                    length
                )
            change_credit_card.visibility = View.VISIBLE
            card_image.visibility = View.VISIBLE
            when {
                OrderHelper.currentAddedAccount!!.selectedcardtype == 0 -> {
                    card_image.setImageResource(
                        R.drawable.card_amex
                    )

                    txt_credit_card.text =
                        getString(
                            R.string.ending_with_field_summary,
                            length
                        ).replace("-", "")
                }
                OrderHelper.currentAddedAccount!!.selectedcardtype == 3 -> card_image.setImageResource(
                    R.drawable.card_doscover
                )
                OrderHelper.currentAddedAccount!!.selectedcardtype == 6 -> card_image.setImageResource(
                    R.drawable.card_mastercard
                )
                OrderHelper.currentAddedAccount!!.selectedcardtype == 7 -> card_image.setImageResource(
                    R.drawable.card_visa
                )
            }

        }

        if (!giftCard) {
            if (OrderHelper.fromSummary) {
                if (OrderHelper.currentAddedAccount != null) {
                    val length = OrderHelper.currentAddedAccount!!.cardsuffix
                    txt_credit_card.text =
                        getString(
                            R.string.ending_with_field_summary,
                            length
                        )
                    change_credit_card.visibility = View.VISIBLE
                    card_image.visibility = View.VISIBLE
                    when {
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 0 -> {
                            card_image.setImageResource(
                                R.drawable.card_amex
                            )

                            txt_credit_card.text =
                                getString(
                                    R.string.ending_with_field_summary,
                                    length
                                ).replace("-", "")
                        }
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 3 -> card_image.setImageResource(
                            R.drawable.card_doscover
                        )
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 6 -> card_image.setImageResource(
                            R.drawable.card_mastercard
                        )
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 7 -> card_image.setImageResource(
                            R.drawable.card_visa
                        )
                    }

                }

            } else {
                if (OrderHelper.currentAddedAccount != null) {
                    btn_place_order.isClickable = true
                    btn_place_order.isEnabled = true
                    btn_place_order.alpha = 1F

                    val length = OrderHelper.currentAddedAccount!!.cardsuffix
                    txt_credit_card.text =
                        getString(
                            R.string.ending_with_field_summary,
                            length
                        )

                    billingMethod = getString(R.string.type_credit_card)
                    cbRestaurant.setImageResource(R.drawable.radio_check_off)
                    cbGiftCard.setImageResource(R.drawable.radio_check_off)
                    cbCreditCard.setImageResource(R.drawable.ic_check_circle)
                    if (OrderHelper.restaurantDetails!!.supportstip) {
                        if (OrderHelper.basket!!.allowstip) {
                            tip_section.visibility = View.VISIBLE
                            if (tip_layout.visibility == View.GONE)
                                setTip15()
                        }

                    }
                    change_credit_card.visibility = View.VISIBLE
                    card_image.visibility = View.VISIBLE
                    when {
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 0 -> {
                            card_image.setImageResource(
                                R.drawable.card_amex
                            )
                            txt_credit_card.text =
                                getString(
                                    R.string.ending_with_field_summary,
                                    length
                                ).replace("-", "")
                        }
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 3 -> card_image.setImageResource(
                            R.drawable.card_doscover
                        )
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 6 -> card_image.setImageResource(
                            R.drawable.card_mastercard
                        )
                        OrderHelper.currentAddedAccount!!.selectedcardtype == 7 -> card_image.setImageResource(
                            R.drawable.card_visa
                        )
                    }
                }
            }
        }

        Engine().hideSoftKeyboard(homeActivity)

        if (OrderHelper.basket!!.customerhandoffcharge > 0.0) {
            delivery_fee_layout.visibility = View.VISIBLE
            txt_delivery_fee.text =
                "$" + String.format("%.2f", OrderHelper.basket!!.customerhandoffcharge)
        } else {
            delivery_fee_layout.visibility = View.GONE
        }

    }

    private fun setTextWatcherForAmountEditViewDiscount(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

                if (s.isNotEmpty()) {
                    btn_apply.isClickable = true
                    btn_apply.isEnabled = true
                    btn_apply.alpha = 1F
                } else {
                    btn_apply.isClickable = false
                    btn_apply.isEnabled = false
                    btn_apply.alpha = 0.5F
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    if (s.length == 1 && s.toString() == ".") {

                    } else {
                        val tip = s.toString().toDouble()
                        if (Engine().isNetworkAvailable(homeActivity)) {
                            presenter.applyTipToBasket(
                                homeActivity,
                                OrderHelper.basketId!!,
                                tip
                            )
                        } else {
                            Engine().showMsgDialog(
                                "",
                                getString(R.string.error_network_connection),
                                homeActivity
                            )
                        }
                    }
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                if (flagOther)
                    setTipOther()
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 0) {

                    if (OrderHelper.restaurantDetails!!.supportstip)
                        if (flagOther) {
                            presenter.applyTipToBasket(
                                homeActivity,
                                OrderHelper.basketId!!,
                                0.toDouble()
                            )

                            Handler().postDelayed({
                                tip_layout.visibility = View.VISIBLE
                                txt_tip.text = "$" + String.format("%.2f", 0.00)
                            }, 400)
                        }
                }
            }
        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    fun initView() {
        txt_subtotal.text = "$" + String.format("%.2f", OrderHelper.basket!!.subtotal)
        txt_tax.text = "$" + String.format("%.2f", OrderHelper.basket!!.salestax)

        val total = OrderHelper.basket!!.total
        txt_total.text = "$" + String.format("%.2f", total)
        order_price.text = "$" + String.format("%.2f", total)
        if (OrderHelper.restaurantDetails != null)
            if (!OrderHelper.restaurantDetails!!.supportstip) {
                tip_section.visibility = View.GONE
            }

        if (OrderHelper.restaurantDetails != null) {
            if (!OrderHelper.restaurantDetails!!.supportscoupons) {
                layout_discount.visibility = View.GONE
            } else {
                layout_discount.visibility = View.VISIBLE
            }
        }

        if (OrderHelper.basket!!.tip != null) {
            if (OrderHelper.basket!!.tip!! > 0) {
                tip_layout.visibility = View.VISIBLE
                txt_tip.text = "$" + String.format("%.2f", OrderHelper.basket!!.tip!!)
            } else {
                tip_layout.visibility = View.GONE
            }
        }
        if (OrderHelper.basket!!.discounts != null) {
            disounts.removeAllViews()
            if (OrderHelper.basket!!.discounts!!.isNotEmpty())
                for (item in OrderHelper.basket!!.discounts!!) {
                    val cellViewCustomizeLayout = LayoutInflater.from(homeActivity).inflate(
                        R.layout.discount_item,
                        null
                    ) as RelativeLayout

                    cellViewCustomizeLayout.discount_name.text = item.description
                    if (item.amount > 0.0) {
                        cellViewCustomizeLayout.txt_discount_price.visibility = View.VISIBLE
                        cellViewCustomizeLayout.txt_discount_price.text =
                            "-$" + String.format("%.2f", item.amount)
                    }
                    disounts.addView(cellViewCustomizeLayout)
                }
        }
    }


    override fun onSuccessGetBillingSchemes(response: OLOgetBillingSchemesResponse) {
        homeActivity.presenter.dismissProgress()

        try {
            for (item in response.billingschemes) {
                when {
                    item.type == getString(R.string.type_credit_card) -> {
                        layout_credit_card.visibility = View.VISIBLE
                        line_credit.visibility = View.VISIBLE
                        creditcardBillingSchemes = item
                        OrderHelper.saveCreditOnFile = item.supportssaveonfile
                    }
                    item.type == getString(R.string.type_giftcard) -> {
                        layout_gift.visibility = View.VISIBLE
                        line_gift.visibility = View.VISIBLE
                        giftcardBillingSchemes = item
                        OrderHelper.giftcardBillingSchemes = item
                    }
                    item.type == getString(R.string.type_payinstore) -> layout_resraunt.visibility =
                        View.VISIBLE
                }
            }
        } catch (er: IllegalStateException) {
        }
    }

    fun deleteCoupon() {
        if (OrderHelper.basket!!.coupon != null) {
            flagDeleteCoupon = true
            if (Engine().isNetworkAvailable(homeActivity)) {
                homeActivity.presenter.showProgress()
                presenter.deleteCouponFromBasket(homeActivity, OrderHelper.basketId!!)
            } else {
                Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }
        } else {
            flagDeleteCoupon = false
            homeActivity.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnOtherText -> {
                flagOther = true
                softKeyTrigger.setText("")
                if (OrderHelper.restaurantDetails!!.supportstip)
                    presenter.applyTipToBasket(
                        homeActivity,
                        OrderHelper.basketId!!,
                        0.toDouble()
                    )
                setTipOther()
                softKeyTrigger.requestFocus()
                val inputMethodManager: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInputFromWindow(
                    requireActivity().currentFocus!!.windowToken,
                    InputMethodManager.SHOW_FORCED,
                    0
                )


                Handler().postDelayed({
                    tip_layout.visibility = View.VISIBLE
                    txt_tip.text = "$" + String.format("%.2f", 0.00)
                }, 400)

            }
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (OrderHelper.basket!!.coupon != null) {
                    deleteCoupon()
                } else {
                    flagDeleteCoupon = false
                    homeActivity.onBackPressed()
                }
            }
            R.id.txt_save_to_favorite -> {
                openFavoriteOrderDialog()
            }

            R.id.btn_home -> {
                if (OrderHelper.basket!!.coupon != null) {
                    flagBtnHomeRemoveCoupon = true
                    deleteCoupon()
                } else {
                    if (OrderHelper.fromHome)
                        homeActivity.clearStackOnlineOrderAndLocations()
                    else
                        homeActivity.clearStackOnlineOrder()
                }
            }

            R.id.btn15 -> {
                homeActivity.presenter.showProgress()
                setTip15()
            }

            R.id.btn20 -> {
                setTip20()
            }
            R.id.btn18 -> {
                setTip18()
            }
            R.id.btn_remove_coupon -> {
                flagDeleteCoupon = false
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.deleteCouponFromBasket(homeActivity, OrderHelper.basketId!!)
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_apply -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    if (etDiscountCode.text.toString().isNotEmpty()) {
                        homeActivity.presenter.showProgress()
                        presenter.applyCouponToBasket(
                            homeActivity,
                            OrderHelper.basketId!!,
                            etDiscountCode.text.toString()
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

            R.id.layout_resraunt -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    cbRestaurant.setImageResource(R.drawable.ic_check_circle)
                    cbGiftCard.setImageResource(R.drawable.radio_check_off)
                    cbCreditCard.setImageResource(R.drawable.radio_check_off)
                    billingMethod = getString(R.string.type_cash)
                    tip_section.visibility = View.GONE
                    btn_place_order.isClickable = true
                    btn_place_order.isEnabled = true
                    btn_place_order.alpha = 1F


                    if (OrderHelper.restaurantDetails!!.supportstip) {
                        homeActivity.presenter.showProgress()
                        presenter.applyTipToBasket(
                            homeActivity,
                            OrderHelper.basketId!!,
                            0.toDouble()
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

            R.id.change_giftt_card -> {
                giftCard = true
                flagOther = false
                OrderHelper.fromSummary = false
                cbRestaurant.setImageResource(R.drawable.radio_check_off)
                cbGiftCard.setImageResource(R.drawable.ic_check_circle)
                cbCreditCard.setImageResource(R.drawable.radio_check_off)
                tip_section.visibility = View.VISIBLE

                if (giftcardBillingSchemes != null) {
                    if (giftcardBillingSchemes!!.accounts != null) {
                        if (giftcardBillingSchemes!!.accounts!!.isNotEmpty()) {
                            homeActivity.presenter.openFragmentRightNew(
                                ChooseGiftCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        } else {
                            homeActivity.presenter.openFragmentRightNew(
                                AddNewGiftCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        }
                    } else {
                        homeActivity.presenter.openFragmentRightNew(
                            AddNewGiftCardFragment(),
                            AppConstants.TAG_ADD_CREDIT_CARD
                        )
                    }
                }

            }

            R.id.layout_gift -> {
                giftCard = true
                flagOther = false
                OrderHelper.fromSummary = false
                cbRestaurant.setImageResource(R.drawable.radio_check_off)
                cbGiftCard.setImageResource(R.drawable.ic_check_circle)
                cbCreditCard.setImageResource(R.drawable.radio_check_off)

                if (txt_gift_card.text == getString(R.string.pay_gift_card)) {
                    if (giftcardBillingSchemes != null) {
                        if (giftcardBillingSchemes!!.accounts != null) {
                            if (giftcardBillingSchemes!!.accounts!!.isNotEmpty()) {
                                homeActivity.presenter.openFragmentRightNew(
                                    ChooseGiftCardFragment(),
                                    AppConstants.TAG_ADD_CREDIT_CARD
                                )
                            } else {
                                homeActivity.presenter.openFragmentRightNew(
                                    AddNewGiftCardFragment(),
                                    AppConstants.TAG_ADD_CREDIT_CARD
                                )
                            }
                        } else {
                            homeActivity.presenter.openFragmentRightNew(
                                AddNewGiftCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        }
                    }
                } else {
                    btn_place_order.isClickable = true
                    btn_place_order.isEnabled = true
                    btn_place_order.alpha = 1F
                    if (OrderHelper.restaurantDetails!!.supportstip)
                        if (OrderHelper.basket!!.allowstip) {
                            tip_section.visibility = View.VISIBLE
                            setTip15()
                        }
                    billingMethod = getString(R.string.type_stored_value)
                }
            }

            R.id.change_credit_card -> {
                giftCard = false
                flagOther = false
                OrderHelper.fromSummary = false
                cbRestaurant.setImageResource(R.drawable.radio_check_off)
                cbGiftCard.setImageResource(R.drawable.radio_check_off)
                cbCreditCard.setImageResource(R.drawable.ic_check_circle)

                if (creditcardBillingSchemes != null) {
                    if (creditcardBillingSchemes!!.accounts != null) {
                        if (creditcardBillingSchemes!!.accounts!!.isNotEmpty()) {
                            homeActivity.presenter.openFragmentRightNew(
                                ChooseCreditCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        } else {
                            homeActivity.presenter.openFragmentRightNew(
                                AddNewCreditCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        }
                    } else {
                        homeActivity.presenter.openFragmentRightNew(
                            AddNewCreditCardFragment(),
                            AppConstants.TAG_ADD_CREDIT_CARD
                        )
                    }
                }
            }

            R.id.layout_credit_card -> {
                giftCard = false
                flagOther = false
                OrderHelper.fromSummary = false
                cbRestaurant.setImageResource(R.drawable.radio_check_off)
                cbGiftCard.setImageResource(R.drawable.radio_check_off)
                cbCreditCard.setImageResource(R.drawable.ic_check_circle)

                if (card_image.visibility == View.GONE) {
                    if (creditcardBillingSchemes != null) {
                        if (creditcardBillingSchemes!!.accounts != null) {
                            if (creditcardBillingSchemes!!.accounts!!.isNotEmpty()) {
                                homeActivity.presenter.openFragmentRightNew(
                                    ChooseCreditCardFragment(),
                                    AppConstants.TAG_ADD_CREDIT_CARD
                                )
                            } else {
                                homeActivity.presenter.openFragmentRightNew(
                                    AddNewCreditCardFragment(),
                                    AppConstants.TAG_ADD_CREDIT_CARD
                                )
                            }
                        } else {
                            homeActivity.presenter.openFragmentRightNew(
                                AddNewCreditCardFragment(),
                                AppConstants.TAG_ADD_CREDIT_CARD
                            )
                        }
                    }
                } else {
                    btn_place_order.isClickable = true
                    btn_place_order.isEnabled = true
                    btn_place_order.alpha = 1F
                    billingMethod = getString(R.string.type_credit_card)
                    if (OrderHelper.restaurantDetails!!.supportstip)
                        if (OrderHelper.basket!!.allowstip) {
                            tip_section.visibility = View.VISIBLE
                            setTip15()
                        }
                }
            }

            R.id.btn_place_order -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()

                    when (billingMethod) {
                        getString(R.string.type_credit_card) -> //pay with creditcard
                            if (OrderHelper.currentAddedAccount!!.accountid != null) {
                                presenter.submitBasket(
                                    homeActivity,
                                    null,
                                    Engine().getOloAuthToken(mySharedPreferences)!!,
                                    getString(R.string.type_billingaccount),
                                    Engine().getUserEmail(mySharedPreferences)!!,
                                    Engine().getUserFirstName(mySharedPreferences)!!,
                                    Engine().getUserLastName(mySharedPreferences)!!,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    OrderHelper.currentAddedAccount!!.accountid.toString(),
                                    Engine().getUserMobilePhone(mySharedPreferences)!!,
                                    OrderHelper.basketId!!,
                                    OrderHelper.saveOnFile
                                )
                            } else {
                                presenter.submitBasket(
                                    homeActivity,
                                    null,
                                    Engine().getOloAuthToken(mySharedPreferences)!!,
                                    billingMethod,
                                    Engine().getUserEmail(mySharedPreferences)!!,
                                    Engine().getUserFirstName(mySharedPreferences)!!,
                                    Engine().getUserLastName(mySharedPreferences)!!,
                                    OrderHelper.currentAddedAccount!!.cardnumber!!,
                                    OrderHelper.currentAddedAccount!!.securityCode!!.toString(),
                                    OrderHelper.currentAddedAccount!!.zipCode!!.toString(),
                                    OrderHelper.currentAddedAccount!!.expiration!!.split("-")[0].toInt(),
                                    OrderHelper.currentAddedAccount!!.expiration!!.split("-")[1].toInt(),
                                    null, null,
                                    Engine().getUserMobilePhone(mySharedPreferences)!!,
                                    OrderHelper.basketId!!,
                                    OrderHelper.saveOnFile
                                )
                            }
                        getString(R.string.type_cash) -> //pay in store
                            presenter.submitBasket(
                                homeActivity,
                                null,
                                Engine().getOloAuthToken(mySharedPreferences)!!,
                                billingMethod,
                                Engine().getUserEmail(mySharedPreferences)!!,
                                Engine().getUserFirstName(mySharedPreferences)!!,
                                Engine().getUserLastName(mySharedPreferences)!!,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null, null,
                                Engine().getUserMobilePhone(mySharedPreferences)!!,
                                OrderHelper.basketId!!
                            )
                        getString(R.string.type_stored_value) -> //pay with giftcard

                            if (OrderHelper.giftCardAccountId != 0L) {
                                presenter.submitBasket(
                                    homeActivity,
                                    null,
                                    Engine().getOloAuthToken(mySharedPreferences)!!,
                                    getString(R.string.type_billingaccount),
                                    Engine().getUserEmail(mySharedPreferences)!!,
                                    Engine().getUserFirstName(mySharedPreferences)!!,
                                    Engine().getUserLastName(mySharedPreferences)!!,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    OrderHelper.currentAddedGiftCard,
                                    OrderHelper.giftCardAccountId.toString(),
                                    Engine().getUserMobilePhone(mySharedPreferences)!!,
                                    OrderHelper.basketId!!
                                )
                            } else {
                                presenter.submitBasket(
                                    homeActivity,
                                    null,
                                    Engine().getOloAuthToken(mySharedPreferences)!!,
                                    billingMethod,
                                    Engine().getUserEmail(mySharedPreferences)!!,
                                    Engine().getUserFirstName(mySharedPreferences)!!,
                                    Engine().getUserLastName(mySharedPreferences)!!,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    OrderHelper.currentAddedGiftCard,
                                    OrderHelper.giftcardBillingSchemes!!.id.toString(),
                                    Engine().getUserMobilePhone(mySharedPreferences)!!,
                                    OrderHelper.basketId!!
                                )
                            }
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
    }

    override fun onSuccessAddFavorite(response: OLOUserFavoriteBasketsResponse) {
        homeActivity.presenter.dismissProgress()
    }

    override fun onSuccessRemoveCoupon(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        if (flagBtnHomeRemoveCoupon) {
            if (OrderHelper.fromHome)
                homeActivity.clearStackOnlineOrderAndLocations()
            else
                homeActivity.clearStackOnlineOrder()
        } else {
            if (!flagDeleteCoupon) {
                initView()
                etDiscountCode.setText("")
                etDiscountCode.isEnabled = true
                etDiscountCode.isClickable = true
                btn_apply.visibility = View.VISIBLE
                btn_remove_coupon.visibility = View.GONE
            } else {
                flagDeleteCoupon = false
                homeActivity.onBackPressed()
            }
        }
    }

    override fun onSuccessApplyCoupon(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        initView()
        if (response.coupondiscount > 0) {
            btn_apply.visibility = View.GONE
            btn_remove_coupon.visibility = View.VISIBLE
            etDiscountCode.isEnabled = false
            etDiscountCode.isClickable = false
        }
    }

    override fun onSuccessApplyTip(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        initView()
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

}

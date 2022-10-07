package com.tts.gueststar.fragments.managepayment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import com.tts.nsrsdkrelevant.cloudconnect.models.CloudConnectResponse
import com.tts.nsrsdkrelevant.cloudconnect.models.Payment_
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.managepayment.ManagePaymentPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.CardType
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_manage_payment.*
import ncruser.models.NCRInsertCreditCardRequest
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@Suppress("DEPRECATION")
class ManagePaymentFragment : BaseFragment(), ManagePaymentPresenter.ManagePaymentView,
    OnClickListener,
    View.OnTouchListener {
    override fun callGetUserPaymentInfo() {
        presenter.getUserPaymentInfo(
            homeActivity,
            Engine().getAuthToken(mySharedPreferences).toString()
        )
    }

    override fun onShowSuccessAdded() {
        Engine().showMsgDialogNoTitle(getString(R.string.success_added_credit_card), homeActivity)
    }

    override fun closeManagePayment() {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialogNoTitle(getString(R.string.success_remove_credit_card), homeActivity)
        homeActivity.onBackPressed()
    }

    //    private val cloudConnectValues by lazy { CloudConnectValues() }
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: ManagePaymentPresenter
    private lateinit var app: MyApplication
    private var accountId = ""
    private var showBackButton = false
    private var flag2 = false
    private var selectedPaymentType = -1
    @Inject
    lateinit var mySharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = ManagePaymentPresenter(this)
        setListeners()
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getCloudConnectSettings(homeActivity)
        } else {
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_save_credit_card -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    val request = NCRInsertCreditCardRequest(
                        et_credit_card_update.text.toString().replace("-", ""),
                        expiration_date_update.text.toString(),
                        selectedPaymentType
                    )
                    presenter.addCreditCard(
                        homeActivity, request, Engine().getStringData(
                            mySharedPreferences,
                            AppConstants.CUSTOMER_ID
                        ).toString()
                    )

                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }


            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (showBackButton) {
                    if (!flag2) {
                        showBackButton = false
                        layout_add_credit_card.visibility = View.GONE
                        layout_update_card_summary.visibility = View.GONE
                        expiration_date_update.text = getString(R.string.epmty_expire_date)
                        et_credit_card_update.setText("")

                        add_credit_card_layout.visibility = View.VISIBLE
                        title.text = getString(R.string.title_card_summary)
                        layout_card_summary.visibility = View.VISIBLE

                        btn_close.setImageResource(R.drawable.close_icon)
                    } else {
                        showBackButton = false
                        flag2 = false
                        btn_close.setImageResource(R.drawable.close_icon)
                        layout_add_credit_card.visibility = View.VISIBLE
                        layout_update_card_summary.visibility = View.GONE
                        save_credit_card.visibility = View.GONE
                    }
                } else
                    homeActivity.onBackPressed()
            }
            R.id.btn_delete_credit_card -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.deleteCreditCard(
                        homeActivity, accountId, Engine().getStringData(
                            mySharedPreferences,
                            AppConstants.CUSTOMER_ID
                        ).toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.layout_add_credit_card -> {
                et_credit_card_update.setText("")
                expiration_date_update.text = getString(R.string.epmty_expire_date)
                icon_arrow.visibility = View.VISIBLE
                icon_clear_expiration.visibility = View.GONE
                flag2 = true
                layout_add_credit_card.visibility = View.GONE
                btn_close.setImageResource(R.drawable.arrow_left_black)
                if (Build.VERSION.SDK_INT < 23) {
                    expiration_date_update.setTextAppearance(homeActivity, R.style.TextViewDarkHint)
                } else {
                    expiration_date_update.setTextAppearance(R.style.TextViewDarkHint)
                }
                showBackButton = true
                initializeAddCreditCard()
            }

            R.id.btn_update_credit_card -> {
                flag2 = false
                icon_arrow.visibility = View.VISIBLE
                icon_clear_expiration.visibility = View.GONE
                btn_close.setImageResource(R.drawable.arrow_left_black)
                showBackButton = true
                initializeAddCreditCard()
            }

            R.id.btn_expiration_date_update -> {
                initDatePicker()
            }

            R.id.icon_clear_expiration -> {
                if (Build.VERSION.SDK_INT < 23) {
                    expiration_date_update.setTextAppearance(homeActivity, R.style.TextViewDarkHint)
                } else {
                    expiration_date_update.setTextAppearance(R.style.TextViewDarkHint)
                }
                expiration_date_update.text = getString(R.string.epmty_expire_date)
                icon_arrow.visibility = View.VISIBLE
                icon_clear_expiration.visibility = View.GONE
            }

        }
    }

//    fun close() {
//        if (showBackButton) {
//            if (!flag2) {
//                showBackButton = false
//                layout_add_credit_card.visibility = View.GONE
//                layout_update_card_summary.visibility = View.GONE
//                expiration_date_update.text = getString(R.string.epmty_expire_date)
//                et_credit_card_update.setText("")
//
//                add_credit_card_layout.visibility = View.VISIBLE
//                title.text = getString(R.string.title_card_summary)
//                layout_card_summary.visibility = View.VISIBLE
//
//                btn_close.setImageResource(R.drawable.close_icon)
//            } else {
//                showBackButton = false
//                flag2 = false
//                btn_close.setImageResource(R.drawable.close_icon)
//                layout_add_credit_card.visibility = View.VISIBLE
//                layout_update_card_summary.visibility = View.GONE
//                save_credit_card.visibility = View.GONE
//            }
//        } else
//            homeActivity.onBackPressed()
//    }


    private fun initializeAddCreditCard() {
        title.text = getString(R.string.title_add_credit_card)
        layout_update_card_summary.visibility = View.VISIBLE
        add_credit_card_layout.visibility = View.GONE
        layout_card_summary.visibility = View.GONE
        save_credit_card.visibility = View.VISIBLE
        setTextWatcherForAmountEditView(et_credit_card_update)
        checkRequirements()
        et_credit_card_update.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {


                    val userInput = s.toString().replace("[^\\d]".toRegex(), "")
                    if (userInput.length <= 16) {
                        val sb = StringBuilder()
                        for (i in 0 until userInput.length) {
                            if (i % 4 == 0 && i > 0) {
                                sb.append("-")
                            }
                            sb.append(userInput[i])
                        }
                        current = sb.toString()

                        s?.filters = arrayOfNulls<InputFilter>(0)
                    }
                    s?.replace(0, s.length, current, 0, current.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    card_icon1.visibility = View.VISIBLE
                    card_icon1.setImageResource(R.drawable.no_card)
                }
//                    //card_icon1.visibility = View.INVISIBLE
                when {

                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.AMERICAN_EXPRESS -> {
                        card_icon1.visibility = View.VISIBLE
                        card_icon1.setImageResource(R.drawable.card_amex)
                        selectedPaymentType = 0
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.DISCOVER -> {
                        card_icon1.visibility = View.VISIBLE
                        card_icon1.setImageResource(R.drawable.card_doscover)
                        selectedPaymentType = 3
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.MASTERCARD -> {
                        card_icon1.visibility = View.VISIBLE
                        card_icon1.setImageResource(R.drawable.card_mastercard)
                        selectedPaymentType = 6
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.VISA -> {
                        card_icon1.visibility = View.VISIBLE
                        card_icon1.setImageResource(R.drawable.card_visa)
                        selectedPaymentType = 7
                    }
                }
            }
        })
    }


    private fun setListeners() {
        layout_add_credit_card.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        btn_delete_credit_card.setOnClickListener(this)
        btn_update_credit_card.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
        btn_expiration_date_update.setOnClickListener(this)
        btn_save_credit_card.setOnClickListener(this)
        icon_clear_expiration.setOnClickListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkRequirements()
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
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

    private fun getFragment(fragment: BaseFragment, isFromBottomNavigation: Boolean): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, isFromBottomNavigation)
        fragment.arguments = bundle
        return fragment
    }

    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_MANAGE_PAYMENT
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment(),
                false
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    private fun initDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 2

        val dialog = Dialog(homeActivity)

        dialog.setTitle(getString(R.string.date_picker_title))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_date_picker)

        val monthPicker = dialog.findViewById(R.id.monthPicker) as NumberPicker
        val yearPicker = dialog.findViewById(R.id.yearPicker) as NumberPicker

        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
        val btnSet = dialog.findViewById(R.id.btnSetExpDate) as Button

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        if (month <= 12) {
            monthPicker.value = month
        } else {
            monthPicker.value = 12
        }

        yearPicker.minValue = year
        yearPicker.maxValue = year + 50
        yearPicker.wrapSelectorWheel = false

        monthPicker.setOnValueChangedListener { _, _, _ ->
            if (yearPicker.value == year) {
                if (monthPicker.value < month) {
                    if (month <= 12) {
                        monthPicker.value = month
                    } else {
                        monthPicker.value = 12
                    }
                }
            }
        }

        yearPicker.setOnValueChangedListener { _, _, _ ->
            if (yearPicker.value == year) {
                if (monthPicker.value < month) {
                    if (month <= 12) {
                        monthPicker.value = month
                    } else {
                        monthPicker.value = 12
                    }
                }
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSet.setOnClickListener {
            val chosenMonth = monthPicker.value
            val chosenYear = yearPicker.value
            if (chosenMonth < 10)
                expiration_date_update.text =
                    getString(
                        R.string.exp_date_field_value,
                        chosenMonth.toString(),
                        chosenYear.toString()
                    )
            else expiration_date_update.text =
                getString(
                    R.string.exp_date_field_value_nonzero,
                    chosenMonth.toString(),
                    chosenYear.toString()
                )

            checkRequirements()
            icon_arrow.visibility = View.GONE
            icon_clear_expiration.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT < 23) {
                expiration_date_update.setTextAppearance(homeActivity, R.style.TextViewDark)
            } else {
                expiration_date_update.setTextAppearance(R.style.TextViewDark)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkRequirements() {
        if ((et_credit_card_update.text?.length == 18 || et_credit_card_update.text?.length == 19) && expiration_date_update.text.length == 7 && expiration_date_update.text != getString(
                R.string.epmty_expire_date
            )
        ) {
            Engine().setEnableButton(btn_save_credit_card, true)
        } else {
            Engine().setEnableButton(btn_save_credit_card, false)
        }
    }

    override fun saveCloudConnectSettings(response: CloudConnectResponse) {
        Engine().setSelectedCloudConnectSettings(response)
        //    cloudConnectValues.setCloudData(response)
        presenter.getUserPaymentInfo(
            homeActivity,
            Engine().getAuthToken(mySharedPreferences).toString()
        )
    }


    override fun showCardSummary(payments: List<Payment_>) {
        btn_close.setImageResource(R.drawable.close_icon)
        showBackButton = false
        flag2 = false
        homeActivity.presenter.dismissProgress()
        accountId = payments[0].AccountId
        layout_add_credit_card.visibility = View.GONE
        layout_update_card_summary.visibility = View.GONE
        expiration_date_update.text = getString(R.string.epmty_expire_date)
        et_credit_card_update.setText("")

        add_credit_card_layout.visibility = View.VISIBLE
        title.text = getString(R.string.title_card_summary)
        layout_card_summary.visibility = View.VISIBLE
        val length = payments[0].MaskedAccountNumber.length
        et_credit_card.text =
            getString(
                R.string.ending_with_field,
                payments[0].MaskedAccountNumber.subSequence(length - 4, length)
            )


        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val newFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val date: Date = format.parse(payments[0].ExpirationDate)
        val result: String = newFormat.format(date)

        expiration_date.text = getString(R.string.exp_date_field, result)

        when {
            payments[0].MethodType == 0 -> card_icon.setImageResource(R.drawable.card_amex)
            payments[0].MethodType == 7 -> card_icon.setImageResource(R.drawable.card_visa)
            payments[0].MethodType == 6 -> card_icon.setImageResource(R.drawable.card_mastercard)
            payments[0].MethodType == 3 -> card_icon.setImageResource(R.drawable.card_doscover)
        }

    }

    override fun onShowAddPayment() {
        homeActivity.presenter.dismissProgress()
        layout_add_credit_card.visibility = View.VISIBLE
        title.text = getString(R.string.title_add_credit_card)
    }

    override fun disableButton() {
        //Engine().setEnableButton(btn_submit_promo, false)
    }

    override fun enableButton() {
        //  Engine().setEnableButton(btn_submit_promo, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        this.clearFindViewByIdCache()
    }
}
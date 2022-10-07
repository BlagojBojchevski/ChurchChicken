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
import com.tts.gueststar.ui.onlineorder.AddGiftCardPresenter
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.Billingfield
import com.tts.olosdk.models.OLOBillingSchemeBalanceResponse
import kotlinx.android.synthetic.main.fragment_add_new_gift_card.*
import javax.inject.Inject

@Suppress("DEPRECATION")
class AddNewGiftCardFragment : BaseFragment(), View.OnClickListener,
    AddGiftCardPresenter.AddGiftCardView {
    override fun onSuccessRetriveBalance(response: OLOBillingSchemeBalanceResponse) {
        homeActivity.presenter.dismissProgress()
        if (response.success) {
            OrderHelper.currentAddedGiftCardBalance = response.balance
            val giftCard = Billingfield(value = etSignUpZip.text.toString())
            OrderHelper.currentAddedGiftCard = giftCard
            OrderHelper.giftCardAccountId = 0L
            homeActivity.clearStackAddCreditCard()
        } else {
            Engine().showMsgDialog(getString(R.string.app_name), response.message, homeActivity)
        }
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_save_gift_card, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_save_gift_card, true)
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

    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: AddGiftCardPresenter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_new_gift_card, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = AddGiftCardPresenter(this)
        Engine().setEnableButton(btn_save_gift_card, false)
        btn_close.setOnClickListener(this)
        btn_save_gift_card.setOnClickListener(this)

        setTextWatcherForAmountEditView(etSignUpZip)
    }


    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)

        btn_close.setImageResource(R.drawable.arrow_left_black)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.clearStackAddCreditCard()
            }
            R.id.btn_save_gift_card -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.retrieveBalance(
                        homeActivity,
                        OrderHelper.giftcardBillingSchemes!!.id,
                        etSignUpZip.text.toString(), OrderHelper.basketId!!
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


    private fun checkRequirements() {
        if (etSignUpZip.text?.length == 14) {
            Engine().setEnableButton(btn_save_gift_card, true)
        } else {
            Engine().setEnableButton(btn_save_gift_card, false)
        }
    }

}

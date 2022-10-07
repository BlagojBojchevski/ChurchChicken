package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import app.com.relevantsdk.sdk.models.SubmitPromoCodeResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.promocode.PromoCodePresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_promo_code.*
import javax.inject.Inject

class PromoCodeFragment : BaseFragment(), PromoCodePresenter.PromoCodeView, OnClickListener,
    View.OnTouchListener {
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: PromoCodePresenter
    private lateinit var app: MyApplication
    private lateinit var mHandler: Handler
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
        return inflater.inflate(R.layout.fragment_promo_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = PromoCodePresenter(this)
        mHandler = Handler()

        txt_contact_us.underline()
        setListeners()
        setTextWatcherForAmountEditView(etPromoCode)
        Engine().setEnableButton(btn_submit_promo, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.btn_submit_promo -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.submitPromoCode(
                        homeActivity,
                        etPromoCode.text.toString(),
                        Engine().getAuthToken(mySharedPreferences).toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }
            R.id.txt_contact_us -> {
                val uriText =
                    "mailto:" + getString(R.string.EMAILCONTACT_US) + "?subject=" + getString(R.string.EMAILSUBJECTFAQ) + "&body=" + Engine().getDeviceName(
                        mySharedPreferences
                    )
                val uri = Uri.parse(uriText)
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = uri
                startActivity(Intent.createChooser(emailIntent, "Email"))
            }

            R.id.btn_clear_promo ->{
                etPromoCode.setText("")
                btn_clear_promo.visibility = View.GONE
            }
        }
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setListeners() {
        txt_contact_us.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        btn_submit_promo.setOnClickListener(this)
        btn_clear_promo.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (etPromoCode.text.toString().isNotEmpty()) {
                    btn_clear_promo.visibility = View.VISIBLE
                } else {
                    btn_clear_promo.visibility = View.GONE
                }

                presenter.onPromoCodeValidateion(
                    etPromoCode.text.toString()
                )
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

    override fun onSuccessSubmitPromo(response: SubmitPromoCodeResponse) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), response.message, homeActivity)
        Engine().hideSoftKeyboard(homeActivity)
        homeActivity.onBackPressed()
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_submit_promo, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_submit_promo, true)
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }
}
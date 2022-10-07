package com.tts.gueststar.fragments.userauth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import app.com.relevantsdk.sdk.models.ForgotPasswordResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.forgotpassword.ForgotPasswordPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_forgot_password.*

class ForgotPasswordFragment : BaseFragment(), ForgotPasswordPresenter.ForgotPasswordView,
    OnClickListener,
    View.OnTouchListener {
    override fun onSuccessResetPassword(response: ForgotPasswordResponse) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialogForgetPass(
            getString(R.string.app_name),
            response.message,
            homeActivity
        )
    }

    private lateinit var homeActivity: MainActivity
    lateinit var presenter: ForgotPasswordPresenter
    private lateinit var app: MyApplication
    private lateinit var mHandler: Handler

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
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = ForgotPasswordPresenter(this)
        mHandler = Handler()
        Engine.setNextPage = AppConstants.EMPTY_TAG
        setListeners()
        setTextWatcherForAmountEditView(etForgotEmail)
        Engine().setEnableButton(btn_reset_password, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.btn_reset_password -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.userForgotPassword(etForgotEmail.text.toString(), homeActivity)
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }
            R.id.btn_clear_email -> {
                btn_clear_email.visibility = View.GONE
                etForgotEmail.setText("")
            }
        }
    }


    private fun setListeners() {
        btn_clear_email.setOnClickListener(this)
        btn_back.setOnClickListener(this)
        btn_reset_password.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (etForgotEmail.text.toString().isNotEmpty()) {
                    btn_clear_email.visibility = View.VISIBLE
                } else {
                    btn_clear_email.visibility = View.GONE
                }
                presenter.emailValidation(etForgotEmail.text.toString())
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_reset_password, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_reset_password, true)
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

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }
}

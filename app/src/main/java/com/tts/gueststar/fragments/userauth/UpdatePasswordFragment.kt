package com.tts.gueststar.fragments.userauth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.*
import android.view.*
import android.view.View.OnClickListener
import android.widget.EditText
import app.com.relevantsdk.sdk.models.UpdateUserPasswordResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.updatepassword.UpdatePasswordPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_update_password.*
import javax.inject.Inject

class UpdatePasswordFragment : BaseFragment(), UpdatePasswordPresenter.UpdatePasswordView, OnClickListener,
    View.OnTouchListener {

    private lateinit var homeActivity: MainActivity
    lateinit var presenter: UpdatePasswordPresenter
    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var isVisiblePasswordOld = false
    private var isVisiblePasswordNew = false
    private var isVisiblePasswordConfirm = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return inflater.inflate(R.layout.fragment_update_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = UpdatePasswordPresenter(this)
        Engine.setNextPage = AppConstants.TAG_UPDATE_PASSWORD
        setListeners()
        setTextWatcherForAmountEditView(etOldPassword)
        setTextWatcherForAmountEditView(etNewPassword)
        setTextWatcherForAmountEditView(etConformPassword)
        Engine().setEnableButton(btn_update_password, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.btn_update_password -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.updatePassword(
                        homeActivity,
                        etOldPassword.text.toString(),
                        etNewPassword.text.toString(),
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
            R.id.btn_hide_show_old -> {
                if (isVisiblePasswordOld) {
                    isVisiblePasswordOld = false
                    btn_hide_show_old.setBackgroundResource(R.drawable.eye_on)
                    etOldPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etOldPassword.setSelection(etOldPassword.text!!.length)
                } else {
                    isVisiblePasswordOld = true
                    btn_hide_show_old.setBackgroundResource(R.drawable.eye_off)
                    etOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    etOldPassword.setSelection(etOldPassword.text!!.length)
                }
            }

            R.id.btn_hide_show_new -> {
                if (isVisiblePasswordNew) {
                    isVisiblePasswordNew = false
                    btn_hide_show_new.setBackgroundResource(R.drawable.eye_on)
                    etNewPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etNewPassword.setSelection(etNewPassword.text!!.length)
                } else {
                    isVisiblePasswordNew = true
                    btn_hide_show_new.setBackgroundResource(R.drawable.eye_off)
                    etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    etNewPassword.setSelection(etNewPassword.text!!.length)
                }
            }

            R.id.btn_hide_show_confirm -> {
                if (isVisiblePasswordConfirm) {
                    isVisiblePasswordConfirm = false
                    btn_hide_show_confirm.setBackgroundResource(R.drawable.eye_on)
                    etConformPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etConformPassword.setSelection(etConformPassword.text!!.length)
                } else {
                    isVisiblePasswordConfirm = true
                    btn_hide_show_confirm.setBackgroundResource(R.drawable.eye_off)
                    etConformPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    etConformPassword.setSelection(etConformPassword.text!!.length)
                }
            }
        }
    }


    private fun setListeners() {
        mainLayout.setOnTouchListener(this)
        btn_close.setOnClickListener(this)
        btn_update_password.setOnClickListener(this)
        btn_hide_show_new.setOnClickListener(this)
        btn_hide_show_old.setOnClickListener(this)
        btn_hide_show_confirm.setOnClickListener(this)
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter.updatePasswordalidation(
                    etOldPassword.text.toString(),
                    etNewPassword.text.toString(),
                    etConformPassword.text.toString()
                )
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_update_password, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_update_password, true)
    }

    override fun showGenericError() {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), getString(R.string.handle_blank_pop_up), homeActivity)
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
    }

    override fun onSuccessUpdatePassword(response: UpdateUserPasswordResponse?) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), response!!.message, homeActivity)
        homeActivity.onBackPressed()
    }

    override fun unauthorizedRequest(messgae: String) {
        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.dismissProgress()
        Engine.setNextPage = AppConstants.TAG_UPDATE_PASSWORD
        Engine().showMsgDialog(getString(R.string.app_name), messgae, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        homeActivity.presenter.openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
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

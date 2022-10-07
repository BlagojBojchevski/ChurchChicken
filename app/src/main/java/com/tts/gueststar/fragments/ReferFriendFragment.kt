package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.UserReferralResponse
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.referfriend.ReferFriendPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_refer_friend.*
import javax.inject.Inject
import android.content.*
import android.widget.Toast


class ReferFriendFragment : BaseFragment(), ReferFriendPresenter.ReferFriendView, OnClickListener,
    View.OnTouchListener {

    private lateinit var homeActivity: MainActivity
    lateinit var presenter: ReferFriendPresenter
    private lateinit var app: MyApplication
    private lateinit var mHandler: Handler
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var referalMessage = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getUserReferral(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString()
            )
        }else{
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
        return inflater.inflate(R.layout.fragment_refer_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = ReferFriendPresenter(this)
        mHandler = Handler()

        setListeners()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }

            R.id.btn_copy -> {
                val text = txt_referal_code.text
                val clipboardManager =
                    homeActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip:ClipData? = ClipData.newPlainText("Referal Code", text)
                clipboardManager.setPrimaryClip(clip!!)

                Toast.makeText(homeActivity, getString(R.string.code_copied), Toast.LENGTH_SHORT)
                    .show()
            }

            R.id.btn_refer -> {
                val text = txt_referal_code.text
                val clipboardManager =
                    homeActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    "Referal Code",
                    referalMessage
                )
                clipboardManager.setPrimaryClip(clip!!)

                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                if (text == null || text.isEmpty())
                    share.putExtra(Intent.EXTRA_TEXT, "")
                else
                    share.putExtra(Intent.EXTRA_TEXT, referalMessage )
                homeActivity.startActivity(Intent.createChooser(share, "Share your code"))
            }
        }
    }


    private fun setListeners() {
        btn_close.setOnClickListener(this)
        mainLayout.setOnTouchListener(this)
        btn_copy.setOnClickListener(this)
        btn_refer.setOnClickListener(this)
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

    override fun onSuccessReferel(response: UserReferralResponse) {
        homeActivity.presenter.dismissProgress()
        referalMessage = response.referral_message
        txt_referal_code.text = response.referral_code
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
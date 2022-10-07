package com.tts.gueststar.fragments.userauth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_main_sign_up.*


class MainSignUpFragment : BaseFragment(), View.OnClickListener {

    private lateinit var homeActivity: MainActivity
    private var isFromBottomNavigation = true
    private var isFromContactUs = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
    }


    override fun onResume() {
        super.onResume()
        if (isFromBottomNavigation) {
            actionbar_main_signup.visibility = View.GONE
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)
        } else {
            actionbar_main_signup.bringToFront()
            actionbar_main_signup.visibility = View.VISIBLE
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        }

        if (isFromContactUs) {
            btn_close_signup.setImageResource(R.drawable.arrow_white_left)
            actionbar_main_signup.bringToFront()
            actionbar_main_signup.visibility = View.VISIBLE
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromBottomNavigation = arguments!!.getBoolean(AppConstants.FROM_NAVIGATION)
            isFromContactUs = arguments!!.getBoolean(AppConstants.FROM_CONTACT_US)
        }
    }

    private fun setListeners() {
        btn_signup.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        btn_close_signup.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_signup -> {
                if (isFromBottomNavigation) {
                    if (Engine().getNextPage() == AppConstants.EMPTY_TAG)
                        Engine.setNextPage = AppConstants.TAG_HOME
                    homeActivity.presenter.openFragmentUp(
                        SignUpFragment(),
                        AppConstants.TAG_SIGN_UP
                    )
                } else {
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            SignUpFragment()
                        ), AppConstants.TAG_SIGN_UP
                    )
                }
            }
            R.id.btn_login -> {
                if (isFromBottomNavigation) {
                    if (Engine().getNextPage() == AppConstants.EMPTY_TAG)
                        Engine.setNextPage = AppConstants.TAG_HOME
                    homeActivity.presenter.openFragmentUp(LogInFragment(), AppConstants.TAG_SIGN_UP)
                } else {
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            LogInFragment()
                        ), AppConstants.TAG_SIGN_UP
                    )
                }
            }
            R.id.btn_close_signup -> {
                homeActivity.onBackPressed()
                //  Engine.supportFromLogin = false
            }
        }
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

}
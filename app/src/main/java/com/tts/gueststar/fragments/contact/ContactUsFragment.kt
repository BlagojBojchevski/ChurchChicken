package com.tts.gueststar.fragments.contact


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_contact_us.*


class  ContactUsFragment : BaseFragment(), View.OnClickListener, View.OnTouchListener {


    private lateinit var homeActivity: MainActivity


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
        return inflater.inflate(R.layout.fragment_contact_us, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }

    private fun setListeners() {
        btnAppSupport.setOnClickListener(this)
        btnStoreFeedback.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        main.setOnTouchListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p0?.id) {
            R.id.main -> {
                return p1?.pointerCount!! > 1
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAppSupport -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS =
                        AppConstants.TAG_APP_SUPPORT
                    if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                        homeActivity.presenter.openFragmentRight(
                            getFragment(
                                SupportLocationsFragment(),
                                true
                            ), AppConstants.TAG_SUPPORT_LOCATIONS
                        )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }

            }

            R.id.btnStoreFeedback -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS =
                        AppConstants.TAG_PROVIDE_FEEDBACK
                    if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                        homeActivity.presenter.openFragmentRight(
                            getFragment(
                                SupportLocationsFragment(),
                                false
                            ), AppConstants.TAG_SUPPORT_LOCATIONS
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
                homeActivity.onBackPressed()
            }

        }
    }

    private fun getFragment(fragment: BaseFragment, isFromAppSupport: Boolean): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_APP_SUPPORT, isFromAppSupport)
        fragment.arguments = bundle
        return fragment
    }

}

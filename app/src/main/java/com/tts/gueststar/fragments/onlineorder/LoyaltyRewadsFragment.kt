package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.RewardsLoyaltyPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OLOgetLoyaltyRewardsResponse
import com.tts.olosdk.models.OLOloyalySchemesResponse
import com.tts.olosdk.models.Reward
import com.tts.royrogers.adapters.RewardsLoyaltyAdapter
import com.tts.royrogers.interfaces.SelectedLoyaltyRewardInterface
import kotlinx.android.synthetic.main.fragment_rewards_online_order.*
import javax.inject.Inject

class LoyaltyRewadsFragment : BaseFragment(), View.OnClickListener,
    RewardsLoyaltyPresenter.RewardsLoyaltyView {
    override fun onSuccessApplyRewardToBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        homeActivity.presenter.dismissProgress()
        homeActivity.onBackPressed()
    }

    override fun onSuccessGetLoyaltySchemes(response: OLOloyalySchemesResponse) {
        if (response.schemes[0].membership != null) {
            membershipId = response.schemes[0].membership!!.id
            presenter.getLoyaltyQualifiedRewards(
                homeActivity,
                OrderHelper.basketId!!,
                membershipId, Engine().getOloAuthToken(mySharedPreferences)!!
            )
        } else {
            homeActivity.presenter.dismissProgress()
        }
    }

    override fun onSuccessGetLoyaltyRewards(response: OLOgetLoyaltyRewardsResponse) {
        homeActivity.presenter.dismissProgress()
        if (response.rewards.isEmpty()) {
            btn_save_for_later.visibility = View.INVISIBLE
            btn_save_for_later.isClickable = false
            layout_no_rewards.visibility = View.VISIBLE
            layout_rewards.visibility = View.GONE
        } else {
            try {
                btn_save_for_later.visibility = View.VISIBLE
                btn_save_for_later.isClickable = true
                layout_no_rewards.visibility = View.GONE
                layout_rewards.visibility = View.VISIBLE
            } catch (we: IllegalStateException) {
            }
        }

        val rewards = mutableListOf<Reward>()
        for (item in  response.rewards) {
            if (!item.expired)
                rewards.add(item)
        }

        val adapter = RewardsLoyaltyAdapter(
            homeActivity,
            rewards,
            object : SelectedLoyaltyRewardInterface {
                override fun onRewardSelected(
                    reward: Reward
                ) {
                    if (Engine().isNetworkAvailable(homeActivity)) {
                        list.clear()
                        list.add(reward.reference)
                        homeActivity.presenter.showProgress()
                        presenter.applyLoyaltyRewardToBasket(
                            homeActivity,
                            OrderHelper.basketId!!,
                            membershipId, list
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
        )
        rvRerwardsWallet.layoutManager =
            LinearLayoutManager(homeActivity.applicationContext)
        rvRerwardsWallet.adapter = adapter
    }

    val list = mutableListOf<String>()
    var membershipId = 0L
    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: RewardsLoyaltyPresenter

    private var isFromHome: Boolean? = null
    private var isFromSummary: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            externalPartnerId = arguments!!.getString(AppConstants.EXTERNAL_ID)!!
            isFromHome = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            isFromSummary = arguments!!.getBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rewards_online_order, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = RewardsLoyaltyPresenter(this)
        btn_save_for_later.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        btn_home.setOnClickListener(this)
    }


    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        btn_close.setImageResource(R.drawable.arrow_white_left)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)

        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getUserLoyaltySchemes(
                homeActivity,
                OrderHelper.basketId!!,
                Engine().getOloAuthToken(mySharedPreferences)!!
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }
            R.id.btn_save_for_later -> {
                homeActivity.onBackPressed()
            }
            R.id.btn_home -> {
                if (OrderHelper.fromHome)
                    homeActivity.clearStackOnlineOrderAndLocations()
                else
                    homeActivity.clearStackOnlineOrder()
            }
        }
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
package com.tts.gueststar.fragments.rewards

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.Reward
import app.com.relevantsdk.sdk.models.RewardsResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.RewardsAdapter
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.rewards.RewardsPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_rewards.*
import javax.inject.Inject

class RewardsFragment : BaseFragment(), View.OnClickListener, RewardsPresenter.RewardsView {
    private var balancePoints = 0
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private lateinit var presenter: RewardsPresenter
    private var isFromHomeFragment: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isFromHomeFragment = requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onResume() {
        super.onResume()
        if (isFromHomeFragment) {
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
            btn_close.visibility = View.VISIBLE
        } else {
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)
            btn_close.visibility = View.GONE
        }
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getRewards(
                BuildConfig.APPKEY,
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_rewards, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(1)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = RewardsPresenter(homeActivity, this)
        setListeners()
    }

    private fun setListeners() {
        btn_close.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }
        }
    }


    override fun onSuccessGetRewards(response: RewardsResponse?) {
        homeActivity.presenter.dismissProgress()
        try {
            if (response?.rewards != null) {
                if (response.rewards.isNotEmpty()) {
                    layoutRewards.visibility = View.VISIBLE
                    balancePoints = response.balance.points
                    tvPointsText.text = getString(R.string.point_balance_text)
                    val rewards = mutableListOf<Reward>()

                    for (item in response.rewards) {
                        if (!item.expired!!)
                            rewards.add(item)
                    }

                    val adapter = RewardsAdapter(
                        homeActivity,
                        rewards,
                        response.balance.points
                    )
                    rvRewards.layoutManager =
                        LinearLayoutManager(
                            homeActivity.applicationContext
                        )
                    rvRewards.adapter = adapter
                } else {
                    tvPointsText.text = getString(R.string.no_points_text)
                    layoutRewards.visibility = View.GONE
                }
            } else {
                tvPointsText.text = getString(R.string.no_points_text)
                layoutRewards.visibility = View.GONE
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_REWARDS
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
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

    override fun onDetach() {
        super.onDetach()
        presenter.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

}

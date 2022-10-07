package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import app.com.relevantsdk.sdk.models.*
import app.com.relevantsdk.sdk.utils.Utils
import com.appboy.Appboy
import com.appboy.enums.Month
import com.appboy.enums.NotificationSubscriptionType
import com.google.gson.Gson
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.BonusRewardHomeAdapter
import com.tts.gueststar.adapters.ImageSliderAdapter
import com.tts.gueststar.adapters.MilestoneRewardHomeAdapter
import com.tts.gueststar.adapters.RewardHomeAdapter
import com.tts.gueststar.databinding.FragmentHomeBinding
import com.tts.gueststar.fragments.notificationcenter.NotificationCenterFragment
import com.tts.gueststar.fragments.notificationcenter.SingleNotificationFragment
import com.tts.gueststar.fragments.onlineorder.OrderMenuFragment
import com.tts.gueststar.fragments.onlineorder.SelectOrderModeFragment
import com.tts.gueststar.fragments.rewards.RewardsFragment
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.home.HomePresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.MySharedPreferences
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.OloGetRestaurantDetailsResponse
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import javax.inject.Inject


class HomeFragment : BaseFragment(), View.OnClickListener, HomePresenter.HomeView {

    private var loyaltyPoints: Int = 0
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: HomePresenter
    private lateinit var slideinleft: Animation
    private lateinit var slideoutleft: Animation
    private lateinit var slideleftin: Animation
    private lateinit var slideinright: Animation
    private lateinit var mAdapter: RewardHomeAdapter
    private lateinit var imageAdapter: ImageSliderAdapter
    private lateinit var mAdapterBonusPlan: BonusRewardHomeAdapter
    private lateinit var mAdapterMilestone: MilestoneRewardHomeAdapter
    private val mData = ArrayList<RewardHomeModule>()
    private var lastNotification: Notification_? = null
    private var currentPage = 0
    private var showMobilePay = false
    val handler = Handler()
    val swipeTimer = Timer()
    private var selectedLocation: LocationsResponse.Location? = null
    private var alertDialogBuilder: AlertDialog.Builder? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(0)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = HomePresenter(this)
        setListeners()
        animationInitialization()
        val list1 = mutableListOf("", "")
        mData.add(
            RewardHomeModule(
                "",
                list1,
                0,
                false,
                "",
                "",
                false,
                "",
                "",
                0,
                "",
                "",
                "",
                0,
                0,
                0,
                0
            )
        )
        mAdapter = RewardHomeAdapter(homeActivity, 0)
        mAdapter.setData(mData)
        try {
            binding.coverflow.adapter = mAdapter
//             coverflow.setShouldRepeat(false)
            binding.coverflow.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            }
            binding.coverflow.setOnScrollPositionListener(object :
                FeatureCoverFlow.OnScrollPositionListener {
                override fun onScrolledToPosition(position: Int) {
                }

                override fun onScrolling() {
                    binding.scrolview.isVerticalScrollBarEnabled = false
                }
            })
        } catch (er: IndexOutOfBoundsException) {
        }

        binding.txtChangeFavLocation.underline()
        binding.txtViewAll.underline()
        binding.txtViewAllMessages.underline()
        binding.btnDirections.underline()
        binding.btnOrder.underline()
        binding.btnDelivery.underline()

    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun animationInitialization() {
        slideinleft = AnimationUtils.loadAnimation(homeActivity, R.anim.push_right_in)
        slideinright = AnimationUtils.loadAnimation(homeActivity, R.anim.push_right_out)
        slideoutleft = AnimationUtils.loadAnimation(homeActivity, R.anim.push_left_out)
        slideleftin = AnimationUtils.loadAnimation(homeActivity, R.anim.push_left_in)
    }

    private fun reorderVies(typesList: MutableList<Int>, response: HomeModulesResponse) {
        homeActivity.presenter.dismissProgress()
        if (binding.allViews != null) {
            try {
                val childcount = binding.allViews.childCount
                // create array
                val children = arrayOfNulls<View>(childcount)
                // get children of linearlayout
                for (i in 0 until childcount) {
                    children[i] = binding.allViews.getChildAt(i)
                }
                //now remove all children
                binding.allViews.removeAllViews()
                if (childcount == 6) {
                    for (item in typesList) {
                        if (item != 3) {
                            try {
                                binding.allViews.addView(children[item - 1])
                                children[item - 1]!!.visibility = View.VISIBLE
                            } catch (er: ArrayIndexOutOfBoundsException) {
                            }
                        }
                    }
                } else {
                    for (i in 0 until childcount) {
                        binding.allViews.addView(children[i])
                        children[i]!!.visibility = View.VISIBLE
                    }
                }

                for ((num, item) in response.components.withIndex()) {
                    when (item.component_type) {
                        1 -> //fill fav location data
                            fillFavoriteLocation(response.components[num].location_data)
                        2 -> {
                            var phone = ""
                            for (index in Engine().getUserMobilePhone(mySharedPreferences)!!.indices) {
                                when (index) {
                                    2 -> phone += Engine().getUserMobilePhone(mySharedPreferences)?.get(
                                        index
                                    )!! + "-"
                                    5 -> phone += Engine().getUserMobilePhone(mySharedPreferences)?.get(
                                        index
                                    )!! + "-"
                                    else -> phone += Engine().getUserMobilePhone(mySharedPreferences)
                                        ?.get(index)
                                }
                            }

                            binding.btnRefresh.text = phone
                            fillLoyaltyData(response.components[num].loyalty_data)
                        }
                        3 -> handleMobilePay(response.components[num].mobilepay_data)
                        4 -> //fill rewards data
                            when (response.components[num].rewards_data.rewards_type) {
                                1 -> fillRewardsData(
                                    response.components[num],
                                    response.components[num].rewards_data.rewards.balance.points,
                                    1
                                )
                                2 -> fillRewardsData(
                                    response.components[num],
                                    response.components[num].rewards_data.bonus_rewards.balance,
                                    2
                                )
                                3 -> fillRewardsData(
                                    response.components[num],
                                    response.components[num].rewards_data.milestone_rewards.balance.points,
                                    3
                                )
                            }
                        5 -> {
                            //fill messages data
                            if (item.notification_data.notification.notification_id == 0) {

                                binding.txtNoNotification.visibility = View.VISIBLE
                                binding.txtMessageTitle.visibility = View.GONE
                                binding.txtNoNotification.text = requireContext().getString(R.string.msg_no_notification)
                                binding.btnLearnMore.visibility = View.GONE
                            } else {
                                lastNotification = Notification_(
                                    created_at = item.notification_data.notification.created_at,
                                    notification_id = item.notification_data.notification.notification_id,
                                    notification_text = item.notification_data.notification.notification_text,
                                    expiration_date = item.notification_data.notification.expiration_date,
                                    message_title = item.notification_data.notification.message_title,
                                    message_url = item.notification_data.notification.message_url,
                                    notification_read_status = item.notification_data.notification.notification_read_status,
                                    notification_shown_status = item.notification_data.notification.notification_shown_status,
                                    type = item.notification_data.notification.type
                                )

                                if (item.notification_data.notification.notification_read_status) {
                                    binding.txtNoNotification.visibility = View.VISIBLE
                                    binding.txtMessageTitle.visibility = View.GONE
                                    binding.txtNoNotification.text =
                                        requireContext().getString(R.string.msg_no_notification)
                                    binding.notificationLayout.setOnClickListener(null)
                                    binding.btnLearnMore.visibility = View.GONE
                                } else {
                                    binding.txtNoNotification.visibility = View.GONE
                                    binding.txtMessageTitle.visibility = View.VISIBLE
                                    binding.txtMessageTitle.text =
                                        item.notification_data.notification.notification_text
                                    binding.notificationLayout.setOnClickListener(this)
                                    binding.btnLearnMore.visibility = View.VISIBLE
                                }


                            }
                            //   txt_my_message_all.text = item.notification_data.notification.notification_text

                        }
                        6 -> //fill gift card data
                        {
                        }
                    }
                }
            } catch (er: IndexOutOfBoundsException) {
            }
        } else {
            homeActivity.presenter.dismissProgress()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun fillFavoriteLocation(location_data: LocationData) {

        if (location_data.location.id == 0) {
            binding.txtNoFavLocation.visibility = View.VISIBLE
            binding.txtNoFavLocation.paintFlags =
                binding.txtNoFavLocation.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.btnDirections.visibility = View.GONE
            binding.btnCall.visibility = View.GONE
            binding.btnOrder.visibility = View.GONE
            binding.btnDelivery.visibility = View.GONE

            binding.txtNoFavLocation.setOnClickListener {
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentUp(
                        getFragmentRewards(
                            FavoriteLocationFragment()
                        ), AppConstants.TAG_FAV_LOCATION
                    )
            }
        } else {
            binding.txtFavLocationName.text = location_data.location.app_display_text
            binding.txtFavLocationAdrress.text = location_data.location.address + "\n" + requireContext().getString(
                R.string.location_state_text,
                location_data.location.city_label,
                Engine().convertState(location_data.location.state_label),
                location_data.location.zipcode
            )
            if (location_data.location.delivery_info != null) {
                if (location_data.location.delivery_info!!.isNotEmpty()) {
                    binding.btnDelivery.text = location_data.location.delivery_info!![0].name
                }
            }
        }

        binding.btnCall.setOnClickListener {
            try {
                if (location_data.location.id != 0) {
                    var urlPhoneNumber = location_data.location.phone_number
                    urlPhoneNumber = "tel:$urlPhoneNumber"

                    val intentCall = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse(urlPhoneNumber)
                    )
                    startActivity(intentCall)
                }
            } catch (activityException: Exception) {

            }
        }
        binding.btnDirections.setOnClickListener {
            if (location_data.location.id != 0) {
                try {
                    val offerLatLong =
                        (homeActivity.getGPSController()!!.latitude.toString() + "," + homeActivity.getGPSController()!!.longitude.toString() + "&daddr=" + location_data.location.latitude + "," + location_data.location.longitude)
                    val searchAddress = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=$offerLatLong")
                    )
                    startActivity(searchAddress)
                } catch (activityException: Exception) {
                }
            }
        }

        binding.btnDelivery.setOnClickListener {
            if (Engine().isNetworkAvailable(homeActivity))
                homeActivity.presenter.openFragmentRight(
                    getFragmentWebView(
                        WebViewFragment(),
                        location_data.location.delivery_info!![0].delivery_url,
                        location_data.location.delivery_info!![0].name
                    ), AppConstants.TAG_WEB_VIEW
                )
            else Engine().showMsgDialog(
                "",
                requireContext().getString(R.string.error_network_connection),
                homeActivity
            )
        }


        binding.btnOrder.setOnClickListener {
            if (location_data.location.id != 0) {
                OrderHelper.fromHome = true
                OrderHelper.fromHistory = false
                homeActivity.presenter.openFragmentUp(
                    getFragmentOrder(
                        SelectOrderModeFragment(),
                        location_data.location
                    ), AppConstants.TAG_LOCATIONS
                )
            }
        }
    }

    private fun getFragmentOrder(
        fragment: BaseFragment,
        location: LocationsResponse.Location
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        bundle.putParcelable(AppConstants.SELECTED_LOCATION, location)
        fragment.arguments = bundle
        return fragment
    }

    private fun fillRewardsData(component: Component, points: Int, type: Int) {
        when (type) {
            1 -> {
                try {
                    if (component.rewards_data.rewards.rewards.isEmpty()) {
                        binding.noRewards.visibility = View.VISIBLE
                        binding.coverflow.visibility = View.GONE
                    } else {
                        binding.noRewards.visibility = View.GONE
                        binding.coverflow.visibility = View.VISIBLE
                        val am =
                            homeActivity.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
                        if (am!!.isEnabled) {
                            val rewards = mutableListOf<RewardHomeModule>()
                            for (item in component.rewards_data.rewards.rewards) {
                                if (!item.expired!!)
                                    rewards.add(item)
                            }
                            if (rewards.isEmpty()) {
                                binding.noRewards.visibility = View.VISIBLE
                                binding.coverflow.visibility = View.GONE
                            } else {
                                if (rewards.size < 3) {
                                    val niza = mutableListOf<RewardHomeModule>()
                                    for (item in rewards)
                                        niza.add(item)

                                    for (item in rewards)
                                        niza.add(item)
                                    if (niza.isEmpty()) {
                                        binding.noRewards.visibility = View.VISIBLE
                                        binding.coverflow.visibility = View.GONE
                                    } else {
                                        mAdapter = RewardHomeAdapter(homeActivity, points)
                                        mAdapter.setData(niza as ArrayList<RewardHomeModule>?)
                                    }
                                } else if (rewards.size > 2) {
                                    val niza = mutableListOf<RewardHomeModule>()
                                    for (item in rewards) {
                                        if (niza.size < 3)
                                            niza.add(item)
                                    }
                                    if (niza.isEmpty()) {
                                        binding.noRewards.visibility = View.VISIBLE
                                        binding.coverflow.visibility = View.GONE
                                    } else {
                                        mAdapter = RewardHomeAdapter(homeActivity, points)
                                        mAdapter.setData(niza as ArrayList<RewardHomeModule>?)
                                    }
                                }
                            }

                        } else {
                            val rewards = mutableListOf<RewardHomeModule>()
                            for (item in component.rewards_data.rewards.rewards) {
                                if (!item.expired!!)
                                    rewards.add(item)
                            }
                            if (rewards.isEmpty()) {
                                binding.noRewards.visibility = View.VISIBLE
                                binding.coverflow.visibility = View.GONE
                            } else {
                                mAdapter = RewardHomeAdapter(homeActivity, points)
                                mAdapter.setData(rewards as ArrayList<RewardHomeModule>?)
                            }
                        }

                        try {
                            binding.coverflow.adapter = mAdapter
                            if (am.isEnabled)
                                binding.coverflow.setShouldRepeat(false)

                            binding.coverflow.onItemClickListener =
                                AdapterView.OnItemClickListener { _, _, _, _ ->
                                    homeActivity.presenter.openFragmentUp(
                                        getFragmentRewards(
                                            RewardsFragment()
                                        ), AppConstants.TAG_REWARDS
                                    )
                                }

                            binding.coverflow?.setOnScrollPositionListener(object :
                                FeatureCoverFlow.OnScrollPositionListener {
                                override fun onScrolledToPosition(position: Int) {
                                }

                                override fun onScrolling() =
                                    binding.scrolview.requestDisallowInterceptTouchEvent(true)
                            })
                        } catch (err: Exception) {
                        }
                    }
                } catch (er: IndexOutOfBoundsException) {
                }

            }
            2 -> {
                mAdapterBonusPlan = BonusRewardHomeAdapter(homeActivity, points)
                if (component.rewards_data.bonus_rewards.rewards.isNotEmpty()) {
                    mAdapterBonusPlan.setData((component.rewards_data.bonus_rewards.rewards as ArrayList<RewardX>?)!!)
                    binding.coverflow.adapter = mAdapterBonusPlan
                    binding.coverflow.onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, _, _ ->
                        }

                    binding.coverflow?.setOnScrollPositionListener(object :
                        FeatureCoverFlow.OnScrollPositionListener {
                        override fun onScrolledToPosition(position: Int) {
                        }

                        override fun onScrolling() {
                            binding.scrolview.requestDisallowInterceptTouchEvent(true)
                        }
                    })

                }
            }
            3 -> {
                if (component.rewards_data.milestone_rewards.rewards.isNotEmpty()) {
                    mAdapterMilestone = MilestoneRewardHomeAdapter(homeActivity, points)
                    try {
                        mAdapterMilestone.setData((component.rewards_data.milestone_rewards.rewards as ArrayList<Reward>?)!!)
                        binding.coverflow.adapter = mAdapterMilestone
                        try {
                            binding.coverflow.onItemClickListener =
                                AdapterView.OnItemClickListener { _, _, _, _ ->
                                    (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(
                                        1
                                    )
                                    homeActivity.presenter.openFragmentUp(
                                        getFragmentRewards(
                                            RewardsFragment()
                                        ), AppConstants.TAG_REWARDS
                                    )
                                }
                        } catch (et: IndexOutOfBoundsException) {
                        }

                        binding.coverflow.setOnScrollPositionListener(object :
                            FeatureCoverFlow.OnScrollPositionListener {
                            override fun onScrolledToPosition(position: Int) {
                            }

                            override fun onScrolling() {
                                binding.scrolview.requestDisallowInterceptTouchEvent(true)
                            }
                        })
                    } catch (er: IndexOutOfBoundsException) {
                    }
                }
            }
        }

    }

    private fun handleMobilePay(mobilepay_data: MobilepayData) {
        if (mobilepay_data.mobile_pay.pay_support) {
            binding.payonlayout.visibility = View.VISIBLE
            showMobilePay = true
        } else {
            showMobilePay = false
            binding.payonlayout.visibility = View.GONE
            MySharedPreferences.putBoolean(homeActivity, MySharedPreferences.keyIsPayOn, false)
        }
    }

    private fun fillLoyaltyData(loyalty_data: LoyaltyData) {
        if (showMobilePay)
            binding.payonlayout.visibility = View.VISIBLE
        else
            binding.payonlayout.visibility = View.GONE

        binding.txtBalancePoints.text = loyalty_data.loyalty.default_bp_points.toString()

        loyaltyPoints = loyalty_data.loyalty.default_bp_points
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)
        if (Engine().checkIfLogin(mySharedPreferences)) {
            if (Engine().isNetworkAvailable(homeActivity)) {
                homeActivity.presenter.showProgress()
                presenter.getHomeModules(
                    homeActivity,
                    Engine().getAuthToken(mySharedPreferences).toString()
                )
                presenter.getImages(homeActivity)
                if (!Engine().checkIfDataIsSendToBraze(mySharedPreferences)) {
                    presenter.getUserProfile(Engine().getAuthToken(mySharedPreferences).toString(),
                        homeActivity)
                }
            } else {
                Engine().showMsgDialog(
                    requireContext().getString(R.string.app_name),
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }
        } else {
            homeActivity.openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
        }
    }


    private fun setListeners() {
        binding.btnEarnPay.setOnClickListener(this)
        binding.txtPayAndEarn.setOnClickListener(this)
        binding.btnDoneEarnRewards.setOnClickListener(this)
        binding.first.setOnClickListener(this)
        binding.btnClosePayEarn.setOnClickListener(this)
        binding.txtViewAll.setOnClickListener(this)
        binding.btnRefresh.setOnClickListener(this)
        binding.txtViewAllMessages.setOnClickListener(this)
        binding.notificationLayout.setOnClickListener(this)
        binding.txtChangeFavLocation.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.txt_change_fav_location -> {
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentUp(
                        getFragmentRewards(
                            FavoriteLocationFragment()
                        ), AppConstants.TAG_FAV_LOCATION
                    )
            }
            R.id.notificationLayout -> {
                if (lastNotification != null) {
                    homeActivity.presenter.openFragmentUp(
                        getFragmentSingleNotification(
                            SingleNotificationFragment(),
                            lastNotification!!
                        ), AppConstants.TAG_ACCOUNT
                    )
                }
            }

            R.id.txt_view_all_messages -> {
                homeActivity.presenter.openFragmentUp(
                    getFragmentRewards(
                        NotificationCenterFragment()
                    ), AppConstants.TAG_ACCOUNT
                )
            }

            R.id.btn_refresh -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    if (MySharedPreferences.getBoolean(
                            homeActivity,
                            MySharedPreferences.keyIsPayOn
                        )
                    ) {
                        presenter.getUserPayCode(
                            homeActivity,
                            Engine().getAuthToken(mySharedPreferences).toString()
                        )
                    } else {
                        presenter.getUserCode(
                            homeActivity,
                            Engine().getAuthToken(mySharedPreferences).toString()
                        )
                    }
                } else {
                    Engine().showMsgDialog(
                        requireContext().getString(R.string.app_name),
                        requireContext().getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.txt_view_all -> {
                (homeActivity as SetBottomNavigationIcon).onNavigationIconChange(1)
                homeActivity.presenter.openFragmentUp(
                    getFragmentRewards(
                        RewardsFragment()
                    ), AppConstants.TAG_REWARDS
                )
            }

            R.id.btn_earn_pay -> {
                if (binding.first.visibility == View.VISIBLE) {
                    binding.second.clearAnimation()
                    binding.second.startAnimation(slideinleft)
                    binding.second.visibility = View.VISIBLE

                    binding.first.clearAnimation()
                    binding.first.startAnimation(slideinright)
                    binding.first.visibility = View.GONE
                }
            }

            R.id.btn_done_earn_rewards -> {
                binding.second.clearAnimation()
                binding.second.startAnimation(slideoutleft)
                binding.second.visibility = View.GONE

                binding.first.clearAnimation()
                binding.first.startAnimation(slideleftin)
                binding.first.visibility = View.VISIBLE
            }
        }
    }

    override fun onSuccessGetHomeModules(response: HomeModulesResponse) {
        homeActivity.presenter.dismissProgress()
        val typesList = mutableListOf<Int>()
        for (item in response.components) {
            typesList.add(item.component_type)
        }
        reorderVies(typesList, response)
    }

    override fun getSitebyIdafterCloudConnect() {
        presenter.getSiteById(homeActivity, selectedLocation!!.external_partner_id)
    }

    override fun onSuccessSiteById(response: OloGetRestaurantDetailsResponse) {
        //  homeActivity.presenter.dismissProgress()
        OrderHelper.restaurantDetails = response
        OrderHelper.advanceonly = response.advanceonly
        OrderHelper.isavailable = response.isavailable
        OrderHelper.advanceorderdays = response.advanceorderdays
        OrderHelper.showCalories = response.showcalories
        OrderHelper.supportsbaskettransfers = response.supportsbaskettransfers
        OrderHelper.supportsSpecialCaracters = response.supportsspecialinstructions
        OrderHelper.specialInstrutionsQuantity = response.specialinstructionsmaxlength
        OrderHelper.acceptsordersbeforeopening = response.acceptsordersbeforeopening
        OrderHelper.acceptsordersuntilclosing = response.acceptsordersuntilclosing

        try {
            if (response.supportsonlineordering) {
                if (response.isavailable) {
                    OrderHelper.deliveryMode = getString(R.string.mode_pickup)
                    if (OrderHelper.basket != null) {
                        if (OrderHelper.basket!!.vendorid == selectedLocation!!.external_partner_id.toInt()) {
                            presenter.addDeliveryModeToBasket(
                                homeActivity,
                                getString(R.string.mode_pickup),
                                OrderHelper.basketId!!
                            )
                        } else {
                            if (OrderHelper.supportsbaskettransfers) {
                                //transfer
                                try {
                                    if (alertDialogBuilder == null) {
                                        alertDialogBuilder = AlertDialog.Builder(context)
                                        alertDialogBuilder!!.setMessage(getString(R.string.transfer_text))
                                            .setCancelable(false).setPositiveButton(
                                                "YES"
                                            ) { dialog, _ ->
                                                presenter.transferBasket(
                                                    homeActivity,
                                                    selectedLocation!!.external_partner_id.toInt(),
                                                    OrderHelper.basketId!!
                                                )
                                                alertDialogBuilder = null
                                                dialog.cancel()
                                            }
                                            .setNegativeButton(
                                                "NO"
                                            ) { dialog, _ ->
                                                alertDialogBuilder = null
                                                presenter.createBasket(
                                                    homeActivity,
                                                    selectedLocation?.external_partner_id!!.toInt(),
                                                    Engine().getOloAuthToken(mySharedPreferences).toString()
                                                )
                                                dialog.cancel()
                                            }
                                        val alert = alertDialogBuilder!!.create()
                                        alert.setTitle("")
                                        alert.show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                presenter.createBasket(
                                    homeActivity,
                                    selectedLocation?.external_partner_id!!.toInt(),
                                    Engine().getOloAuthToken(mySharedPreferences).toString()
                                )
                            }
                        }
                    } else {
                        presenter.createBasket(
                            homeActivity,
                            selectedLocation?.external_partner_id!!.toInt(),
                            Engine().getOloAuthToken(mySharedPreferences).toString()
                        )
                    }

                } else {
                    homeActivity.presenter.dismissProgress()
                    Engine().showMsgDialogNoTitle(
                        getString(R.string.cannot_acceps_orders_error),
                        homeActivity
                    )
                }

            } else {
                homeActivity.presenter.dismissProgress()
                Engine().showMsgDialogNoTitle(
                    getString(R.string.cannot_acceps_orders_error),
                    homeActivity
                )
            }
        } catch (ex: IllegalStateException) {
            homeActivity.presenter.dismissProgress()
        }
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        OrderHelper.basketId = response.id
        presenter.addDeliveryModeToBasket(
            homeActivity,
            getString(R.string.mode_pickup),
            response.id
        )
    }

    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        OrderHelper.basket = response.basket
        OrderHelper.basketId = response.basket.id
        presenter.addDeliveryModeToBasket(
            homeActivity,
            getString(R.string.mode_pickup),
            response.basket.id
        )
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        homeActivity.presenter.openFragmentUp(
            getFragmentMenu(
                OrderMenuFragment(), selectedLocation!!.external_partner_id
            ),
            AppConstants.TAG_ONLINE_ORDER
        )

    }

    override fun successGetUserProfile(response: UserProfileResponse?) {

        val json = Gson().toJson(response)
        MySharedPreferences.putString(
            homeActivity,
            MySharedPreferences.userProfile,
            json
        )

        var birthday = AppConstants.defaultTimestamp
        if (response?.user_data?.birthday != null) {
            birthday = response.user_data.birthday!!
        }

        response?.user_data?.let {
            Engine().saveUserProfileData(
                mySharedPreferences,
                response.user_data.first_name,
                response.user_data.last_name,
                response.user_data.phone_number,
                response.user_data.favorite_location.app_display_text,
                response.user_data.favorite_location.id,
                birthday,
                response.user_data.profile_picture_url,
                response.user_data.hashed_user_id,
                response.user_data.hashed_email
            )

            when {
                response.user_data.marketing_info.marketing -> {
                    Appboy.getInstance(requireContext()).currentUser?.setEmailNotificationSubscriptionType(
                        NotificationSubscriptionType.OPTED_IN)
                }
                else -> {
                    Appboy.getInstance(requireContext()).currentUser?.setEmailNotificationSubscriptionType(
                        NotificationSubscriptionType.UNSUBSCRIBED)
                }
            }
            if (Appboy.getInstance(requireContext()).currentUser == null) sendDataToBraze()
        }


    }

    private fun sendDataToBraze() {
        Engine().saveSendFlag(mySharedPreferences, true)
        Appboy.getInstance(requireContext()).changeUser(Engine().getHashedEmail(mySharedPreferences).toString())
        Appboy.getInstance(requireContext()).currentUser?.addAlias(Engine().getUserEmail(mySharedPreferences).toString(), "EMAIL")
        Appboy.getInstance(requireContext()).currentUser?.setEmail(Engine().getUserEmail(mySharedPreferences))
        Appboy.getInstance(requireContext()).currentUser?.setFirstName(Engine().getUserFirstName(mySharedPreferences))
        Appboy.getInstance(requireContext()).currentUser?.setLastName(Engine().getUserLastName(mySharedPreferences))
        Appboy.getInstance(requireContext()).currentUser?.setPhoneNumber(Engine().getUserMobilePhone(mySharedPreferences))
        val birthday = Engine().getUserBirthday(mySharedPreferences)
        val defaultBirthday = -2208988793 //for some reason we receive default date if user have not enter date of birth
        if (birthday != null && birthday != defaultBirthday){
            val date = Utils.getDateCurrentTimeZone(birthday)
            if (date != null){
                val year = Utils.getFromDate(date, 1)
                val month = Utils.getFromDate(date, 2)
                val day = Utils.getFromDate(date, 3)
                val monthO = Month.getMonth(month)
                Appboy.getInstance(context).currentUser?.setDateOfBirth(year, monthO, day)
            }
        }
    }

    private fun getFragmentMenu(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessGetImages(response: GalleryStreamResponse) {
        homeActivity.presenter.dismissProgress()
        try {
            if (response.images.isNotEmpty()) {

                binding.defaultCarouselImage.visibility = View.GONE
                binding.viewPager.visibility = View.VISIBLE
                binding.indicator.visibility = View.VISIBLE
                val duration = response.display_duration * 1000
                val delay = response.transition_duration


                val imageList = ArrayList<String>()
                for (item in response.images) {
                    imageList.add(item.image_url)
                }

                val urls = ArrayList<String>()
                for (item in response.images) {
                    urls.add(item.image_hyperlink)
                }

                binding.viewPager.adapter = null
                imageAdapter = ImageSliderAdapter(homeActivity, imageList, urls)
                binding.viewPager.adapter = imageAdapter
                binding.viewPager.setScrollDurationFactor(delay.toDouble())
                binding.indicator.setViewPager(binding.viewPager)


                val runnable = Runnable {
                    if (imageAdapter.count == currentPage) {
                        currentPage = 0
                    } else {
                        currentPage++
                    }
                    if (binding.viewPager != null)
                        binding.viewPager.setCurrentItem(currentPage, true)
                }

                swipeTimer.schedule(object : TimerTask() {
                    override fun run() {
                        handler.postDelayed(runnable, delay * 1000.toLong())
                    }
                }, delay.toLong(), duration.toLong())

                binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                    override fun onPageSelected(position: Int) {
                        currentPage = position
                        handler.removeCallbacks(runnable)
                    }

                    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
                        try {
                            swipeTimer.cancel()
                        } catch (e: Exception) {
                        }
                        handler.postDelayed(runnable, delay * 1000.toLong())

                    }

                    override fun onPageScrollStateChanged(arg0: Int) {}
                })
            }

        } catch (er: IllegalStateException) {
        }
    }

    override fun showGenericError() {
        try {
            homeActivity.presenter.dismissProgress()
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.handle_blank_pop_up),
                homeActivity
            )
        } catch (er: IllegalStateException) {
        }
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
    }

    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_HOME
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    override fun getUserPayCode() {
        presenter.getUserPayCode(
            homeActivity,
            Engine().getAuthToken(mySharedPreferences).toString()
        )
    }

    override fun onShowAddPaymentPopup() {
        homeActivity.presenter.dismissProgress()
    }


    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentWebView(fragment: BaseFragment, url: String, title: String): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.WEB_VIEW_URL, url)
        bundle.putString(AppConstants.WEB_VIEW_TITLE, title)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentRewards(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentSingleNotification(
        fragment: BaseFragment,
        selectedN: Notification_
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putParcelable(AppConstants.SELECTED_NOTIFICATION, selectedN)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessGetUserCode(code: String) {
        homeActivity.presenter.dismissProgress()
        if (binding.first.visibility == View.VISIBLE) {
            binding.second.clearAnimation()
            binding.second.startAnimation(slideinleft)
            binding.second.visibility = View.VISIBLE

            binding.first.clearAnimation()
            binding.first.startAnimation(slideinright)
            binding.first.visibility = View.GONE
        }
    }


    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
        homeActivity.presenter.dismissProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        homeActivity.presenter.dismissProgress()
    }
}
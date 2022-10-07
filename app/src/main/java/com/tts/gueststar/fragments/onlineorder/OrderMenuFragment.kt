package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.MainMenuAdapter
import com.tts.gueststar.interfaces.ChooseMenuCategoryInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.MainMenuPresenter
import com.tts.olosdk.models.Category
import com.tts.olosdk.models.OLORestaurantMenuResponse
import javax.inject.Inject
import com.tts.gueststar.utility.*
import kotlinx.android.synthetic.main.fragment_order_menu.*

class OrderMenuFragment : BaseFragment(), View.OnClickListener,
    MainMenuPresenter.MainMenuView, SwipeController.SwipeControllerInter {
    override fun left() {
        Toast.makeText(homeActivity, "left fun", Toast.LENGTH_SHORT).show()
    }

    override fun right() {
        Toast.makeText(homeActivity, "right fun", Toast.LENGTH_SHORT).show()
    }

    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: MainMenuPresenter
    private lateinit var adapter: MainMenuAdapter
    private var isFromHome: Boolean? = null
    private var isCurbsideDelivery: Boolean? = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_menu, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            externalPartnerId = requireArguments().getString(AppConstants.EXTERNAL_ID)!!
            isFromHome = requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            isCurbsideDelivery =
                requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_CURBSIDE_DELIVERY)
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = MainMenuPresenter(this)
        btn_close.setOnClickListener(this)
        basket.setOnClickListener(this)
    }


    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        homeActivity.presenter.dismissProgress()

        if (isFromHome!!) {
            btn_close.setImageResource(R.drawable.arrow_white_left)
            btn_close.contentDescription = getString(R.string.back_button)
        }

        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getRestaurantMenu(homeActivity, externalPartnerId.toInt())
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
        if (OrderHelper.basket != null) {
            if (OrderHelper.basket!!.products != null) {
                if (OrderHelper.basket!!.products!!.isNotEmpty()) {
                    basket_number.text = OrderHelper.basket!!.products!!.size.toString()
                }
            } else {
                basket_number.text = getString(R.string.zero)
            }
        } else {
            basket_number.text = getString(R.string.zero)
        }
        address_name.text = OrderHelper.locationAddress
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }

            R.id.basket -> {
                OrderHelper.flagChechForRewards = false
                homeActivity.presenter.openFragmentRight(
                    getFragmentSumamry(
                        OrderSummaryFragment(), externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }
        }
    }

    private fun getFragmentSumamry(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
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

    override fun onSuccessGetRestaurants(response: OLORestaurantMenuResponse) {
        homeActivity.presenter.dismissProgress()
        try {
            OrderHelper.restaurantResponse = response
            rvMenuCategories.layoutManager =
                LinearLayoutManager(requireContext())

            var imagePathh = ""
            if (response.imagepath != null) {
                imagePathh = response.imagepath!!
            }
            adapter = MainMenuAdapter(
                homeActivity,
                imagePathh,
                response.categories,
                object : ChooseMenuCategoryInterface {
                    override fun chooseMenuCategory(address: Category, position: Int) {
                        homeActivity.presenter.openFragmentRight(
                            getFragmentSubMenu(
                                OrderSubMenuFragment(),
                                externalPartnerId, response, position

                            ),
                            AppConstants.TAG_ONLINE_ORDER
                        )
                    }

                })

            rvMenuCategories.adapter = adapter

        } catch (ex: IllegalStateException) {
            homeActivity.presenter.dismissProgress()
        }
    }

    private fun getFragmentSubMenu(
        fragment: BaseFragment,
        external_partner_id: String,
        response: OLORestaurantMenuResponse,
        selectedPosition: Int
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putInt(AppConstants.SELECTED_CATEGORY, selectedPosition)
        bundle.putSerializable(AppConstants.RESTAURANT_RESPONSE, response)
        fragment.arguments = bundle
        return fragment
    }


    override fun onPause() {
        super.onPause()
        homeActivity.presenter.dismissProgress()
    }

    override fun onStop() {
        super.onStop()
        homeActivity.presenter.dismissProgress()
    }
}

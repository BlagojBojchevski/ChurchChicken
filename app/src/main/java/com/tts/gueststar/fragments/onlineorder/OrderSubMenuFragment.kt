package com.tts.gueststar.fragments.onlineorder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.MenuCategoriesAdapter
import com.tts.gueststar.adapters.ProoductsAdapter
import com.tts.gueststar.interfaces.ChooseItemInterface
import com.tts.gueststar.interfaces.ChooseMenuCategorySubMenuInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.olosdk.models.Category
import javax.inject.Inject
import com.tts.gueststar.utility.*
import com.tts.olosdk.models.OLORestaurantMenuResponse
import com.tts.olosdk.models.Product
import kotlinx.android.synthetic.main.fragment_order_sub_menu.*


class OrderSubMenuFragment : BaseFragment(), View.OnClickListener {

    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private lateinit var adapter: MenuCategoriesAdapter
    private lateinit var adapterProducts: ProoductsAdapter
    private var response: OLORestaurantMenuResponse? = null
    private var selectedMenuPosition: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_sub_menu, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            selectedMenuPosition = arguments!!.getInt(AppConstants.SELECTED_CATEGORY)
            externalPartnerId = arguments!!.getString(AppConstants.EXTERNAL_ID)!!
            response =
                arguments!!.getSerializable(AppConstants.RESTAURANT_RESPONSE) as OLORestaurantMenuResponse?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)


        rvMenuCategories.layoutManager =
            LinearLayoutManager(
                homeActivity.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        var imagePathh = ""
        if(response!!.imagepath!=null){
            imagePathh =response!!.imagepath!!
        }
        adapter = MenuCategoriesAdapter(
            homeActivity,
            response!!.categories,
            object : ChooseMenuCategorySubMenuInterface {
                override fun chooseMenuCategory(address: Category, positiont: Int) {
                    adapterProducts = ProoductsAdapter(
                        homeActivity,
                        imagePathh,
                        response!!.categories[positiont].products,
                        object : ChooseItemInterface {
                            override fun chooseItemCategory(item: Product) {
                                OrderHelper.selectedOptionsIds.clear()
                                OrderHelper.selectedModifiersIds.clear()
                                OrderHelper.checkedRadioGruopWithModifiers.clear()
                                OrderHelper.checkedCheckBoxWithModifiers.clear()
                                OrderHelper.fromEdit = false
                                OrderHelper.fromEditForEdit = false
                                homeActivity.presenter.openFragmentRight(
                                    getFragmentItemDetails(
                                        ItemDetailsFragment(),
                                        externalPartnerId, item, imagePathh
                                    ),
                                    AppConstants.TAG_ONLINE_ORDER
                                )
                            }
                        }
                    )
                    rvMenuProducts.adapter = adapterProducts
                    selectedMenuPosition = positiont
                }

            }, response!!.categories[selectedMenuPosition].id
        )
        rvMenuCategories.adapter = adapter
        rvMenuCategories.scrollToPosition(selectedMenuPosition)
        rvMenuProducts.layoutManager =
            GridLayoutManager(
                homeActivity.applicationContext,
                2
            )
        adapterProducts = ProoductsAdapter(
            homeActivity,
            imagePathh,
            response!!.categories[selectedMenuPosition].products,
            object : ChooseItemInterface {
                override fun chooseItemCategory(item: Product) {
                    OrderHelper.selectedOptionsIds.clear()
                    OrderHelper.selectedModifiersIds.clear()
                    OrderHelper.checkedRadioGruopWithModifiers.clear()
                    OrderHelper.checkedCheckBoxWithModifiers.clear()
                    OrderHelper.fromEdit = false
                    OrderHelper.fromEditForEdit = false
                    homeActivity.presenter.openFragmentRight(
                        getFragmentItemDetails(
                            ItemDetailsFragment(),
                            externalPartnerId, item, imagePathh
                        ),
                        AppConstants.TAG_ONLINE_ORDER
                    )
                }

            }
        )
        rvMenuProducts.adapter = adapterProducts
        lineCenter.bringToFront()
        btn_close.setOnClickListener(this)
        btn_basket.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        address_name.text = OrderHelper.locationAddress
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
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }

            R.id.btn_basket -> {
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

    private fun getFragmentItemDetails(
        fragment: BaseFragment,
        external_partner_id: String,
        item: Product,
        imagePath: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putSerializable(AppConstants.SELECTED_ITEM, item)
        bundle.putString(AppConstants.IMAGE_PATH, imagePath)
        bundle.putBoolean(AppConstants.FROM_EDIT, false)
        fragment.arguments = bundle
        return fragment
    }
}

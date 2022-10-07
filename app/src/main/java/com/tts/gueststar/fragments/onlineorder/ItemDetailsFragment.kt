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
import com.tts.gueststar.adapters.ItemCustomizeAdapter
import com.tts.gueststar.interfaces.ButtonCheckChanges
import com.tts.gueststar.interfaces.ButtonCheckChangesSubModifiers
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.ItemDetailsPresenter
import javax.inject.Inject
import com.tts.gueststar.utility.*
import com.tts.olosdk.models.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_item_details.*
import android.text.InputFilter
import java.util.HashMap
class ItemDetailsFragment : BaseFragment(), View.OnClickListener, ButtonCheckChanges,
    ItemDetailsPresenter.ItemDetailsView, ButtonCheckChangesSubModifiers {

    private var modifiersFordelete: MutableList<Option> = mutableListOf()
    private var productQuantity = 1
    private var listAddedIds = mutableListOf<Long>()
    private var rows = mutableListOf<ItemCustomizeAdapter.ItemCustomizeView>()
    private var rowsTmp = mutableListOf<ItemCustomizeAdapter.ItemCustomizeView>()
    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private lateinit var adapter: ItemCustomizeAdapter
    private var selectedItem: Product? = null
    private var imagePath = ""
    lateinit var presenter: ItemDetailsPresenter
    private var fromEdit = false
    private var productForEdit: Product1? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            selectedItem = requireArguments().getSerializable(AppConstants.SELECTED_ITEM) as Product?
            productForEdit = requireArguments().getSerializable(AppConstants.EDIT_ITEM) as Product1?
            externalPartnerId = requireArguments().getString(AppConstants.EXTERNAL_ID)!!
            imagePath = requireArguments().getString(AppConstants.IMAGE_PATH)!!
            fromEdit = requireArguments().getBoolean(AppConstants.FROM_EDIT)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        OrderHelper.currentCalories = 0
        OrderHelper.groupQuantityModifiers = HashMap()

        presenter = ItemDetailsPresenter(this)
        homeActivity.presenter.dismissProgress()
        item_name.text = selectedItem!!.name
        item_description.text = selectedItem!!.description
        if (OrderHelper.showCalories) {
            if (selectedItem!!.basecalories != null) {
                item_calories.text = selectedItem!!.basecalories + " Calories per serving"
                OrderHelper.currentCalories = selectedItem!!.basecalories!!.toLong()
            }
        } else {
            item_calories.visibility = View.GONE
        }


        if (selectedItem!!.images != null)
            if (selectedItem!!.images!!.isNotEmpty()) {
                for (item in selectedItem!!.images!!) {
                    if (item.groupname == "mobile-app-large") {
                        Picasso.get().load(imagePath + item.filename).into(item_image)
                    }
                }
            }

        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getProductModifiers(homeActivity, selectedItem!!.id)
        } else {
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.error_network_connection),
                homeActivity
            )
        }

        val linearLayoutManager =
            LinearLayoutManager(homeActivity)
        container.layoutManager = linearLayoutManager
        btn_add_to_bag.setOnClickListener(this)
        btn_close.setOnClickListener(this)
        btn_plus.setOnClickListener(this)
        btn_minus.setOnClickListener(this)
        btn_basket.setOnClickListener(this)

        if (OrderHelper.supportsSpecialCaracters) {
            txt_special.text =
                getString(R.string.special_desc_1) + " " + OrderHelper.specialInstrutionsQuantity + " " + getString(
                    R.string.special_desc_2
                ) + "\n" + getString(R.string.special_desc)
            et_special_instructions.filters = arrayOf<InputFilter>(
                InputFilter.LengthFilter(
                    OrderHelper.specialInstrutionsQuantity
                )
            )
        } else {
            special_ins.visibility = View.GONE
        }

        OrderHelper.currentPrice = selectedItem!!.cost
        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)

        if (productForEdit != null) {
            et_special_instructions.setText(productForEdit!!.specialinstructions)
            productQuantity = productForEdit!!.quantity

            txt_quantity.text = productQuantity.toString()
            item_price.text =
                "$" + String.format("%.2f", OrderHelper.currentPrice * productQuantity)
        }
    }

    override fun onSuccessAddProductToBasketBatch(response: OLOBasketResponseBatch) {
        homeActivity.presenter.dismissProgress()

        if (response.errors.isNotEmpty()) {
            Engine().showMsgDialog(
                getString(R.string.app_name),
                response.errors.get(0).message,
                homeActivity
            )
        } else {
            OrderHelper.basket = response.basket
            homeActivity.onBackPressed()
            OrderHelper.flagChechForRewards = false
            homeActivity.presenter.openFragmentRight(
                getFragmentSummary(
                    OrderSummaryFragment(), externalPartnerId
                ),
                AppConstants.TAG_ONLINE_ORDER
            )
        }
    }


    override fun onSuccessAddProductToBasket(response: OLOBasketResponse) {
        OrderHelper.basket = response
        homeActivity.presenter.dismissProgress()
        homeActivity.onBackPressed()
        OrderHelper.flagChechForRewards = false
        homeActivity.presenter.openFragmentRight(
            getFragmentSummary(
                OrderSummaryFragment(), externalPartnerId
            ),
            AppConstants.TAG_ONLINE_ORDER
        )
    }

    private fun getFragmentSummary(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        fragment.arguments = bundle
        return fragment
    }

    @SuppressLint("SetTextI18n")
    override fun updatePrice() {
        item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
    }


    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)

        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.dismissProgress()
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

    @SuppressLint("SetTextI18n")
    private fun remove(option: Option, modifier: Modifier) {
        try {
            val iter1 = rows.iterator()
            while (iter1.hasNext()) {
                val str1 = iter1.next()
                if (str1 is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                    if (str1.parentId == modifier.id) {

                        if (option.modifiers != null)
                            for (item in option.modifiers!!) {
                                unselectModifiers(item.options)
                            }
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str1.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str1.item.id)
                        OrderHelper.quantityModifiers.put(str1.item.id, 0)
                        OrderHelper.selectedModifiersIds.remove(str1.item.id)
                        OrderHelper.groupQuantityModifiers.put(str1.mod.id, 0)
                        if (str1.item.id in OrderHelper.selectedModifiersDedault) {
                            str1.item.isdefault = true
                            OrderHelper.selectedModifiersDedault.remove(str1.item.id)
                        }
                        if (str1.item.id in OrderHelper.selectedModifiersIds) {
                            OrderHelper.currentPrice -= str1.item.cost
                            if (str1.item.basecalories != null) {
                                OrderHelper.currentCalories -= str1.item.basecalories!!.toLong()
                            }
                            item_calories.text =
                                OrderHelper.currentCalories.toString() + " Calories per serving"
                            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        }
                        OrderHelper.selectedModifiersIds.remove(modifier.id)
                        iter1.remove()
                    }

                } else if (str1 is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                    if (str1.parentId == modifier.id) {
                        if (option.modifiers != null)
                            for (item in option.modifiers!!) {
                                unselectModifiers(item.options)
                            }

                        OrderHelper.checkedRadioGruopWithModifiers.remove(str1.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str1.item.id)
                        OrderHelper.quantityModifiers.put(modifier.id, 0)
                        OrderHelper.groupQuantityModifiers.put(str1.mod.id, 0)
                        if (str1.item.id in OrderHelper.selectedModifiersIds) {
                            if (str1.item.basecalories != null) {
                                OrderHelper.currentCalories -= str1.item.basecalories!!.toLong()
                            }
                            OrderHelper.currentPrice -= str1.item.cost
                            item_calories.text =
                                OrderHelper.currentCalories.toString() + " Calories per serving"
                            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        }
                        OrderHelper.selectedModifiersIds.remove(modifier.id)
                        iter1.remove()
                    }
                } else if (str1 is ItemCustomizeAdapter.TitleContainerSubModifier) {
                    if (str1.parentId == modifier.id) {
                        if (option.modifiers != null)
                            for (item in option.modifiers!!) {
                                unselectModifiers(item.options)
                            }
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str1.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str1.item.id)
                        OrderHelper.groupQuantityModifiers.put(option.id, 0)
                        OrderHelper.quantityModifiers.put(modifier.id, 0)
                        OrderHelper.selectedModifiersIds.remove(modifier.id)
                        iter1.remove()
                    }
                }
            }

            adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
            container.adapter = adapter
            container.post {
                container.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
            }
        } catch (er: IndexOutOfBoundsException) {
        }
    }

    @SuppressLint("SetTextI18n")
    private fun unselectModifiers(modifiers: List<Option>) {
        try {
            for (item in modifiers) {
                if (item.modifiers != null) {
                    for (item1 in item.modifiers!!) {
                        //   if (item1.id in OrderHelper.selectedOptionsIds)
                        for (itm in item1.options)
                            if (itm.id in OrderHelper.selectedModifiersIds) {
                                if (itm.id in OrderHelper.selectedModifiersIds) {
                                    if (itm.basecalories != null) {
                                        OrderHelper.currentCalories -= itm.basecalories!!.toLong()
                                    }
                                    OrderHelper.currentPrice -= itm.cost
                                    OrderHelper.selectedModifiersIds.remove(itm.id)
                                    item_price.text =
                                        "$" + String.format(
                                            "%.2f",
                                            OrderHelper.currentPrice
                                        )
                                    item_calories.text =
                                        OrderHelper.currentCalories.toString() + " Calories per serving"
                                }
                                modifiersFordelete.remove(itm)
                                remove(itm, item1)
                            }
                    }

                }
            }
        } catch (er: NoSuchElementException) {
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onRadioBtnSubModifiersCheckChanges(
        group: Modifier,
        modifier: Option,
        check: Boolean
    ) {
        //   OrderHelper.fromEdit = false
        rowsTmp = mutableListOf()
        val iter = rows.iterator()
        while (iter.hasNext()) {
            val str = iter.next()
            if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    if (str.item.id in OrderHelper.selectedModifiersIds) {
                        if (str.item.basecalories != null) {
                            OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                        }
                        OrderHelper.currentPrice -= str.item.cost
                        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        item_calories.text =
                            OrderHelper.currentCalories.toString() + " Calories per serving"
                    }
                    if (str.item.id in OrderHelper.selectedModifiersDedault) {
                        str.item.isdefault = true
                        OrderHelper.selectedModifiersDedault.remove(str.item.id)
                    }
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                    OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    iter.remove()
                }
            } else if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    if (str.item.id in OrderHelper.selectedModifiersIds) {
                        OrderHelper.currentPrice -= str.item.cost
                        if (str.item.basecalories != null) {
                            OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                        }
                        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        item_calories.text =
                            OrderHelper.currentCalories.toString() + " Calories per serving"
                    }
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                    OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    iter.remove()
                }
            } else if (str is ItemCustomizeAdapter.TitleContainerSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.parentId, 0)
                    OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    iter.remove()
                }
            }
        }

        modifiersFordelete.remove(modifier)
        unselectModifiers(modifiersFordelete)

        if (modifier.modifiers != null)
            if (modifier.modifiers!!.isNotEmpty()) {
                var size = 0
                val iter1 = rows.iterator()
                while (iter1.hasNext()) {
                    val str = iter1.next()
                    rowsTmp.add(str)
                    if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                        if (str.parentId != 0.toLong() && str.mod.id == group.id) {
                            size++
                            if (str.mod.options.size == size) {
                                if (modifier.modifiers != null)
                                    if (modifier.modifiers!!.isNotEmpty()) {
                                        for (item in modifier.modifiers!!) {
                                            listAddedIds.add(group.id)
                                            rowsTmp.add(
                                                ItemCustomizeAdapter.TitleContainerSubModifier(
                                                    modifier,
                                                    item,
                                                    group.id
                                                )
                                            )
                                            for (itm in item.options) {
                                                if (item.mandatory) {
                                                    rowsTmp.add(
                                                        ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                            item,
                                                            itm, group.id,modifier
                                                        )
                                                    )
                                                } else {
                                                    rowsTmp.add(
                                                        ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                            item,
                                                            itm, group.id,modifier
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                        }

                    } else
                        if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                            if (str.parentId != 0.toLong() && str.mod.id == group.id) {
                                size++
                                if (str.mod.options.size == size) {
                                    if (modifier.modifiers != null)
                                        if (modifier.modifiers!!.isNotEmpty()) {
                                            for (item in modifier.modifiers!!) {
                                                listAddedIds.add(group.id)
                                                rowsTmp.add(
                                                    ItemCustomizeAdapter.TitleContainerSubModifier(
                                                        modifier,
                                                        item,
                                                        group.id
                                                    )
                                                )
                                                for (itm in item.options) {
                                                    if (item.mandatory) {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    } else {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }

                    rows = rowsTmp
                }
            }
        adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
        container.adapter = adapter
        container.post {
            container.recycledViewPool.clear()
            adapter.notifyDataSetChanged()
        }
        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
        item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
    }


    @SuppressLint("SetTextI18n")
    override fun onCheckBoxSubModifiersCheck(
        group: Modifier,
        modifier: Option,
        check: Boolean
    ) {
        //   OrderHelper.fromEdit = false
        rowsTmp = mutableListOf()
        if (!check) {
            if (modifier.basecalories != null) {
                OrderHelper.currentCalories -= modifier.basecalories!!.toLong()
            }
            OrderHelper.currentPrice -= modifier.cost
            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
            item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
            val iter = rows.iterator()
            while (iter.hasNext()) {
                val str = iter.next()
                if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                        modifiersFordelete.add(item)
                        if (str.item.id in OrderHelper.selectedModifiersIds) {
                            if (str.item.basecalories != null) {
                                OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                                OrderHelper.currentPrice -= str.item.cost
                            }

                            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                            item_calories.text =
                                OrderHelper.currentCalories.toString() + " Calories per serving"
                        }
                        OrderHelper.quantityModifiers.put(
                            str.item.id,
                            0
                        )
                        OrderHelper.selectedModifiersIds.remove(str.item.id)
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                        iter.remove()
                    }
                } else if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                        modifiersFordelete.add(item)
                        if (str.item.id in OrderHelper.selectedModifiersIds) {
                            OrderHelper.currentPrice -= str.item.cost
                            if (str.item.basecalories != null) {
                                OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                            }
                            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                            item_calories.text =
                                OrderHelper.currentCalories.toString() + " Calories per serving"
                        }
                        OrderHelper.quantityModifiers.put(
                            str.item.id,
                            0
                        )
                        OrderHelper.selectedModifiersIds.remove(str.item.id)
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                        iter.remove()
                    }
                } else if (str is ItemCustomizeAdapter.TitleContainerSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                        modifiersFordelete.add(item)
                        OrderHelper.selectedModifiersIds.remove(str.itm.id)
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.itm.id, 0)
                        iter.remove()
                    }
                }
            }

            modifiersFordelete.remove(modifier)
            unselectModifiers(modifiersFordelete)

            adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
            container.adapter = adapter
            container.post {
                container.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
            }
        }
        if (check) {
            if (modifier.basecalories != null) {
                OrderHelper.currentCalories += modifier.basecalories!!.toLong()
            }
            OrderHelper.currentPrice += modifier.cost
            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
            item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"

            if (modifier.modifiers != null)
                if (modifier.modifiers!!.isNotEmpty()) {
                    var size = 0
                    val iter1 = rows.iterator()
                    while (iter1.hasNext()) {
                        val str = iter1.next()
                        rowsTmp.add(str)
                        if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                            if (str.parentId != 0.toLong() && str.mod.id == group.id) {
                                size++
                                if (str.mod.options.size == size) {
                                    if (modifier.modifiers != null)
                                        if (modifier.modifiers!!.isNotEmpty()) {
                                            for (item in modifier.modifiers!!) {
                                                listAddedIds.add(group.id)
                                                rowsTmp.add(
                                                    ItemCustomizeAdapter.TitleContainerSubModifier(
                                                        modifier,
                                                        item,
                                                        group.id
                                                    )
                                                )
                                                for (itm in item.options) {
                                                    if (item.mandatory) {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    } else {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }

                        } else if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                            if (str.parentId != 0.toLong() && str.mod.id == group.id) {
                                size++
                                if (str.mod.options.size == size) {
                                    if (modifier.modifiers != null)
                                        if (modifier.modifiers!!.isNotEmpty()) {
                                            for (item in modifier.modifiers!!) {
                                                listAddedIds.add(group.id)
                                                rowsTmp.add(
                                                    ItemCustomizeAdapter.TitleContainerSubModifier(
                                                        modifier,
                                                        item,
                                                        group.id
                                                    )
                                                )
                                                for (itm in item.options) {
                                                    if (item.mandatory) {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    } else {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                                item,
                                                                itm, group.id,modifier
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                        rows = rowsTmp
                    }
                    adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
                    container.adapter = adapter
                    container.post {
                        container.recycledViewPool.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
        }


    }

    override fun onSuccessGetItemModifiers(response: OLOProductModifiersResponse) {
        homeActivity.presenter.dismissProgress()
        for (item in response.optiongroups) {
            rows.add(ItemCustomizeAdapter.TitleContainer(item, 0))
            for (itm in item.options) {
                if (item.mandatory) {
                    rows.add(
                        ItemCustomizeAdapter.ItemCustomizeRadioButton(
                            item,
                            itm, 0
                        )
                    )
                } else {
                    rows.add(
                        ItemCustomizeAdapter.ItemCustomizeCheckButton(
                            item,
                            itm, 0
                        )
                    )
                }
            }
        }

        if (fromEdit) {
            for (product in productForEdit!!.choices) {
                for (item in response.optiongroups) {
                    for (itm in item.options) {
                        if (product.optionid == itm.id) {
                            OrderHelper.checkedRadioGruopWithModifiers.put(
                                item.id,
                                product.optionid
                            )
                        }
                        if (itm.modifiers != null)
                            for (it in itm.modifiers!!) {
                                if (product.optionid == it.id) {
                                    OrderHelper.checkedRadioGruopWithModifiers.put(
                                        itm.id,
                                        product.optionid
                                    )
                                }
                                getAll(product.optionid, it)
                            }
                    }
                }
            }

            adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, -1)
            container.adapter = adapter
            container.recycledViewPool.clear()
            adapter.notifyDataSetChanged()
        } else {
            try {
                adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, -1)
                container?.adapter = adapter
                container.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
            } catch (ex: IllegalStateException) {
            }
        }
    }

    private fun getAll(optionid: Long, option: Modifier) {
        for (item in option.options) {
            if (optionid == item.id) {
                OrderHelper.checkedRadioGruopWithModifiers.put(
                    option.id,
                    optionid
                )
            }
            if (item.modifiers != null)
                if (item.modifiers!!.isNotEmpty())
                    for (itm in item.modifiers!!) {
                        if (optionid == itm.id) {
                            OrderHelper.checkedRadioGruopWithModifiers.put(
                                item.id,
                                optionid
                            )
                        }
                        getAll(optionid, itm)
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

    @SuppressLint("SetTextI18n")
    override fun onRadioBtnCheckChanges(
        group: Optiongroup,
        modifier: Option, check: Boolean
    ) {
        //  OrderHelper.fromEdit = false
        modifiersFordelete.clear()
        val iter = rows.iterator()
        while (iter.hasNext()) {
            val str = iter.next()
            if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    // modifiersFordelete.clear()
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    if (str.item.id in OrderHelper.selectedModifiersIds) {
                        if (str.item.basecalories != null) {
                            //    OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                        }
                        //  OrderHelper.currentPrice -= str.item.cost
                        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        item_calories.text =
                            OrderHelper.currentCalories.toString() + " Calories per serving"
                    }
                    //         OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                    iter.remove()
                }

//                if (str.parentId == 0.toLong()) {
//                    if (str.item.basecalories != null) {
//                        OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
//                    }
//                    OrderHelper.currentPrice -= str.item.cost
//                }

            } else if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    //modifiersFordelete.clear()
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    if (str.item.id in OrderHelper.selectedModifiersIds) {
                        if (str.item.basecalories != null) {
                            //  OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                        }
                        // OrderHelper.currentPrice -= str.item.cost
                        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                        item_calories.text =
                            OrderHelper.currentCalories.toString() + " Calories per serving"
                    }
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    //   OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                    iter.remove()
                }
//                if (str.parentId == 0.toLong()) {
//                    if (str.item.basecalories != null) {
//                        OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
//                    }
//                    OrderHelper.currentPrice -= str.item.cost
//                }

            } else if (str is ItemCustomizeAdapter.TitleContainerSubModifier) {
                if (str.parentId != 0.toLong() && str.parentId == group.id) {
                    //   modifiersFordelete.clear()
                    if (group.options.isNotEmpty())
                        for (item in group.options)
                            if (item.id in OrderHelper.selectedModifiersIds)
                                if (item !in modifiersFordelete)
                                    modifiersFordelete.add(item)
                    OrderHelper.quantityModifiers.put(
                        str.item.id,
                        0
                    )
                    //      OrderHelper.selectedModifiersIds.remove(str.item.id)
                    OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                    OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                    OrderHelper.groupQuantityModifiers.put(str.itm.id, 0)
                    iter.remove()
                }
            }
            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
            item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
        }

        modifiersFordelete.remove(modifier)
        unselectModifiers(modifiersFordelete)


        if (modifier.modifiers != null)
            if (modifier.modifiers!!.isNotEmpty()) {
                for (item in modifier.modifiers!!) {
                    rows.add(
                        ItemCustomizeAdapter.TitleContainerSubModifier(
                            modifier,
                            item,
                            group.id
                        )
                    )
                    for (itm in item.options) {
                        if (item.mandatory) {
                            rows.add(
                                ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                    item,
                                    itm, group.id
                                    , modifier
                                )
                            )
                        } else {
                            rows.add(
                                ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                    item,
                                    itm, group.id, modifier
                                )
                            )
                        }
                    }
                }
            }
        adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
        container.adapter = adapter
        container.post { adapter.notifyDataSetChanged() }

        item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
        item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
    }

    @SuppressLint("SetTextI18n")
    override fun onCheckBoxCheck(
        group: Optiongroup,
        modifier: Option, check: Boolean
    ) {
        // OrderHelper.fromEdit = false
        rowsTmp = mutableListOf()
        if (!check) {
            val iter = rows.iterator()
            while (iter.hasNext()) {
                val str = iter.next()
                if (str is ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                    //  modifiersFordelete.add(item)
                                        if (str.item.id in OrderHelper.selectedModifiersIds) {
                                            if (str.item.basecalories != null) {
                                                OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                                            }

                                        }
                        //
                        //     OrderHelper.selectedModifiersIds.remove(str.item.id)
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)

                        if (str.item.id in OrderHelper.selectedModifiersDedault) {
                            str.item.isdefault = true
                            OrderHelper.selectedModifiersDedault.remove(str.item.id)
                        }
                        if (str.itm.id == modifier.id) {
                            iter.remove()
                            if (str.item.id in OrderHelper.selectedModifiersIds) {
                                OrderHelper.selectedModifiersIds.remove(str.item.id)
                                OrderHelper.currentPrice -= str.item.cost
                                item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                                item_calories.text =
                                    OrderHelper.currentCalories.toString() + " Calories per serving"
                            }
                        }
                    }
                } else if (str is ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                    // modifiersFordelete.add(item)
                                        if (str.item.id in OrderHelper.selectedModifiersIds) {
                                            if (str.item.basecalories != null) {
                                                OrderHelper.currentCalories -= str.item.basecalories!!.toLong()
                                            }
//                            OrderHelper.currentPrice -= str.item.cost
//                            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
//                            item_calories.text =
//                                OrderHelper.currentCalories.toString() + " Calories per serving"
                                        }
                        //   OrderHelper.selectedModifiersIds.remove(str.item.id)
                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.mod.id, 0)
                        if (str.itm.id == modifier.id) {
                            iter.remove()
                            if (str.item.id in OrderHelper.selectedModifiersIds) {
                                OrderHelper.selectedModifiersIds.remove(str.item.id)
                                OrderHelper.currentPrice -= str.item.cost
                                item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
                                item_calories.text =
                                    OrderHelper.currentCalories.toString() + " Calories per serving"
                            }
                        }
                    }
                } else if (str is ItemCustomizeAdapter.TitleContainerSubModifier) {
                    if (str.parentId != 0.toLong() && str.parentId == group.id) {
                        if (group.options.isNotEmpty())
                            for (item in group.options)
                                if (item.id in OrderHelper.selectedModifiersIds)
                                    if (item !in modifiersFordelete)
                                    //       modifiersFordelete.add(item)
                                    //     OrderHelper.selectedModifiersIds.remove(str.itm.id)
                                        OrderHelper.checkedRadioGruopWithModifiers.remove(str.item.id)
                        OrderHelper.checkedCheckBoxWithModifiers.remove(str.item.id)
                        OrderHelper.groupQuantityModifiers.put(str.itm.id, 0)
                        if (str.itm.id == modifier.id)
                            iter.remove()
                    }
                }
            }

            modifiersFordelete.remove(modifier)
            unselectModifiers(modifiersFordelete)

            adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
            container.adapter = adapter
            container.post {
                container.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
            }
        }
        if (check) {
            if (modifier.basecalories != null) {
                OrderHelper.currentCalories += modifier.basecalories!!.toLong()
            }
            OrderHelper.currentPrice += modifier.cost
            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
            item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
            if (modifier.modifiers != null)
                if (modifier.modifiers!!.isNotEmpty()) {
                    var size = 0
                    val iter1 = rows.iterator()
                    while (iter1.hasNext()) {
                        val str = iter1.next()
                        rowsTmp.add(str)
                        if (str is ItemCustomizeAdapter.ItemCustomizeRadioButton) {
                            if (str.mod.id == group.id) {
                                size++
                                if (str.mod.options.size == size) {
                                    if (modifier.modifiers != null)
                                        if (modifier.modifiers!!.isNotEmpty()) {
                                            for (item in modifier.modifiers!!) {
                                                listAddedIds.add(group.id)
                                                rowsTmp.add(
                                                    ItemCustomizeAdapter.TitleContainerSubModifier(
                                                        modifier,
                                                        item,
                                                        group.id
                                                    )
                                                )
                                                for (itm in item.options) {
                                                    if (item.mandatory) {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                                item,
                                                                itm, group.id, modifier
                                                            )
                                                        )
                                                    } else {
                                                        rowsTmp.add(
                                                            ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                                item,
                                                                itm, group.id, modifier
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                }
                            }

                        } else
                            if (str is ItemCustomizeAdapter.ItemCustomizeCheckButton) {
                                if (str.mod.id == group.id) {
                                    size++
                                    if (str.mod.options.size == size) {
                                        if (modifier.modifiers != null)
                                            if (modifier.modifiers!!.isNotEmpty()) {
                                                for (item in modifier.modifiers!!) {
                                                    listAddedIds.add(group.id)
                                                    rowsTmp.add(
                                                        ItemCustomizeAdapter.TitleContainerSubModifier(
                                                            modifier,
                                                            item,
                                                            group.id
                                                        )
                                                    )
                                                    for (itm in item.options) {
                                                        if (item.mandatory) {
                                                            rowsTmp.add(
                                                                ItemCustomizeAdapter.ItemCustomizeRadioButtonSubModifier(
                                                                    item,
                                                                    itm, group.id, modifier
                                                                )
                                                            )
                                                        } else {
                                                            rowsTmp.add(
                                                                ItemCustomizeAdapter.ItemCustomizeCheckButtonSubModifier(
                                                                    item,
                                                                    itm, group.id, modifier
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            }

                        rows = rowsTmp
                    }
                    adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
                    container.adapter = adapter
                    container.post {
                        container.recycledViewPool.clear()
                        adapter.notifyDataSetChanged()
                    }
                }

            adapter = ItemCustomizeAdapter(rows, homeActivity, this, this, 0)
            container.adapter = adapter
            container.post {
                container.recycledViewPool.clear()
                adapter.notifyDataSetChanged()
            }
        } else {
            if (modifier.basecalories != null) {
                OrderHelper.currentCalories -= modifier.basecalories!!.toLong()
            }
            OrderHelper.currentPrice -= modifier.cost
            item_price.text = "$" + String.format("%.2f", OrderHelper.currentPrice)
            item_calories.text = OrderHelper.currentCalories.toString() + " Calories per serving"
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

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_basket -> {
                OrderHelper.flagChechForRewards = false
                homeActivity.presenter.openFragmentRight(
                    getFragmentSumamry(
                        OrderSummaryFragment(), externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }

            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }
            R.id.btn_add_to_bag -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()

                    var options = ""
                    val optin: String
                    if (OrderHelper.selectedModifiersIds.isNotEmpty()) {
                        for (item in OrderHelper.selectedModifiersIds.asReversed()) {
                            options += ("$item,")
                        }
                        optin = options.substring(0, options.length - 1)
                    } else {
                        optin = ""
                    }

                    var flagHasModifersQuantity = false
                    for (keys in OrderHelper.quantityModifiers) {
                        if (keys.value != 0) {
                            flagHasModifersQuantity = true
                        }
                    }

                    if (flagHasModifersQuantity) {
                        val choices = mutableListOf<ChoiceX>()
                        if (OrderHelper.selectedModifiersIds.isNotEmpty()) {
                            for (item in OrderHelper.selectedModifiersIds.asReversed()) {
                                var flag = false
                                for (keys in OrderHelper.quantityModifiers) {
                                    if (keys.value != 0) {
                                        if (keys.key == item) {
                                            flag = true
                                            val choic =
                                                ChoiceX(choiceid = item, quantity = keys.value)
                                            choices.add(choic)
                                        }
                                    }
                                }
                                if (!flag)
                                    choices.add(ChoiceX(choiceid = item, quantity = 1))
                            }
                        }

                        if (OrderHelper.fromEditForEdit) {
                            presenter.updateProductFromBasketBatch(
                                homeActivity,
                                productForEdit!!.id,
                                selectedItem!!.id,
                                productQuantity,
                                externalPartnerId.toInt(),
                                et_special_instructions.text.toString(), choices,
                                OrderHelper.basketId!!
                            )
                        } else {
                            presenter.addProductToBasketBatch(
                                homeActivity,
                                selectedItem!!.id,
                                productQuantity,
                                externalPartnerId.toInt(),
                                et_special_instructions.text.toString(), choices,
                                OrderHelper.basketId!!
                            )
                        }
                    } else {
                        if (OrderHelper.fromEditForEdit) {
                            presenter.updateProductFromBasket(
                                homeActivity,
                                productForEdit!!.id,
                                selectedItem!!.id,
                                productQuantity,
                                externalPartnerId.toInt(),
                                et_special_instructions.text.toString(), optin,
                                OrderHelper.basketId!!
                            )
                        } else {
                            presenter.addProductToBasket(
                                homeActivity,
                                selectedItem!!.id,
                                productQuantity,
                                externalPartnerId.toInt(),
                                et_special_instructions.text.toString(), optin,
                                OrderHelper.basketId!!
                            )
                        }
                    }
//                    if (OrderHelper.fromEdit) {
//                        presenter.updateProductFromBasket(
//                            homeActivity,
//                            productForEdit!!.id,
//                            selectedItem!!.id,
//                            productQuantity,
//                            externalPartnerId.toInt(),
//                            et_special_instructions.text.toString(), optin,
//                            OrderHelper.basketId!!
//                        )
//                    } else {
//                        presenter.addProductToBasket(
//                            homeActivity,
//                            selectedItem!!.id,
//                            productQuantity,
//                            externalPartnerId.toInt(),
//                            et_special_instructions.text.toString(), optin,
//                            OrderHelper.basketId!!
//                        )
//                    }
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }

            }

            R.id.btn_plus -> {
                if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                    productQuantity += selectedItem!!.quantityincrement.toInt()
                } else {
                    productQuantity++
                }
                if (selectedItem!!.maximumquantity != null && selectedItem!!.maximumquantity.isNotEmpty()) {
                    if (productQuantity <= selectedItem!!.maximumquantity.toInt()) {
                        txt_quantity.text = productQuantity.toString()
                        item_price.text =
                            "$" + String.format("%.2f", OrderHelper.currentPrice * productQuantity)
                    } else {
                        if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                            productQuantity -= selectedItem!!.quantityincrement.toInt()
                        } else {
                            productQuantity--
                        }
                    }
                } else {
                    if (productQuantity <= 999) {
                        txt_quantity.text = productQuantity.toString()
                        item_price.text =
                            "$" + String.format("%.2f", OrderHelper.currentPrice * productQuantity)
                    } else {
                        if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                            productQuantity -= selectedItem!!.quantityincrement.toInt()
                        } else {
                            productQuantity--
                        }
                    }
                }


            }
            R.id.btn_minus -> {
                if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                    productQuantity -= selectedItem!!.quantityincrement.toInt()
                } else {
                    productQuantity--
                }

                if (selectedItem!!.minimumquantity != null && selectedItem!!.minimumquantity.isNotEmpty()) {
                    if (productQuantity >= selectedItem!!.minimumquantity.toInt()) {
                        txt_quantity.text = productQuantity.toString()
                        item_price.text =
                            "$" + String.format("%.2f", OrderHelper.currentPrice * productQuantity)
                    } else {
                        if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                            productQuantity += selectedItem!!.quantityincrement.toInt()
                        } else {
                            productQuantity++
                        }
                    }

                } else {
                    if (productQuantity >= 1) {
                        txt_quantity.text = productQuantity.toString()
                        item_price.text =
                            "$" + String.format("%.2f", OrderHelper.currentPrice * productQuantity)
                    } else {
                        if (selectedItem?.quantityincrement != null && selectedItem!!.quantityincrement.isNotEmpty()) {
                            productQuantity += selectedItem!!.quantityincrement.toInt()
                        } else {
                            productQuantity++
                        }
                    }
                }
            }

        }
    }
}
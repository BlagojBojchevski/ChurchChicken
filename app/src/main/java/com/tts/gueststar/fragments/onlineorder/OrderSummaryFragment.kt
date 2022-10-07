package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.OrderSummaryAdapter
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.interfaces.OrderSummaryItemsInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.OrderSummaryPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.DateTimePicker
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.*
import kotlinx.android.synthetic.main.fragment_order_summary.*
import kotlinx.android.synthetic.main.item_fees.view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Calendar
import javax.inject.Inject

class OrderSummaryFragment : BaseFragment(), View.OnClickListener,
    OrderSummaryPresenter.OrderSummaryView {

    override fun onSuccessGetRestaurantMenu(response: OLORestaurantMenuResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.restaurantResponse = response
        if (OrderHelper.restaurantResponse != null) {
            for (item in OrderHelper.restaurantResponse!!.categories) {
                for (itm in item.products) {
                    if (itm.id == choiceSelected!!.productId) {
                        selectedItem = itm
                    }
                }
            }

            OrderHelper.selectedOptionsIds.clear()
            OrderHelper.selectedModifiersIds.clear()
            OrderHelper.checkedRadioGruopWithModifiers.clear()
            OrderHelper.checkedCheckBoxWithModifiers.clear()
            for (item in choiceSelected!!.choices) {
                OrderHelper.selectedModifiersIds.add(item.optionid)
                OrderHelper.quantityModifiers[item.optionid] = item.quantity
            }

            for (it_ in OrderHelper.selectedModifiersIds)
                OrderHelper.TMPselectedModifiersIds.add(it_)

            OrderHelper.fromEdit = true
            OrderHelper.fromEditForEdit = true
//            homeActivity.clearStackOnlineOrder()
            homeActivity.presenter.openFragmentRight(
                getFragmentItemDetails(
                    ItemDetailsFragment(),
                    externalPartnerId,
                    selectedItem!!,
                    choiceSelected!!,
                    OrderHelper.restaurantResponse!!.imagepath!!
                ),
                AppConstants.TAG_ONLINE_ORDER
            )
        }
    }

    private var alertDialogBuilder: AlertDialog.Builder? = null
    private var choiceSelected: Product1? = null
    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity

    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private lateinit var adapter: OrderSummaryAdapter
    private var selectedItem: Product? = null
    lateinit var presenter: OrderSummaryPresenter
    private var flag = true
    private var asaptext = ""
    private var flagDisableCancel = false
    private var calendar: Date? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onPause() {
        super.onPause()
        OrderHelper.fromLogin = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_summary, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   OrderHelper.pickupTime = null
        if (arguments != null) {
            externalPartnerId = requireArguments().getString(AppConstants.EXTERNAL_ID)!!
        }
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = OrderSummaryPresenter(this)
        btn_close.setOnClickListener(this)
        btn_keep_ordering.setOnClickListener(this)
        change_location.setOnClickListener(this)
        change_car_info.setOnClickListener(this)
        change_delivery_address.setOnClickListener(this)
        change_time.setOnClickListener(this)
        btn_home.setOnClickListener(this)
        btn_checkout.setOnClickListener(this)
        layout_rewards.setOnClickListener(this)
        remove.setOnClickListener(this)
        remove.underline()
        change_car_info.underline()
        change_delivery_address.underline()
        change_location.underline()
        change_time.underline()
        cbASAP.setOnClickListener {
            if (Engine().isNetworkAvailable(homeActivity)) {
                homeActivity.presenter.showProgress()
                presenter.deleteTimeWantedFromBasket(homeActivity, OrderHelper.basketId!!)
            } else {
                Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }
        }

        cbLater.setOnClickListener {
            if (Engine().isNetworkAvailable(homeActivity)) {
                val from: String
                val to: String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dateFrom = LocalDate.now()
                    val dateTo = LocalDate.now().plusDays(OrderHelper.advanceorderdays.toLong())
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                    from = dateFrom.format(formatter)
                    to = dateTo.format(formatter)
                } else {
                    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                    val cal = Calendar.getInstance()
                    val calTo = Calendar.getInstance()
                    val dateFrom = cal.time
                    calTo.add(Calendar.DAY_OF_MONTH, OrderHelper.advanceorderdays)
                    val dateTo = calTo.time
                    from = dateFormat.format(dateFrom)
                    to = dateFormat.format(dateTo)
                }
                homeActivity.presenter.showProgress()
                presenter.getRestaurantCalendar(homeActivity, externalPartnerId.toInt(), from, to)
            } else {
                Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }
        }


    }

    private fun selectLater() {
        if (!OrderHelper.flagChechForRewards) {
            if (Engine().isNetworkAvailable(homeActivity)) {
                cbASAP.setImageResource(R.drawable.radio_check_off)
                cbLater.setImageResource(R.drawable.ic_check_circle)
                val from: String
                val to: String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dateFrom = LocalDate.now()
                    val dateTo = LocalDate.now().plusDays(OrderHelper.advanceorderdays.toLong())
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                    from = dateFrom.format(formatter)
                    to = dateTo.format(formatter)
                } else {
                    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                    val cal = Calendar.getInstance()
                    val calTo = Calendar.getInstance()
                    val dateFrom = cal.time
                    calTo.add(Calendar.DAY_OF_MONTH, OrderHelper.advanceorderdays)
                    val dateTo = calTo.time
                    from = dateFormat.format(dateFrom)
                    to = dateFormat.format(dateTo)
                }

                homeActivity.presenter.showProgress()
                presenter.getRestaurantCalendar(homeActivity, externalPartnerId.toInt(), from, to)
            } else {
                Engine().showMsgDialog(
                    "",
                    getString(R.string.error_network_connection),
                    homeActivity
                )
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        OrderHelper.vendorId = externalPartnerId.toInt()
        initView()
        if (OrderHelper.location != null) {
            tvLocationName.text = OrderHelper.location!!.name
            tvLocationAddress.text = OrderHelper.location!!.address

            if (OrderHelper.fromHistory) {
                tvLocationState.text = getString(
                    R.string.location_state_text,
                    OrderHelper.location!!.city_label,
                    OrderHelper.location!!.state_label,
                    OrderHelper.location!!.zipcode
                ) + "\n" + OrderHelper.location!!.phone_number.replace("(", "").replace(
                    ")",
                    ""
                ).replace(" ", "-")
            } else {
                tvLocationState.text = getString(
                    R.string.location_state_text,
                    OrderHelper.location!!.city_label,
                    Engine().convertState(OrderHelper.location!!.state_label),
                    OrderHelper.location!!.zipcode
                ) + "\n" + OrderHelper.location!!.phone_number.replace("(", "").replace(
                    ")",
                    ""
                ).replace(" ", "-")
            }

            if (OrderHelper.location!!.restaurant_distance == 0.0) {
                tvLocationDistance.visibility = View.INVISIBLE
            } else {
                tvLocationDistance.visibility = View.VISIBLE
                tvLocationDistance.text = getString(
                    R.string.location_distance_text,
                    OrderHelper.location!!.restaurant_distance.toString()
                )
            }


        }

        when (OrderHelper.deliveryMode) {
            getString(R.string.mode_pickup) -> {
                order_method.text = getString(R.string.pickup)
                layout_car.visibility = View.GONE
                layout_delivery.visibility = View.GONE
            }
            getString(R.string.mode_curbside) -> {
                order_method.text = getString(R.string.catering)
                layout_car.visibility = View.VISIBLE
                layout_delivery.visibility = View.GONE
                car_info.text = OrderHelper.carInfo.replace(";", " ")
            }
            else -> {
                order_method.text = getString(R.string.delivery)
                layout_car.visibility = View.GONE
                layout_delivery.visibility = View.VISIBLE
                delyvery_info.text = OrderHelper.deliveryAddress
                line1.visibility = View.GONE
            }
        }


        if (OrderHelper.basket!!.customerhandoffcharge > 0.0) {
            delivery_fee_layout.visibility = View.VISIBLE
            txt_delivery_fee.text =
                "$" + String.format("%.2f", OrderHelper.basket!!.customerhandoffcharge)
        } else {
            delivery_fee_layout.visibility = View.GONE
        }

        if (OrderHelper.pickupTime == null) {
            if (OrderHelper.advanceonly != null)
                if (OrderHelper.advanceonly!! && OrderHelper.isavailable!!) {
                    flagDisableCancel = false
                    selectLater()
                    layout_asap.visibility = View.GONE
                } else {
                    if (OrderHelper.isavailable != null)
                        if (OrderHelper.isavailable!!) {
                            if (Engine().isNetworkAvailable(homeActivity)) {
                                if (flag) {
                                    flag = false
                                    if (OrderHelper.basket!!.timemode != getString(R.string.asap)) {
                                        homeActivity.presenter.showProgress ()
                                        presenter.deleteTimeWantedFromBasket(
                                            homeActivity,
                                            OrderHelper.basketId!!
                                        )
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val dateFrom = LocalDateTime.now()
                                            val formatter =
                                                DateTimeFormatter.ofPattern("yyyyMMdd HH:mm")
                                            val date = LocalDateTime.parse(
                                                OrderHelper.basket!!.earliestreadytime,
                                                formatter
                                            )
                                            Log.d(
                                                "TAG",
                                                "onResume: ${dateFrom} - ${OrderHelper.basket!!.earliestreadytime} - ${OrderHelper.basket!!.leadtimeestimateminutes}"
                                            )
                                            if (dateFrom.isAfter(date)) {
                                                OrderHelper.pickupTime = null
                                                try {
                                                    cbLater.setImageResource(R.drawable.radio_check_off)
                                                    cbASAP.setImageResource(R.drawable.ic_check_circle)
                                                    asap_time.text =
                                                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                            R.string.minutes
                                                        ) + ")"
                                                    Handler().postDelayed({
                                                        asaptext =
                                                            getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                R.string.minutes
                                                            ) + ")"
                                                        asap_time.text =
                                                            getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                R.string.minutes
                                                            ) + ")"
                                                    }, 400)
                                                } catch (er: NullPointerException) {
                                                    homeActivity.presenter.dismissProgress()
                                                }
                                            } else {
                                                flagDisableCancel = true
                                                selectLater()
                                            }
                                        } else {
                                            val dateFormat =
                                                SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)
                                            val cal = Calendar.getInstance()
                                            val dateFrom = cal.time
                                            val date =
                                                dateFormat.parse(OrderHelper.basket!!.earliestreadytime)
                                            if (dateFrom.after(date)) {
                                                OrderHelper.pickupTime = null
                                                try {
                                                    cbLater.setImageResource(R.drawable.radio_check_off)
                                                    cbASAP.setImageResource(R.drawable.ic_check_circle)
                                                    asap_time.text =
                                                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                            R.string.minutes
                                                        ) + ")"

                                                    Handler().postDelayed({
                                                        asaptext =
                                                            getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                R.string.minutes
                                                            ) + ")"
                                                        asap_time.text =
                                                            getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                R.string.minutes
                                                            ) + ")"
                                                    }, 400)
                                                } catch (er: NullPointerException) {
                                                    homeActivity.presenter.dismissProgress()
                                                }
                                            } else {
                                                flagDisableCancel = true
                                                selectLater()
                                            }
                                        }
                                    }


                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val dateFrom = LocalDateTime.now()
                                        val formatter =
                                            DateTimeFormatter.ofPattern("yyyyMMdd HH:mm")
                                        val date = LocalDateTime.parse(
                                            OrderHelper.basket!!.earliestreadytime,
                                            formatter
                                        )

                                        if (dateFrom.isAfter(date)) {
                                            OrderHelper.pickupTime = null
                                            try {
                                                cbLater.setImageResource(R.drawable.radio_check_off)
                                                cbASAP.setImageResource(R.drawable.ic_check_circle)
                                                asap_time.text =
                                                    getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                        R.string.minutes
                                                    ) + ")"
                                                Handler().postDelayed({
                                                    asaptext =
                                                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                            R.string.minutes
                                                        ) + ")"
                                                    asap_time.text =
                                                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                            R.string.minutes
                                                        ) + ")"
                                                }, 400)
                                            } catch (er: NullPointerException) {
                                                homeActivity.presenter.dismissProgress()
                                            }
                                        } else {
                                            flagDisableCancel = true
                                            selectLater()
                                        }
                                    } else {
                                        val dateFormat =
                                            SimpleDateFormat("yyyyMMdd HH:mm", Locale.US)
                                        val cal = Calendar.getInstance()
                                        val dateFrom = cal.time
                                        val date =
                                            dateFormat.parse(OrderHelper.basket!!.earliestreadytime)
                                        if (dateFrom.after(date)) {
                                            OrderHelper.pickupTime = null
                                            try {
                                                cbLater.setImageResource(R.drawable.radio_check_off)
                                                cbASAP.setImageResource(R.drawable.ic_check_circle)
                                                asap_time.text =
                                                    getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                        R.string.minutes
                                                    ) + ")"

                                                try {
                                                    Handler().postDelayed({
                                                        asaptext =
                                                            getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                R.string.minutes
                                                            ) + ")"
                                                        if (asap_time != null)
                                                            asap_time.text =
                                                                getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                                                                    R.string.minutes
                                                                ) + ")"
                                                    }, 400)
                                                } catch (we: java.lang.IllegalStateException) {
                                                }
                                            } catch (er: NullPointerException) {
                                                homeActivity.presenter.dismissProgress()
                                            }
                                        } else {
                                            flagDisableCancel = true
                                            selectLater()
                                        }
                                    }
                                }
                            } else {
                                Engine().showMsgDialog(
                                    "",
                                    getString(R.string.error_network_connection),
                                    homeActivity
                                )
                            }

                        } else {
                            flagDisableCancel = false
                            Log.d("TAG", "onResume: Vlez 6")
                            selectLater()
                        }
                }
        } else {
            txt_later.text = OrderHelper.pickupTime
            txt_later.visibility = View.VISIBLE
            cbLater.setImageResource(R.drawable.ic_check_circle)
            cbASAP.setImageResource(R.drawable.radio_check_off)


            Handler().postDelayed({
                try {
                    asaptext =
                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                            R.string.minutes
                        ) + ")"
                    asap_time.text =
                        getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                            R.string.minutes
                        ) + ")"
                } catch (er: IllegalStateException) {
                }
            }, 400)

        }

        if (OrderHelper.fromLogin) {
            afterLogin()
        }
    }


    @SuppressLint("SetTextI18n")
    fun initView() {
        rvItemsSummary.layoutManager =
            LinearLayoutManager(homeActivity.applicationContext)
        adapter = OrderHelper.basket!!.products?.let {
            OrderSummaryAdapter(
                homeActivity,
                it,
                object : OrderSummaryItemsInterface {
                    override fun removeItem(choice: Product1) {
                        try {
                            if (alertDialogBuilder == null) {
                                alertDialogBuilder = AlertDialog.Builder(context)
                                alertDialogBuilder!!.setMessage(
                                    getString(R.string.txt_remove_1) + " " + choice.name + " " + getString(
                                        R.string.txt_remove_2
                                    )
                                ).setCancelable(false).setPositiveButton(
                                    "Yes"
                                ) { dialog, _ ->
                                    if (Engine().isNetworkAvailable(homeActivity)) {
                                        homeActivity.presenter.showProgress()
                                        presenter.deleteProductFromBasket(
                                            homeActivity,
                                            OrderHelper.basketId!!,
                                            choice.id
                                        )
                                    } else {
                                        Engine().showMsgDialog(
                                            "",
                                            getString(R.string.error_network_connection),
                                            homeActivity
                                        )
                                    }
                                    alertDialogBuilder = null
                                    dialog.cancel()

                                }
                                    .setNegativeButton(
                                        "No"
                                    ) { dialog, _ ->
                                        alertDialogBuilder = null
                                        dialog.cancel()

                                    }
                                val alert = alertDialogBuilder!!.create()
                                alert.setTitle("")
                                alert.show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun editItem(choice: Product1) {
                        choiceSelected = choice
                        if (OrderHelper.restaurantResponse != null) {
                            for (item in OrderHelper.restaurantResponse!!.categories) {
                                for (itm in item.products) {
                                    if (itm.id == choice.productId) {
                                        selectedItem = itm
                                    }
                                }
                            }

                            OrderHelper.selectedOptionsIds.clear()
                            OrderHelper.selectedModifiersIds.clear()
                            OrderHelper.checkedRadioGruopWithModifiers.clear()
                            OrderHelper.checkedCheckBoxWithModifiers.clear()
                            for (item in choice.choices) {
                                OrderHelper.selectedModifiersIds.add(item.optionid)
                                OrderHelper.quantityModifiers.put(item.optionid, item.quantity)

                            }

                            for (it_ in OrderHelper.selectedModifiersIds)
                                OrderHelper.TMPselectedModifiersIds.add(it_)

                            OrderHelper.fromEdit = true
                            OrderHelper.fromEditForEdit = true
                            if (!OrderHelper.fromHistory)
                                homeActivity.onBackPressed()
                            homeActivity.presenter.dismissProgress()
                            var imagePa = ""
                            if (OrderHelper.restaurantResponse!!.imagepath != null) {
                                imagePa = OrderHelper.restaurantResponse!!.imagepath!!
                            }

                            homeActivity.presenter.openFragmentRight(
                                getFragmentItemDetails(
                                    ItemDetailsFragment(),
                                    externalPartnerId,
                                    selectedItem!!,
                                    choice,
                                    imagePa
                                ),
                                AppConstants.TAG_ONLINE_ORDER
                            )
                        } else {
                            if (Engine().isNetworkAvailable(homeActivity)) {
                                homeActivity.presenter.showProgress()
                                presenter.getRestaurantMenu(
                                    homeActivity,
                                    externalPartnerId.toInt()
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
                })
        }!!

        rvItemsSummary.adapter = adapter

        item_count.text = OrderHelper.basket!!.products!!.size.toString()
        if (OrderHelper.basket!!.products!!.size == 1) {
            items.text = getString(R.string.item)
        } else {
            items.text = getString(R.string.items)
        }
        txt_subtotal.text = "$" + String.format("%.2f", OrderHelper.basket!!.subtotal)
        txt_tax.text = "$" + String.format("%.2f", OrderHelper.basket!!.salestax)

        val total = OrderHelper.basket!!.total
        val tot = roundOffDecimal(total)
        txt_total.text = "$" + String.format("%.2f", tot)


        if (OrderHelper.basket!!.products!!.isEmpty()) {
            btn_checkout.alpha = 0.5F
            btn_checkout.isEnabled = false
            btn_checkout.isClickable = false
        } else {
            if (asaptext.length == homeActivity.getString(R.string.order_asap).length && OrderHelper.pickupTime == null) {
                btn_checkout.alpha = 0.5F
                btn_checkout.isEnabled = false
                btn_checkout.isClickable = false
            } else {
                btn_checkout.alpha = 1F
                btn_checkout.isEnabled = true
                btn_checkout.isClickable = true
            }
        }


        if (OrderHelper.restaurantDetails!!.supportsloyalty) {
            layout_rewards.isEnabled = true
            layout_rewards.isClickable = true
            layout_rewards.visibility = View.VISIBLE
            if (OrderHelper.basket!!.appliedrewards != null) {
                if (OrderHelper.basket!!.appliedrewards!!.isNotEmpty()) {
                    reward_name.text = OrderHelper.basket!!.appliedrewards?.get(0)?.label!!
                    layout_rewards.visibility = View.GONE
                } else {
                    layout_rewards.visibility = View.VISIBLE
                    applyed_rewad.visibility = View.GONE
                }
            } else {
                layout_rewards.visibility = View.VISIBLE
                applyed_rewad.visibility = View.GONE
            }
        } else {
            layout_rewards.isEnabled = false
            layout_rewards.isClickable = false
            layout_rewards.alpha = 0.5F
            //  layout_rewards.visibility = View.GONE
            applyed_rewad.visibility = View.GONE
        }

        if (OrderHelper.basket!!.fees != null)
            if (OrderHelper.basket!!.fees!!.isNotEmpty()) {
                if (fees_layout.visibility == View.GONE) {
                    fees_layout.visibility = View.VISIBLE
                    for (item in OrderHelper.basket!!.fees!!) {

                        val cellViewCustomizeLayout = LayoutInflater.from(context).inflate(
                            R.layout.item_fees,
                            null
                        ) as RelativeLayout
                        cellViewCustomizeLayout.fees_name.text = item.description
                        cellViewCustomizeLayout.txt_fees_value.text =
                            "$" + String.format("%.2f", item.amount)

                        fees_layout.addView(cellViewCustomizeLayout)
                    }
                }
            } else {
                fees_layout.visibility = View.GONE
            }
        if (OrderHelper.basket!!.fees != null)
            if (OrderHelper.basket!!.fees!!.isNotEmpty()) {
                if (fees_layout.visibility == View.GONE) {
                    fees_layout.visibility = View.VISIBLE
                    for (item in OrderHelper.basket!!.fees!!) {

                        val cellViewCustomizeLayout = LayoutInflater.from(context).inflate(
                            R.layout.item_fees,
                            null
                        ) as RelativeLayout
                        cellViewCustomizeLayout.fees_name.text = item.description
                        cellViewCustomizeLayout.txt_fees_value.text =
                            "$" + String.format("%.2f", item.amount)

                        fees_layout.addView(cellViewCustomizeLayout)
                    }
                }
            } else {
                fees_layout.visibility = View.GONE
            }
    }

    private fun roundOffDecimal(number: Double): Double? {
        return try {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            df.format(number).toDouble()
        } catch (ex: NumberFormatException) {
            null
        }
    }

    private fun getFragmentItemDetails(
        fragment: BaseFragment,
        external_partner_id: String,
        item: Product,
        item1: Product1,
        imagePath: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putSerializable(AppConstants.SELECTED_ITEM, item)
        bundle.putSerializable(AppConstants.EDIT_ITEM, item1)
        bundle.putString(AppConstants.IMAGE_PATH, imagePath)
        bundle.putBoolean(AppConstants.FROM_EDIT, true)
        fragment.arguments = bundle
        return fragment
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.change_time -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    val from: String
                    val to: String
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val dateFrom = LocalDate.now()
                        val dateTo =
                            LocalDate.now().plusDays(OrderHelper.advanceorderdays.toLong())
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        from = dateFrom.format(formatter)
                        to = dateTo.format(formatter)
                    } else {
                        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
                        val cal = Calendar.getInstance()
                        val calTo = Calendar.getInstance()
                        val dateFrom = cal.time
                        calTo.add(Calendar.DAY_OF_MONTH, OrderHelper.advanceorderdays)
                        val dateTo = calTo.time
                        from = dateFormat.format(dateFrom)
                        to = dateFormat.format(dateTo)
                    }
                    homeActivity.presenter.showProgress()
                    presenter.getRestaurantCalendar(
                        homeActivity,
                        externalPartnerId.toInt(),
                        from,
                        to
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.change_car_info -> {
                homeActivity.presenter.openFragmentRight(
                    getFragmentCarDetails(
                        EnterCarDetailsFragment(), externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            }

            R.id.remove -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    presenter.deleteLoyaltyRewardFromBasket(
                        homeActivity,
                        OrderHelper.basketId!!,
                        OrderHelper.basket!!.appliedrewards?.get(0)!!.rewardid.toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btn_home -> {
                if (OrderHelper.fromHome)
                    homeActivity.clearStackOnlineOrderAndLocations()
                else
                    homeActivity.clearStackOnlineOrder()
            }

            R.id.btn_close -> {
                OrderHelper.flagChechForRewards = false
                homeActivity.onBackPressed()
            }

            R.id.layout_rewards -> {
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    homeActivity.presenter.openFragmentRight(
                        LoyaltyRewadsFragment(),
                        AppConstants.TAG_ONLINE_ORDER
                    )
                } else {
                    OrderHelper.flagChechForRewards = true
                    Engine.setNextPage = AppConstants.TAG_ONLINE_ORDER
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.btn_keep_ordering -> {
                if (OrderHelper.fromHistory) {
                    homeActivity.clearStackOnlineOrder()
                    homeActivity.presenter.openFragmentRight(
                        getFragmentMenu(
                            OrderMenuFragment(), externalPartnerId
                        ),
                        AppConstants.TAG_ONLINE_ORDER
                    )
                } else {
                    homeActivity.onBackPressed()
                }
            }
            R.id.btn_checkout -> {
//                OrderHelper.currentAddedAccount = null
                OrderHelper.fromSummary = true
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    if (Engine().isNetworkAvailable(homeActivity)) {
                        homeActivity.presenter.showProgress()
                        presenter.validateBasket(homeActivity, OrderHelper.basketId!!)
                    } else {
                        Engine().showMsgDialog(
                            "",
                            getString(R.string.error_network_connection),
                            homeActivity
                        )
                    }
                } else {
                    Engine.setNextPage = AppConstants.TAG_ONLINE_ORDER
                    homeActivity.presenter.openFragmentRight(
                        getFragment(
                            MainSignUpFragment()
                        ), AppConstants.TAG_MAIN_SIGN_UP2
                    )
                }
            }

            R.id.change_delivery_address -> {
                if (Engine().getOloAuthToken(mySharedPreferences)!!.isEmpty()) {
                    homeActivity.presenter.openFragmentRight(
                        getFragmentDelivery(
                            EnterDeliveryAddressFragment(),
                            OrderHelper.deliveryMode,
                            externalPartnerId
                        ),
                        AppConstants.TAG_ONLINE_ORDER
                    )
                } else {
                    homeActivity.presenter.openFragmentRight(
                        getFragmentDelivery(
                            DeliveryAddresesFragment(),
                            OrderHelper.deliveryMode,
                            externalPartnerId
                        ),
                        AppConstants.TAG_ONLINE_ORDER
                    )
                }
            }

            R.id.change_location -> {
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        ChangeOrderLocationFragment(),
                        AppConstants.TAG_ONLINE_ORDER
                    )
            }
        }
    }

    private fun getFragmentCarDetails(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY, true)
        fragment.arguments = bundle
        return fragment
    }

    fun afterLogin() {
        homeActivity.presenter.dismissProgress()
        if (!OrderHelper.flagChechForRewards) {
            Handler().postDelayed({
                homeActivity.presenter.showProgress()
                presenter.validateBasket(homeActivity, OrderHelper.basketId!!)
            }, 400)
        } else {
            homeActivity.presenter.openFragmentRight(
                LoyaltyRewadsFragment(),
                AppConstants.TAG_ONLINE_ORDER
            )
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

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        bundle.putBoolean(AppConstants.FROM_CONTACT_US, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessValidateBasket(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.flagChechForRewards = false

        OrderHelper.basket!!.salestax = response.tax
        OrderHelper.basket!!.total = response.total
        OrderHelper.basket!!.subtotal = response.subtotal

        homeActivity.presenter.openFragmentRight(
            getFragmentCheckout(
                CheckoutFragment(),
                externalPartnerId
            ),
            AppConstants.TAG_ONLINE_ORDER
        )
    }


    private fun getFragmentDelivery(
        fragment: BaseFragment,
        deliveryMode: String,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.DELIVERY_TYPE, deliveryMode)
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putSerializable(AppConstants.DELIVERY_ADDRESSES, null)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, true)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY, true)
        fragment.arguments = bundle
        return fragment
    }

    private fun getFragmentCheckout(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessDeleteProduct(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        initView()
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


    override fun onSuccessGetRestaurantCalendar(response: OLORestaurantCalendarResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.calendarRange = response
        showDateTimeDialog()
    }


    override fun onSuccessDeleteReward(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        OrderHelper.basket = response
        if (OrderHelper.basket!!.appliedrewards != null) {
            if (OrderHelper.basket!!.appliedrewards!!.isNotEmpty()) {
                reward_name.text = OrderHelper.basket!!.appliedrewards?.get(0)?.label!!
                layout_rewards.visibility = View.GONE
            } else {
                layout_rewards.visibility = View.VISIBLE
                applyed_rewad.visibility = View.GONE
            }
        } else {
            layout_rewards.visibility = View.VISIBLE
            applyed_rewad.visibility = View.GONE
        }
        initView()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showDateTimeDialog() {
        // Create the dialog
        val mDateTimeDialog = Dialog(homeActivity)
        val mDateTimeDialogView = layoutInflater
            .inflate(R.layout.date_time_dialog, null) as RelativeLayout
        val mDateTimePicker =
            mDateTimeDialogView.findViewById(R.id.DateTimePicker) as DateTimePicker

        (mDateTimeDialogView.findViewById(R.id.SetDateTime) as Button).setOnClickListener {
            mDateTimePicker.clearFocus()

            val c = Calendar.getInstance()
            c.time = mDateTimePicker.time
            c.set(Calendar.SECOND, c.getMinimum(Calendar.SECOND))

            calendar = c.time
            homeActivity.presenter.showProgress()
            presenter.addTimeWantedToBasket(
                homeActivity,
                OrderHelper.basketId!!,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH),
                c.get(Calendar.YEAR),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE)
            )

            btn_checkout.alpha = 1.0F
            btn_checkout.isEnabled = true
            btn_checkout.isClickable = true
            mDateTimeDialog.dismiss()
        }

        // Cancel the dialog when the "Cancel" button is clicked
        (mDateTimeDialogView.findViewById(R.id.CancelDialog) as Button).setOnClickListener {
            cbLater.setImageResource(R.drawable.red_radio_check_off)
            cbASAP.setImageResource(R.drawable.red_radio_check_off)
            btn_checkout.alpha = 0.5F
            btn_checkout.isEnabled = false
            btn_checkout.isClickable = false
            mDateTimeDialog.dismiss()
        }

        // Reset Date and Time pickers when the "Reset" button is clicked
        (mDateTimeDialogView.findViewById(R.id.ResetDateTime) as Button).setOnClickListener { mDateTimePicker.reset() }

        // Setup TimePicker
        mDateTimePicker.setIs24HourView(false)

        val cal = Calendar.getInstance()
        mDateTimePicker.setMinDate(cal.time)

        // No title on the dialog window
        // mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        val dialogTitle = "Select time and date"
        mDateTimeDialog.setTitle(dialogTitle)
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView)
        mDateTimeDialog.setCancelable(false)
        mDateTimeDialog.setCanceledOnTouchOutside(false)

        // Display the dialog
        mDateTimeDialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccessDeleteWantedTime(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()
        homeActivity.presenter.dismissProgress()
        OrderHelper.pickupTime = null
        OrderHelper.basket = response

        try {
            btn_checkout.alpha = 1F
            btn_checkout.isEnabled = true
            btn_checkout.isClickable = true
            cbLater.setImageResource(R.drawable.radio_check_off)
            cbASAP.setImageResource(R.drawable.ic_check_circle)
            asap_time.text =
                getString(R.string.order_asap) + " (" + OrderHelper.basket!!.leadtimeestimateminutes + " " + getString(
                    R.string.minutes
                ) + ")"
        } catch (er: NullPointerException) {
            homeActivity.presenter.dismissProgress()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccessAddTimewanted(response: OLOBasketResponse) {
        homeActivity.presenter.dismissProgress()

        if (calendar != null) {
            val dateFormat = SimpleDateFormat("E MMM dd", Locale.US)
            val dateFormat2 = SimpleDateFormat("hh:mm a", Locale.US)
            val fisrt = dateFormat.format(calendar!!.time)
            val second = dateFormat2.format(calendar!!.time)

            OrderHelper.pickupTime = "$fisrt at $second"
            txt_later.text = "$fisrt at $second"
            txt_later.visibility = View.VISIBLE
            change_time.visibility = View.VISIBLE
        }
        cbLater.setImageResource(R.drawable.ic_check_circle)
        cbASAP.setImageResource(R.drawable.radio_check_off)
        OrderHelper.basket = response
    }
}

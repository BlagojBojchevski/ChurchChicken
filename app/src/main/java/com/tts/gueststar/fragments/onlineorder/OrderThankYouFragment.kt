package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import kotlinx.android.synthetic.main.fragment_order_thankyou.*
import java.text.SimpleDateFormat
import java.util.*

class OrderThankYouFragment : BaseFragment(), View.OnClickListener {
    lateinit var homeActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFields()
        setListeners()
    }

    private fun setListeners() {
        btnClose.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun setFields() {

        btnClose.visibility = View.VISIBLE

        address.text = OrderHelper.location!!.address + "\n" + getString(
            R.string.location_state_text,
            OrderHelper.location!!.city_label,
            Engine().convertState(OrderHelper.location!!.state_label),
            OrderHelper.location!!.zipcode
        ) + "\n" + OrderHelper.location!!.phone_number.replace("(", "").replace(
            ")",
            ""
        ).replace(" ", "-")

        bottom_text.text = getString(R.string.order_number) + " " + OrderHelper.basket!!.oloid

        if (OrderHelper.deliveryMode == getString(R.string.mode_curbside) || OrderHelper.deliveryMode == getString(
                R.string.mode_pickup
            )
        ) {
            for_payment.text = getString(R.string.order_ready)
        } else {
            for_payment.text = getString(R.string.order_delivery)
        }
        val dateFormat1 = SimpleDateFormat("yyyyMMdd hh:mm", Locale.US)
        val date = dateFormat1.parse(OrderHelper.basket!!.readytime)
        val dateFormat = SimpleDateFormat("E MMM dd", Locale.US)
        val dateFormat2 = SimpleDateFormat("hh:mm a", Locale.US)
        val fisrt = dateFormat.format(date.time)
        val second = dateFormat2.format(date.time)
        checkTotal.text = "$fisrt at $second"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_order_thankyou, container, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                homeActivity.clearOrderHelper()
                homeActivity.clearStackOnlineOrderAndLocations()
            }
        }
    }

}

package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.tts.gueststar.R
import android.widget.TextView
import com.tts.gueststar.interfaces.OrderHistoryItemsInterface
import com.tts.olosdk.models.Order
import kotlinx.android.synthetic.main.item_modifiers_histiry.view.*
import kotlinx.android.synthetic.main.item_order_history.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    val context: Context,
    private val orders: List<Order>,
    private val listener: OrderHistoryItemsInterface
) : RecyclerView.Adapter<OrderHistoryAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_order_history,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(orders[position], context)
        val order = orders[position]
        holder.btnEdit.setOnClickListener {
            listener.reorder(order)
        }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderAddressTime = itemView.order_address_time
        private val orderTotal: TextView = itemView.order_total
        private val address = itemView.address
        private val orderMode = itemView.order_mode
        val parent: LinearLayout = itemView.order_items

        val btnEdit: RelativeLayout = itemView.btn_reorder


        @SuppressLint("SetTextI18n", "InflateParams")
        fun init(order: Order, context: Context) {
            val dateFormat1 = SimpleDateFormat("yyyyMMdd hh:mm", Locale.US)
            val date = dateFormat1.parse(order.readytime)
            val dateFormat = SimpleDateFormat("E MMM dd", Locale.US)
            val dateFormat2 = SimpleDateFormat("hh:mm a", Locale.US)
            val fisrt = dateFormat.format(date.time)
            val second = dateFormat2.format(date.time)
            orderAddressTime.text = "$fisrt at $second"
            address.text = order.vendorname

            when {
                order.deliverymode == context.getString(R.string.mode_pickup) -> orderMode.text =
                    context.getString(R.string.pickup)
                order.deliverymode == context.getString(R.string.mode_curbside) -> orderMode.text =
                    context.getString(R.string.catering)
                else -> orderMode.text = context.getString(R.string.delivery)
            }

            parent.removeAllViews()
            for (item in order.products) {
                val cellViewCustomizeLayout = LayoutInflater.from(context).inflate(
                    R.layout.item_modifiers_histiry,
                    null
                ) as RelativeLayout

                cellViewCustomizeLayout.mod_quantity.text = item.quantity.toString()
                cellViewCustomizeLayout.mod_name.text = item.name
                cellViewCustomizeLayout.mod_price.text = "$" + String.format("%.2f", item.totalcost)
                parent.addView(cellViewCustomizeLayout)
            }


            orderTotal.text = context.getString(R.string.total_sum) + String.format(
                "%.2f",
                order.total
            )
        }
    }
}
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
import com.tts.gueststar.interfaces.OrderHistoryItemsFavesInterface
import com.tts.olosdk.models.Fave
import kotlinx.android.synthetic.main.item_modifiers_histiry.view.*
import kotlinx.android.synthetic.main.item_order_history_faves.view.*

class OrderHistoryAdapterFaves(
    val context: Context,
    val orders: List<Fave>,
    private val listener: OrderHistoryItemsFavesInterface
) : RecyclerView.Adapter<OrderHistoryAdapterFaves.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_order_history_faves,
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

        holder.btnRemove.setOnClickListener {
            listener.remove(order)
        }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val address = itemView.fav_order_restaurant
        private val name = itemView.fav_order_name
        val parent: LinearLayout = itemView.order_items

        val btnEdit: RelativeLayout = itemView.btn_reorder
        val btnRemove: RelativeLayout = itemView.btn_remove


        @SuppressLint("SetTextI18n", "InflateParams")
        fun init(order: Fave, context: Context) {
            btnRemove.visibility = View.VISIBLE
            address.text = order.vendorname
            name.text = order.name

            parent.removeAllViews()
            for (item in order.products) {
                val cellViewCustomizeLayout = LayoutInflater.from(context).inflate(
                    R.layout.item_modifiers_histiry,
                    null
                ) as RelativeLayout

                cellViewCustomizeLayout.mod_quantity.text = item.quantity.toString()
                cellViewCustomizeLayout.mod_name.text = item.name
                if(item.totalcost>0.0) {
                    cellViewCustomizeLayout.mod_price.text =
                        "$" + String.format("%.2f", item.totalcost)
                }
                parent.addView(cellViewCustomizeLayout)
            }
        }
    }
}
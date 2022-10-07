package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.OrderSummaryItemsInterface
import com.tts.olosdk.models.Product1
import kotlinx.android.synthetic.main.item_order_summary.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import com.tts.gueststar.utility.OrderHelper
import kotlinx.android.synthetic.main.item_modifiers.view.*

class OrderSummaryAdapter(
    val context: Context,
    private val products: List<Product1>,
    private val listener: OrderSummaryItemsInterface
) : RecyclerView.Adapter<OrderSummaryAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_order_summary,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(products[position], context)
        val restaurant = products[position]
        holder.btnShowDetails.setOnClickListener {
            if (holder.parent.visibility == View.VISIBLE) {
                holder.parent.visibility = View.GONE
                holder.submodifiers.visibility = View.GONE
                holder.btnShowDetails.text = context.getString(R.string.show_details)
            } else {
                if (restaurant.specialinstructions.isNotEmpty())
                    holder.submodifiers.visibility = View.VISIBLE
                holder.parent.visibility = View.VISIBLE
                holder.btnShowDetails.text = context.getString(R.string.hide_details)
            }
//            listener.callRestaurant(restaurant.phone_number)
        }

        holder.btnEdit.setOnClickListener {
            listener.editItem(restaurant)
        }

        holder.btnRemove.setOnClickListener {
            listener.removeItem(restaurant)
        }
    }


    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName = itemView.product_name
        private val productPrice = itemView.product_price
        val submodifiers: TextView = itemView.submodifiers
        val parent: LinearLayout = itemView.parent_layout

        val btnShowDetails: TextView = itemView.btn_show_details
        val btnEdit: TextView = itemView.btn_edit
        val btnRemove: TextView = itemView.btn_remove


        @SuppressLint("SetTextI18n", "InflateParams")
        fun init(product1: Product1, context: Context) {

            if (product1.choices.isEmpty() && product1.specialinstructions.isEmpty()) {
                btnShowDetails.visibility = View.GONE
            }
            productName.text = product1.quantity.toString() + "x " + product1.name
            productPrice.text = "$" + String.format("%.2f", product1.totalcost)
            btnShowDetails.paintFlags =
                btnShowDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            btnEdit.paintFlags =
                btnEdit.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            btnRemove.paintFlags =
                btnRemove.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            for (item in product1.choices) {
                val cellViewCustomizeLayout = LayoutInflater.from(context).inflate(
                    R.layout.item_modifiers,
                    null
                ) as RelativeLayout
                cellViewCustomizeLayout.mod_name.text = item.name
                for (keys in OrderHelper.quantityModifiers) {
                    if (keys.value != 0) {
                        if (keys.key == item.optionid) {
                            if (keys.value == 1) {
                                cellViewCustomizeLayout.mod_price.text =
                                    "$" + String.format("%.2f", item.cost)
                            } else {
                                cellViewCustomizeLayout.mod_name.text =
                                    keys.value.toString() + "x " + item.name
                            }
                        }
                    }
                }


                cellViewCustomizeLayout.mod_price.text = "$" + String.format("%.2f", item.cost)

                parent.addView(cellViewCustomizeLayout)
            }
            if (product1.specialinstructions.isNotEmpty()) {
                submodifiers.text =
                    context.getString(R.string.special_instructions1) + " - " + product1.specialinstructions
            }

        }
    }
}
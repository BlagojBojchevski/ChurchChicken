package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.CreditCardInterface
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.Billingaccount
import kotlinx.android.synthetic.main.item_gift_card.view.*

class GiftCardsAdapter(
    val context: Context,
    private val billingaccount: List<Billingaccount>,
    private val listener: CreditCardInterface
) : RecyclerView.Adapter<GiftCardsAdapter.LocationViewHolder>() {

    companion object {
        var selectedItem = -1
        val TAG_UNSELECTED = 0
        val TAG_SELECTED = 1

        fun getItemViewTypeAA(position: Int): Int {
            return if (position == selectedItem) TAG_SELECTED else TAG_UNSELECTED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
      //  selectItem(OrderHelper.selectedGiftPosition)
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_gift_card,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return billingaccount.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(billingaccount[position], context, position)

        val billingAccount = billingaccount[position]
        holder.btnItem.setOnClickListener {
            selectItem(position)
            holder.checkbox.setBackgroundResource(R.drawable.red_radio_check_on)
            OrderHelper.selectedGiftPosition = position
            listener.chooseCreditCard(billingAccount)
        }

        holder.deleteGiftCard.setOnClickListener {
            listener.removeCreditCard(billingAccount)
        }


    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    fun selectItem(i: Int) {
//        if(selectedItem !=0)
        selectedItem = i
        notifyDataSetChanged()
    }


    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val giftCard = itemView.gift_card
        val btnItem = itemView.btn_item
        val deleteGiftCard = itemView.delete_gift_card
        val checkbox = itemView.checkbox


        @SuppressLint("SetTextI18n")
        fun init(
            product: Billingaccount,
            context: Context,
            position: Int
        ) {
            val type = getItemViewTypeAA(position)
            if (type == TAG_SELECTED) {
                checkbox.setBackgroundResource(R.drawable.red_radio_check_on)
            } else {
                checkbox.setBackgroundResource(R.drawable.red_radio_check_off)
            }

            giftCard.text =
                context.getString(
                    R.string.ending_with_field_summary,
                    product.cardsuffix
                ) + " $" + String.format("%.2f", product.balance.balance)

        }

    }

}
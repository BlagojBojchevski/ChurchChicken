package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.CreditCardInterface
import com.tts.olosdk.models.Billingaccount
import kotlinx.android.synthetic.main.item_credit_card.view.*

@Suppress("DEPRECATION")
class CreditCardsAdapter(
    val context: Context,
    private val billingaccount: List<Billingaccount>,
    private val listener: CreditCardInterface
) : RecyclerView.Adapter<CreditCardsAdapter.LocationViewHolder>() {

    companion object {
        var selectedItem = -1
        private const val TAG_UNSELECTED = 0
        const val TAG_SELECTED = 1

        fun getItemViewTypeAA(position: Int): Int {
            return if (position == selectedItem) TAG_SELECTED else TAG_UNSELECTED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_credit_card,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return billingaccount.size
    }


    private fun selectItem(i: Int) {
//        if(selectedItem !=0)
        selectedItem = i
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(billingaccount[position], context)
        val billingAccount = billingaccount[position]
        holder.btnItem.setOnClickListener {
            selectItem(position)
            listener.chooseCreditCard(billingAccount)
        }

        holder.deleteCreditCard.setOnClickListener {
            listener.removeCreditCard(billingAccount)
        }


    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val creditCard = itemView.credit_card
        private val cardType = itemView.card_type
        val btnItem: LinearLayout = itemView.btn_item
        val deleteCreditCard: ImageView = itemView.delete_credit_card
        private val checkbox = itemView.checkbox

        @SuppressLint("SetTextI18n")
        fun init(
            product: Billingaccount,
            context: Context
        ) {

            val type = getItemViewTypeAA(position)
            if (type == TAG_SELECTED) {
                checkbox.setBackgroundResource(R.drawable.ic_check_circle)
            } else {
                checkbox.setBackgroundResource(R.drawable.radio_check_off)
            }
            creditCard.text =
                context.getString(
                    R.string.ending_with_field_summary,
                    product.cardsuffix
                )

            when (product.cardtype) {
                "Discover" -> cardType.setImageResource(R.drawable.card_doscover)
                "Mastercard" -> cardType.setImageResource(R.drawable.card_mastercard)
                "Visa" -> cardType.setImageResource(R.drawable.card_visa)
                "Amex" -> {
                    creditCard.text =
                        context.getString(
                            R.string.ending_with_field_summary,
                            product.cardsuffix
                        ).replace("-", "")
                    cardType.setImageResource(R.drawable.card_amex)
                }
            }
        }

    }

}
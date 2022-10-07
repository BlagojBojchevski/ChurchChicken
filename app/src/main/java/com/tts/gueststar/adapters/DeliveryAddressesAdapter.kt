package com.tts.gueststar.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ChooseDeliveryAddressInterface
import com.tts.olosdk.models.Deliveryaddresse

import kotlinx.android.synthetic.main.item_delivery_address.view.*

class DeliveryAddressesAdapter(
    val context: Context,
    private val deliveryAddresses: List<Deliveryaddresse>,
    private val listener: ChooseDeliveryAddressInterface
) : RecyclerView.Adapter<DeliveryAddressesAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_delivery_address,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return deliveryAddresses.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(deliveryAddresses[position])
        val address = deliveryAddresses[position]
        holder.btn_address.setOnClickListener {
            listener.chooseDeliveryAddress(address)
        }
        holder.delete_address.setOnClickListener {
            listener.deleteDeliveryAddress(address)
        }

    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressName = itemView.txt_address
        val btn_address = itemView.btn_address
        val delete_address = itemView.delete_address


        fun init(address: Deliveryaddresse) {
            addressName.text = address.streetaddress


        }
    }
}
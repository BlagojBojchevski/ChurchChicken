package com.tts.gueststar.adapters

import android.content.Context
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.LocationActionsInterface
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.inflater_locations.view.*

class LocationsAdapter(
    val context: Context,
    private val locations: List<LocationsResponse.Location>,
    private val listener: LocationActionsInterface
)
    : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(LayoutInflater.from(context).inflate(R.layout.inflater_locations, parent, false))
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(locations[position], context)
        val restaurant = locations[position]
        holder.btnCall.setOnClickListener {
            listener.callRestaurant(restaurant.phone_number)
        }

        holder.btnDirections.setOnClickListener {
            listener.directions(restaurant.latitude, restaurant.longitude)
        }

        holder.btnOrder.setOnClickListener {
            listener.orderOnline(restaurant)
        }

        holder.btnDelivery.setOnClickListener {
            if(restaurant.delivery_info!!.isNotEmpty())
            listener.openWebView(restaurant.delivery_info!![0].delivery_url,restaurant.delivery_info!![0].name)
        }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationName = itemView.tvLocationName
        private val locationAdress = itemView.tvLocationAddress
        private val locationState = itemView.tvLocationState
        private val locationHours = itemView.tvLocationHours
        private val locationDistance = itemView.tvLocationDistance
        val btnCall: LinearLayout = itemView.layoutCall
        val btnOrder: LinearLayout = itemView.layoutOrder
        val btnDirections: LinearLayout = itemView.layoutDirections
        val tvDelivery = itemView.tvDelivery
        val btnDelivery = itemView.layoutDelivery


        fun init(location: LocationsResponse.Location, context: Context){
            locationName.text = location.app_display_text
            locationAdress.text = location.address
            locationState.text = context.getString(R.string.location_state_text, location.city_label, Engine().convertState(location.state_label), location.zipcode )
            locationHours.text = context.getString(R.string.location_hours_text, location.today_open_hour!!.open_at, location.today_open_hour!!.close_at)
            locationDistance.text = context.getString(R.string.location_distance_text, location.restaurant_distance.toString())

            if(!location.online_order_support_status){
                btnOrder.visibility = View.INVISIBLE
                btnOrder.isEnabled = false
                btnDelivery.visibility = View.VISIBLE
                if(location.delivery_info!!.isNotEmpty())
                tvDelivery.text = location.delivery_info!![0].name
            } else {
                btnOrder.visibility = View.VISIBLE
                btnOrder.isEnabled = true
                btnDelivery.visibility = View.INVISIBLE
            }
            
            tvDelivery.paintFlags =
                tvDelivery.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            if(Engine().checkEnableGPS(context)) {
                if (location.restaurant_distance == 0.0)
                    locationDistance.visibility = View.GONE
                else locationDistance.visibility = View.VISIBLE
            } else locationDistance.visibility = View.GONE
        }
    }
}
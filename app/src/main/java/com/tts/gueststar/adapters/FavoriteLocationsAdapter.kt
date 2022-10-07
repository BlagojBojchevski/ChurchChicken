package com.tts.gueststar.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.OnFavLocationPicked
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.inflater_favorite_locations.view.*

class FavoriteLocationsAdapter(
    val context: Context,
    private val locations: List<LocationsResponse.Location>,
    private val listener: OnFavLocationPicked
) : RecyclerView.Adapter<FavoriteLocationsAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.inflater_favorite_locations,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(locations[position], context)
        holder.itemView.setOnClickListener {
            listener.onFavLocationPicked(locations[position],locations[position].app_display_text, locations[position].id)
        }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationName = itemView.tvLocationName
        private val locationAdress = itemView.tvLocationAddress
        private val locationState = itemView.tvLocationState
        private val locationHours = itemView.tvLocationHours
        private val locationDistance = itemView.tvLocationDistance


        fun init(location: LocationsResponse.Location, context: Context) {
            locationName.text = location.app_display_text
            locationAdress.text = location.address
            locationState.text = context.getString(
                R.string.location_state_text,
                location.city_label,
                Engine().convertState(location.state_label),
                location.zipcode
            )
            locationHours.text = context.getString(
                R.string.location_hours_text,
                location.today_open_hour!!.open_at,
                location.today_open_hour!!.close_at
            )
            locationDistance.text =
                context.getString(R.string.location_distance_text, location.restaurant_distance.toString())
            if (Engine().checkEnableGPS(context)) {
                if (location.restaurant_distance == 0.0)
                    locationDistance.visibility = View.GONE
                else locationDistance.visibility = View.VISIBLE
            } else locationDistance.visibility = View.GONE
        }
    }
}
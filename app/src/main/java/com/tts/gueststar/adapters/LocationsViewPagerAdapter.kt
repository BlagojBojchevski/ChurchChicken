package com.tts.gueststar.adapters

import android.content.Context
import android.graphics.Paint
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.LocationActionsInterface
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.inflater_locations_viewpager.view.*
import java.util.*
import kotlin.collections.ArrayList

class LocationsViewPagerAdapter(
    private var context: Context,
    list: ArrayList<LocationsResponse.Location>,
    private var listener: LocationActionsInterface
) : PagerAdapter() {

    private var locationsList = list
    private var layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.inflater_locations_viewpager, container, false)

        view.tvLocationsMapName.text = locationsList[position].app_display_text

        view.tvLocationsMapOrder.paintFlags =
            view.tvLocationsMapOrder.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.tvLocationsMapDelivery.paintFlags =
            view.tvLocationsMapDelivery.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.tvLocationsMapAddress.text = locationsList[position].address
        view.tvLocationsMapCityState.text = context.getString(
            R.string.location_state_text,
            locationsList[position].city_label,
            Engine().convertState(locationsList[position].state_label),
            locationsList[position].zipcode
        )
        view.tvLocationsMapOpenedHours.text = context.getString(
            R.string.location_hours_text,
            locationsList[position].today_open_hour!!.open_at,
            locationsList[position].today_open_hour!!.close_at
        )
        view.tvLocationsMapDistance.text = context.getString(
            R.string.location_distance_text2,
            locationsList[position].restaurant_distance.toString()
        )
        if (Engine().checkEnableGPS(context))
            view.tvLocationsMapDistance.visibility = View.VISIBLE
        else view.tvLocationsMapDistance.visibility = View.GONE

        if(!locationsList[position].online_order_support_status){
            view.tvLocationsMapOrder.visibility = View.GONE
            view.tvLocationsMapOrder.isEnabled = false
        }

        if(locationsList[position].delivery_info != null) {
            if (locationsList[position].delivery_info!!.isNotEmpty()) {
                view.tvLocationsMapDelivery.visibility = View.VISIBLE
                view.tvLocationsMapDelivery.text =locationsList[position].delivery_info!![0].name
            }
        }


        if (Engine().checkEnableGPS(context)) {
            if (locationsList[position].restaurant_distance == 0.0)
                view.tvLocationsMapDistance.visibility = View.GONE
            else view.tvLocationsMapDistance.visibility = View.VISIBLE
        } else view.tvLocationsMapDistance.visibility = View.GONE


        view.tvLocationsMapDirections.setOnClickListener {
            listener.directions(locationsList[position].latitude, locationsList[position].longitude)
        }

        view.tvLocationsMapCall.setOnClickListener {
            listener.callRestaurant(locationsList[position].phone_number)
        }

        view.tvLocationsMapOrder.setOnClickListener {
            listener.orderOnline(locationsList[position])
        }

        view.tvLocationsMapDelivery.setOnClickListener {
            if(locationsList[position].delivery_info != null){
                if (locationsList[position].delivery_info!!.isNotEmpty()){
                    listener.openWebView(locationsList[position].delivery_info!![0].delivery_url,locationsList[position].delivery_info!![0].name)
                }
            }

        }



        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as ConstraintLayout)
    }


    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return locationsList.size
    }
}
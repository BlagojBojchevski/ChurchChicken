package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.models.LoyaltyHistory
import kotlinx.android.synthetic.main.item_loyalty_history.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class LoyaltyHistoryAdapter(
    val context: Context,
    private val items: MutableList<LoyaltyHistory>
) : RecyclerView.Adapter<LoyaltyHistoryAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_loyalty_history,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(items[position], context)
    }

    @Suppress("DEPRECATION")
    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.txt_name_or_restaurant
        private val date = itemView.txt_date
        private val points = itemView.txt_points
        private val price = itemView.txt_price


        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun init(item: LoyaltyHistory, context: Context) {
            if (Build.VERSION.SDK_INT < 23) {
                points.setTextAppearance(context, R.style.TextView22)
            } else {
                points.setTextAppearance(R.style.TextView22)
            }
            name.text = item.name
            points.text = "+" + item.points.toString() + " Points"

            if (item.name.isEmpty()) {
                name.visibility = View.GONE
            } else {
                name.visibility = View.VISIBLE
            }

            val stamp = Timestamp(item.date)
            val d = Date(stamp.time * 1000)
            val f = SimpleDateFormat("MM/dd/yyyy")
            f.timeZone = TimeZone.getTimeZone("GMT")
            date.text = f.format(d).toString()

            when {
                item.activity_type == "receipts" -> {
                    price.visibility = View.VISIBLE
                    price.text = "$" + item.subtotal
//                    if (Build.VERSION.SDK_INT < 23) {
//                        points.setTextAppearance(context, R.style.TextView22RedRegulas)
//                    } else {
//                        points.setTextAppearance(R.style.TextView22RedRegulas)
//                    }
                }
                item.activity_type == "rewards" -> {
                    if (Build.VERSION.SDK_INT < 23) {
                        points.setTextAppearance(context, R.style.TextView22RedRegulas)
                    } else {
                        points.setTextAppearance(R.style.TextView22RedRegulas)
                    }
                    if (item.points == 0) {
                        points.text = context.getString(R.string.rewards_free)
                    } else {
                        if (Build.VERSION.SDK_INT < 23) {
                            points.setTextAppearance(context, R.style.TextView22RedRegulas)
                        } else {
                            points.setTextAppearance(R.style.TextView22RedRegulas)
                        }
                        points.text = "-" + item.points.toString() + " Points"
                    }
                    price.visibility = View.GONE
                    price.text = ""
                }
                else -> {
                    price.visibility = View.GONE
                    price.text = ""
                }
            }
        }
    }
}
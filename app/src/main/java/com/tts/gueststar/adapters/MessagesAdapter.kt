package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.com.relevantsdk.sdk.models.Notification_
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.OnMessageClicked
import kotlinx.android.synthetic.main.message_item.view.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MessagesAdapter(
    val context: Context,
    private val items: List<Notification_>,
    private val listener: OnMessageClicked
) : RecyclerView.Adapter<MessagesAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.message_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(items[position])
        holder.itemView.setOnClickListener {
            listener.onMessageClick(items[position])
        }
    }

    @Suppress("DEPRECATION")
    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val message = itemView.text_message
        private val date = itemView.date_message
        private val icon = itemView.icon


        @SuppressLint("SimpleDateFormat", "SetTextI18n", "DefaultLocale")
        fun init(item: Notification_) {
            message.text = item.notification_text
            val monthString = SimpleDateFormat("MMM").format(Date(item.created_at * 1000))
            val dayString = SimpleDateFormat("dd").format(Date(item.created_at * 1000))
            date.text = monthString.toUpperCase() + "\n" + dayString

            if (item.notification_read_status) {
                icon.setImageResource(R.drawable.icon_read)
            } else {
                icon.setImageResource(R.drawable.icon_unread)
            }

        }
    }
}
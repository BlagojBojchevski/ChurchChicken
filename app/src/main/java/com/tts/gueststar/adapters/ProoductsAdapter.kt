package com.tts.gueststar.adapters

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ChooseItemInterface
import com.tts.gueststar.utility.OrderHelper
import com.tts.gueststar.utility.RoundedCornersTransformation
import com.tts.olosdk.models.Product
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_product.view.*

class ProoductsAdapter(
    val context: Context,
    private val imagePath: String,
    private val products: List<Product>,
    private val listener: ChooseItemInterface
) : RecyclerView.Adapter<ProoductsAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_product,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(products[position], context, imagePath)
        val menuCategory = products[position]
        holder.btn_categories.setOnClickListener {
            listener.chooseItemCategory(menuCategory)
        }

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_name = itemView.item_name
        val item_price = itemView.item_price
        val item_image = itemView.item_image
        val item_image1 = itemView.item_image1
        val item_cal = itemView.item_cal
        val btn_categories = itemView.btn_categories

        @SuppressLint("SetTextI18n")
        fun init(
            product: Product,
            context: Context,
            imagePath: String
        ) {
            if (OrderHelper.showCalories) {
                if (product.caloriesseparator != null) {
                    item_cal.text =
                        product.basecalories + product.caloriesseparator + product.maxcalories + " Cal"
                } else {
                    if (product.basecalories != null) {
                        item_cal.text = product.basecalories + " Cal"
                    }
                }
            } else {
                item_cal.visibility = View.GONE
            }

            item_name.text = product.name

            if (product.cost > 0)
                item_price.text = "$" + product.cost
            else
                item_price.visibility = View.GONE

            if (product.images != null)
                if (product.images!!.isNotEmpty()) {
                    for (item in product.images!!) {
                        if (item.groupname == "mobile-app") {
                            val radius = context.resources.getDimension(R.dimen.dimen_2).toInt()
                            val transformation = RoundedCornersTransformation(radius, 0)
                            Picasso.get().load(imagePath + item.filename).transform(transformation)
                                .into(item_image)
                            item_image1.visibility = View.GONE
                        }
                    }
                }
        }
    }
}
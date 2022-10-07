package com.tts.gueststar.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ChooseMenuCategoryInterface
import com.tts.olosdk.models.Category
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_main_manu.view.*

class MainMenuAdapter(
    val context: Context,
    val imagePath: String,
    private val products: List<Category>,
    private val listener: ChooseMenuCategoryInterface
) : RecyclerView.Adapter<MainMenuAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_main_manu,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(products[position], imagePath)
        val menuCategory = products[position]
        holder.btn_categories.setOnClickListener {
            listener.chooseMenuCategory(menuCategory, position)
        }

    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_name = itemView.item_name
        val item_image = itemView.item_image
        val btn_categories = itemView.btn_categories

        fun init(product: Category, imagePath: String) {
            item_name.text = product.name
            if (product.images != null)
                if (product.images!!.isNotEmpty()) {
                    for (item in product.images!!) {
                        if (item.groupname == "mobile-app") {
                            Picasso.get().load(imagePath + item.filename).into(item_image)
                        }
                    }
                }
        }
    }
}
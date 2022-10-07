package com.tts.gueststar.adapters

import android.content.Context
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ChooseMenuCategorySubMenuInterface
import com.tts.olosdk.models.Category
import kotlinx.android.synthetic.main.item_main_menu_sub.view.*

class MenuCategoriesAdapter(
    val context: Context,
    private val products: List<Category>,
    private val listener: ChooseMenuCategorySubMenuInterface,
    var currentPosition: Long
) : RecyclerView.Adapter<MenuCategoriesAdapter.LocationViewHolder>() {

    private var selectedItem = 0
    private val TAG_UNSELECTED = 0
    private val TAG_SELECTED = 1

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_main_menu_sub,
                parent,
                false
            )
        )
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    private fun selectItem(i: Int) {
        selectedItem = i
        notifyDataSetChanged()
    }


    private fun getItemViewTypeE(position: Int): Int {
        return if (position == selectedItem) TAG_SELECTED else TAG_UNSELECTED
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.init(products[position], currentPosition)
        val menuCategory = products[position]
        holder.btn_categories.setOnClickListener {
            listener.chooseMenuCategory(menuCategory, position)
            selectItem(position)
            holder.line.visibility = View.VISIBLE
            TextViewCompat.setTextAppearance(holder.item_name, R.style.TextViewRedBold18)
            currentPosition = products[position].id
        }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_name = itemView.item_name
        val line = itemView.line_e
        val btn_categories = itemView.btn_categories

        fun init(product: Category, position: Long) {
            if (position == product.id) {
                TextViewCompat.setTextAppearance(item_name, R.style.TextView18)
                line.visibility = View.VISIBLE
            } else {
                TextViewCompat.setTextAppearance(item_name, R.style.TextView18Unselected)
                line.visibility = View.INVISIBLE
            }
            item_name.text = product.name

        }

    }
}
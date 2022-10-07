package com.tts.gueststar.adapters

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.OpenHyperlinkListener
import com.squareup.picasso.Picasso

import java.util.ArrayList

class ImageSliderAdapter(context: Context, private val images: ArrayList<String>,private val urls: ArrayList<String>) : PagerAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val contextt = context

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val myImageLayout = inflater.inflate(R.layout.slide, view, false)
        val myImage = myImageLayout
            .findViewById<View>(R.id.image_slider) as ImageView
        Picasso.get()
            .load(images[position])
            .placeholder(R.drawable.carousel_default)
            .into(myImage)
        view.addView(myImageLayout, 0)
        myImageLayout.setOnClickListener{
            (contextt as OpenHyperlinkListener).openHyperlink(urls[position])
        }
        return myImageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}
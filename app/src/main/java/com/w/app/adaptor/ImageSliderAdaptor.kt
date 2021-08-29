package com.w.app.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.w.app.R
import java.util.*


class ImageSliderAdaptor(var context: Activity, private val arrayList: ArrayList<*>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {

        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.row_imageslider, view, false) as ViewGroup
        val mImageView = itemView.findViewById<ImageView>(R.id.ivBackground)
        Glide.with(context)
            .load(arrayList[position])
            .placeholder(R.drawable.temp)
            .centerCrop()
            .into(mImageView)

        (view as ViewPager).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View?)
    }
}
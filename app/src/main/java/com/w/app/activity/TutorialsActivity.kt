package com.w.app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.w.app.R
import com.w.app.base.BaseActivity
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_tutorial.*
import kotlinx.android.synthetic.main.activity_tutorial.viewPager


class TutorialsActivity:BaseActivity() {
    var imagelist:ArrayList<Drawable>?=null
    override fun setLayout(): Int {
       return R.layout.activity_tutorial
    }

    override fun InitView() {
        SharePreference.setBooleanPref(this@TutorialsActivity, SharePreference.isTutorial,true)
        imagelist = ArrayList()
        imagelist!!.add(resources.getDrawable(R.drawable.ic_slider1))
        imagelist!!.add(resources.getDrawable(R.drawable.ic_slider2))
        imagelist!!.add(resources.getDrawable(R.drawable.ic_slider3))
        imagelist!!.add(resources.getDrawable(R.drawable.ic_slider4))
        imagelist!!.add(resources.getDrawable(R.drawable.ic_slider5))
        viewPager.adapter=StartScreenAdapter(this@TutorialsActivity,imagelist!!)
        tabLayout.setupWithViewPager(viewPager, true)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                if (i == imagelist!!.size - 1) {
                    tvBtnSkip.text="Start"
                }else{
                    tvBtnSkip.text="Skip"
                }
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })

        tvBtnSkip.setOnClickListener {
            openActivity(LoginActivity::class.java)
            finish()
        }
    }

    class StartScreenAdapter(var mContext: Context, var mImagelist: ArrayList<Drawable>) : PagerAdapter() {
        @SuppressLint("SetTextI18n")
        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(mContext)
            val layout = inflater.inflate(R.layout.row_tutorial, collection, false) as ViewGroup
            val iv: ImageView = layout.findViewById(R.id.ivScreen)
            iv.setImageDrawable(mImagelist[position])
            collection.addView(layout)
            return layout
        }

        override fun destroyItem(
            collection: ViewGroup,
            position: Int,
            view: Any
        ) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return mImagelist.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}
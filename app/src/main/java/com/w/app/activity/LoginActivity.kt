package com.w.app.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import com.w.app.R
import com.w.app.adaptor.ImageSliderAdaptor
import com.w.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import kotlin.collections.ArrayList


class LoginActivity:BaseActivity() {

    var timer: Timer?=null
    private var currentPage = 0
    private var imagelist:ArrayList<Drawable>?=null

    override fun setLayout(): Int {
        return R.layout.activity_login
    }

    override fun InitView() {
        imagelist= ArrayList()
        imagelist!!.add(resources.getDrawable(R.drawable.temp))
        imagelist!!.add(resources.getDrawable(R.drawable.temp1))
        imagelist!!.add(resources.getDrawable(R.drawable.temp3))
        imagelist!!.add(resources.getDrawable(R.drawable.temp4))
        imagelist!!.add(resources.getDrawable(R.drawable.temp5))
        loadPagerImages(imagelist!!)
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvLogin -> {
                openActivity(LoginMainActivity::class.java)
            }
            R.id.tvRegistration->{
                startActivity(Intent(this@LoginActivity,RegistrationActivity::class.java).putExtra("type","login"))
                finish()
            }
            R.id.tvBtnSkip->{
                openActivity(DashboardActivity::class.java)
                finish()
            }
        }
    }

    private fun loadPagerImages(imageHase: ArrayList<*>) {
        viewPager.setAdapter(ImageSliderAdaptor(this@LoginActivity, imageHase))
        val handler = Handler()

        val Update = Runnable {
            if (currentPage == imageHase.size) {
                currentPage = 0
            }
            viewPager.setCurrentItem(currentPage++, true)
        }

        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            // task to be scheduled
            override fun run() {
                handler.post(Update)
            }
        }, 500, 3000)


    }

    override fun onPause() {
        super.onPause()
        if(timer!=null)
            timer!!.cancel()

    }

    override fun onResume() {
        super.onResume()
        timer = Timer()
        val handler = Handler()
        val Update = Runnable {
            if (currentPage == imagelist!!.size) {
                currentPage = 0
            }
            viewPager.setCurrentItem(currentPage++, true)
        }
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post(Update)
            }
        }, 7000, 5000)
    }


}
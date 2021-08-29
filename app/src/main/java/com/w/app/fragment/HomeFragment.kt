package com.w.app.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.*
import com.w.app.R
import com.w.app.activity.DashboardActivity
import com.w.app.base.BaseFragmnet
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment:BaseFragmnet() {
    var temp=1
    override fun setView(): Int {
       return R.layout.fragment_home
    }
    override fun Init(view: View) {
        setWallPaper(1)
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        llLatest.setOnClickListener {
            if(temp!=1){
                setWallPaper(1)
                temp=1
            }
        }
        llTranding.setOnClickListener {
            if(temp!=2){
                setWallPaper(2)
                temp=2
            }
        }
        setAds()
    }

    @SuppressLint("WrongConstant")
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FramFragment, fragment)
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
        fragmentTransaction.commit()
    }

    fun setWallPaper(pos:Int){
        viewLatest.setBackgroundColor(resources.getColor(R.color.white))
        viewTranding.setBackgroundColor(resources.getColor(R.color.white))
        when (pos){
            1->{
                viewLatest.setBackgroundColor(resources.getColor(R.color.dark_gray))
                replaceFragment(LatestFragment())
            }
            2->{
                viewTranding.setBackgroundColor(resources.getColor(R.color.dark_gray))
                replaceFragment(TrandingFragment())
            }
        }
    }

    private fun setAds() {
        MobileAds.initialize(activity, "ca-app-pub-8854584886196021/1204732589")
        val adRequest =
            AdRequest.Builder().build()
        ad.loadAd(adRequest)
        ad.setAdListener(object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(errorCode: Int) {
                ad.setVisibility(View.GONE)
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        })
        val adView = AdView(activity)
        adView.adSize = AdSize.SMART_BANNER
    }
}
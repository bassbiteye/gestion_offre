package com.w.app.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.w.app.R
import com.w.app.activity.AddWallpaperActivity
import com.w.app.activity.DashboardActivity
import com.w.app.base.BaseFragmnet
import com.w.app.utils.Common
import kotlinx.android.synthetic.main.fragment_mywallpaper.*
import kotlinx.android.synthetic.main.fragment_mywallpaper.ivMenu

class MyWallpaperFragment:BaseFragmnet() {
    var temp=1
    override fun setView(): Int {
       return R.layout.fragment_mywallpaper
    }

    override fun Init(view: View) {
        setWallPaper(1)
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        llPending.setOnClickListener {
            if(temp!=1){
                setWallPaper(1)
                temp=1
            }
        }
        llApproved.setOnClickListener {
            if(temp!=2){
                setWallPaper(2)
                temp=2
            }
        }
        llReject.setOnClickListener {
            if(temp!=3){
                setWallPaper(3)
                temp=3
            }
        }
        ivAddWallpaper.setOnClickListener {
            openActivity(AddWallpaperActivity::class.java)
        }
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
        viewPanding.setBackgroundColor(resources.getColor(R.color.white))
        viewApproved.setBackgroundColor(resources.getColor(R.color.white))
        viewReject.setBackgroundColor(resources.getColor(R.color.white))
        when (pos){
            1->{
                Common.isUploadTrue=false
                viewPanding.setBackgroundColor(resources.getColor(R.color.dark_gray))
                replaceFragment(PendingFragment())
            }
            2->{
                viewApproved.setBackgroundColor(resources.getColor(R.color.dark_gray))
                replaceFragment(ApprovedFragment())
            }
            3->{
                viewReject.setBackgroundColor(resources.getColor(R.color.dark_gray))
                replaceFragment(RejectFragment())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(Common.isUploadTrue){
            temp=1
            setWallPaper(1)
        }
    }
}
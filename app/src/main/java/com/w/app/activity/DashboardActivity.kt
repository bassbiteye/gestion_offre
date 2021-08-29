package com.w.app.activity

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.fragment.*
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardActivity : BaseActivity() {

    var drawer_layout: DrawerLayout? = null
    var nav_view: LinearLayout? = null
    var tvName:TextView?=null
    var ivProfile:ImageView?=null
    override fun setLayout(): Int {
        return R.layout.activity_dashboard
    }
    var temp=1
    override fun InitView() {
        drawer_layout = findViewById(R.id.drawer_layout)
        nav_view = findViewById(R.id.nav_view)

        tvName=drawer_layout!!.findViewById(R.id.tv_NevProfileName)!!
        ivProfile=drawer_layout!!.findViewById(R.id.iv_NevProfile)!!

        if(SharePreference.getBooleanPref(this@DashboardActivity,SharePreference.isLogin)){
            if (Common.isCheckNetwork(this@DashboardActivity)) {
                val hasmap = HashMap<String, String>()
                hasmap.put("user_id", SharePreference.getStringPref(this@DashboardActivity, SharePreference.userId)!!)
                callApiProfile(hasmap,false)
            } else {
                Common.alertErrorOrValidationDialog(
                    this@DashboardActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }else{
            replaceFragment(HomeFragment())
        }

    }

    open fun onDrawerToggle() {
        drawer_layout!!.openDrawer(nav_view!!)
    }

    override fun onBackPressed() {
        finish()
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_home -> {
                drawer_layout!!.closeDrawers()
                if(temp!=1){
                    setWallPaper(1)
                    temp=1
                }
            }
            R.id.rl_category->{
                drawer_layout!!.closeDrawers()
                if(temp!=2){
                    setWallPaper(2)
                    temp=2
                }
            }
            R.id.rl_favourite -> {
                drawer_layout!!.closeDrawers()
                if(temp!=3){
                    setWallPaper(3)
                    temp=3
                }
            }
            R.id.rl_mywallpaper -> {
                drawer_layout!!.closeDrawers()
                if(temp!=4){
                    setWallPaper(4)
                    temp=4
                }

            }
            R.id.rl_setting -> {
                drawer_layout!!.closeDrawers()
                if(temp!=5){
                    setWallPaper(5)
                    temp=5
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FramFragment, fragment)
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
        fragmentTransaction.commit()
    }


    private fun callApiProfile(hasmap: HashMap<String, String>,isProfile:Boolean) {
        Common.showLoadingProgress(this@DashboardActivity)
        val call = ApiClient.getClient.getProfile(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        if(isProfile){
                            Common.isProfileMainEdit=false
                        }
                        val dataResponse: RestResponseModel = restResponce.getResponseData()!!
                        setProfileData(dataResponse,isProfile)
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@DashboardActivity,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@DashboardActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun setProfileData(
        dataResponse: RestResponseModel,
        profile: Boolean
    ) {
        tvName!!.text = dataResponse.getUsername()
        Glide.with(this@DashboardActivity).load(dataResponse.getProfile_image())
            .placeholder(resources.getDrawable(R.drawable.ic_placeholder)).into(ivProfile!!)
        if(!profile){
            replaceFragment(HomeFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        if(Common.isProfileMainEdit){
            if (Common.isCheckNetwork(this@DashboardActivity)) {
                val hasmap = HashMap<String, String>()
                hasmap.put("user_id", SharePreference.getStringPref(this@DashboardActivity, SharePreference.userId)!!)
                callApiProfile(hasmap,true)
            } else {
                Common.alertErrorOrValidationDialog(
                    this@DashboardActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }

    fun setWallPaper(pos:Int){
        when (pos){
            1->{
                replaceFragment(HomeFragment())
            }
            2->{
                replaceFragment(CategoryFragment())
            }
            3->{
                if(SharePreference.getBooleanPref(this@DashboardActivity,SharePreference.isLogin)){
                    replaceFragment(FavouriteFragment())
                }else {
                    Common.setCommanLogin(this@DashboardActivity)
                }
            }
            4->{
                if(SharePreference.getBooleanPref(this@DashboardActivity,SharePreference.isLogin)){
                    replaceFragment(MyWallpaperFragment())
                }else{
                    Common.setCommanLogin(this@DashboardActivity)
                }
            }
            5->{
                if(SharePreference.getBooleanPref(this@DashboardActivity,SharePreference.isLogin)){
                    replaceFragment(SettingFragment())
                }else{
                    Common.setCommanLogin(this@DashboardActivity)
                }
            }
        }
    }
}
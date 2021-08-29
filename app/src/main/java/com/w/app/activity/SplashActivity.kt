package com.w.app.activity

import android.os.Handler
import com.w.app.R
import com.w.app.base.BaseActivity
import com.w.app.utils.SharePreference

class SplashActivity : BaseActivity() {
    override fun setLayout(): Int {
       return R.layout.activity_splash
    }

    override fun InitView() {
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Handler().postDelayed({
            if(!SharePreference.getBooleanPref(this@SplashActivity,SharePreference.isTutorial)){
                openActivity(TutorialsActivity::class.java)
                finish()
            }else{
                if(SharePreference.getBooleanPref(this@SplashActivity,SharePreference.isLogin)){
                    openActivity(DashboardActivity::class.java)
                    finish()
                }else{
                    openActivity(LoginActivity::class.java)
                    finish()
                }
            }
        },3000)
    }
}

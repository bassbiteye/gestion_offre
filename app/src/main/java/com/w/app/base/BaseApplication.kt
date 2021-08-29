package com.w.app.base


import com.w.app.R
import android.app.Application
import androidx.multidex.MultiDex
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(
            CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .disableCustomViewInflation()
                .build()
        )
        MultiDex.install(this);
    }
}
package com.w.app.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.w.app.R
import com.w.app.adaptor.ImageSliderAdaptor
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_login.viewPager
import kotlinx.android.synthetic.main.activity_mainlogin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LoginMainActivity : BaseActivity() {
    var timer: Timer? = null
    private var currentPage = 0
    private var imagelist: ArrayList<Drawable>? = null
    var token = ""
    override fun setLayout(): Int {
        return R.layout.activity_mainlogin
    }

    override fun InitView() {
        FirebaseApp.initializeApp(this@LoginMainActivity)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                token = task.result?.token!!
            })
        imagelist = ArrayList()
        imagelist!!.add(resources.getDrawable(R.drawable.temp))
        imagelist!!.add(resources.getDrawable(R.drawable.temp1))
        imagelist!!.add(resources.getDrawable(R.drawable.temp3))
        imagelist!!.add(resources.getDrawable(R.drawable.temp4))
        imagelist!!.add(resources.getDrawable(R.drawable.temp5))
        loadPagerImages(imagelist!!)
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvRegistration -> {
                startActivity(
                    Intent(
                        this@LoginMainActivity,
                        RegistrationActivity::class.java
                    ).putExtra("type", "loginmain")
                )
                finish()
            }
            R.id.tvForgetPassword -> {
                openActivity(ForgetpasswordActivity::class.java)
            }
            R.id.ivBack -> {
                finish()
            }
            R.id.tv_btn_login -> {
                if (edEmailAddress.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(
                        this@LoginMainActivity,
                        resources.getString(R.string.validation_email)
                    )
                } else if (!Common.isValidEmail(edEmailAddress.text.toString())) {
                    Common.alertErrorOrValidationDialog(
                        this@LoginMainActivity,
                        resources.getString(R.string.validation_valid_email)
                    )
                } else if (edPassword.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(
                        this@LoginMainActivity,
                        resources.getString(R.string.validation_password)
                    )
                } else {
                    val hasmap = HashMap<String, String>()
                    hasmap.put("email", edEmailAddress.text.toString())
                    hasmap.put("password", edPassword.text.toString())
                    hasmap.put("device_type", "1")
                    hasmap.put("device_token",token)
                    if (Common.isCheckNetwork(this@LoginMainActivity)) {
                        callApiLogin(hasmap)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@LoginMainActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun loadPagerImages(imageHase: ArrayList<*>) {
        val adapter = ImageSliderAdaptor(this@LoginMainActivity, imageHase)
        viewPager.setAdapter(adapter)
        val handler = Handler()
        val Update = Runnable {
            if (currentPage == imageHase.size) {
                currentPage = 0
            }
            viewPager.setCurrentItem(currentPage++, true)
        }
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post(Update)
            }
        }, 500, 3000)
    }

    override fun onPause() {
        super.onPause()
        if (timer != null)
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

    private fun callApiLogin(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@LoginMainActivity)
        val call = ApiClient.getClient.getLogin(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<RestResponseModel>>,
                response: Response<RestResponse<RestResponseModel>>
            ) {
                if (response.code() == 200) {
                    val loginResponce: RestResponse<RestResponseModel> = response.body()!!
                    if (loginResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        val loginModel: RestResponseModel = loginResponce.getResponseData()!!
                        SharePreference.setBooleanPref(
                            this@LoginMainActivity,
                            SharePreference.isLogin,
                            true
                        )
                        SharePreference.setStringPref(
                            this@LoginMainActivity,
                            SharePreference.userId,
                            loginModel.getId()!!
                        )
                        val intent = Intent(this@LoginMainActivity, DashboardActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent);
                        finish()
                        finishAffinity()
                    } else if (loginResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@LoginMainActivity,
                            loginResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@LoginMainActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
}
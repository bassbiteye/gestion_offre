package com.w.app.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
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
import kotlinx.android.synthetic.main.activity_login.viewPager
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class RegistrationActivity:BaseActivity() {
    var timer: Timer?=null
    private var currentPage = 0
    private var imagelist:ArrayList<Drawable>?=null
    var token = ""
    override fun setLayout(): Int {
       return R.layout.activity_register
    }

    override fun InitView() {
        FirebaseApp.initializeApp(this@RegistrationActivity)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                token = task.result?.token!!
            })
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
                if(intent.getStringExtra("type")!!.equals("login")){
                    startActivity(Intent(this@RegistrationActivity,LoginActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this@RegistrationActivity,LoginMainActivity::class.java))
                    finish()
                }
            }
            R.id.tvLogin->{
                openActivity(LoginMainActivity::class.java)
                finish()
            }
            R.id.tvRegistration->{
                if(edUserName.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_name)
                    )
                }else if(edEmailAddress.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_email)
                    )
                }else if(!Common.isValidEmail(edEmailAddress.text.toString())){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_valid_email)
                    )
                }else if(edPassword.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_password)
                    )
                }else if(edPassword.text.toString().length<8){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_valid_password)
                    )
                }else if(edCPassword.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_cpassword)
                    )
                }else if(!edPassword.text.toString().equals(edCPassword.text.toString())){
                    Common.alertErrorOrValidationDialog(
                        this@RegistrationActivity,
                        resources.getString(R.string.validation_valid_cpassword)
                    )
                }else{
                    val hasmap=HashMap<String,String>()
                    hasmap.put("email",edEmailAddress.text.toString())
                    hasmap.put("username",edUserName.text.toString())
                    hasmap.put("password",edPassword.text.toString())
                    hasmap.put("device_type","1")
                    hasmap.put("device_token",token)
                    if(Common.isCheckNetwork(this@RegistrationActivity)){
                        callApiRegistration(hasmap)
                    }else{
                        Common.alertErrorOrValidationDialog(this@RegistrationActivity,resources.getString(R.string.no_internet))
                    }
                }

            }
        }
    }

    override fun onBackPressed() {
        if(intent.getStringExtra("type")!!.equals("login")){
            startActivity(Intent(this@RegistrationActivity,LoginActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this@RegistrationActivity,LoginMainActivity::class.java))
            finish()
        }
    }

    private fun loadPagerImages(imageHase: ArrayList<*>) {
        val adapter = ImageSliderAdaptor(this@RegistrationActivity, imageHase)
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

    private fun callApiRegistration(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@RegistrationActivity)
        val call= ApiClient.getClient.setRegistration(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if(response.code()==200){
                    val loginResponce: RestResponse<RestResponseModel> = response.body()!!
                    if(loginResponce.getResponseCode().equals("1")){
                        Common.dismissLoadingProgress()
                        successfulDialog(this@RegistrationActivity, loginResponce.getResponseMessage())
                    }else if(loginResponce.getResponseCode().equals("0")){
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(this@RegistrationActivity,loginResponce.getResponseMessage())
                    }
                }
            }
            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(this@RegistrationActivity,resources.getString(R.string.error_msg))
            }
        })
    }

    fun successfulDialog(act: Activity, msg: String?) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(act)
            val m_view = m_inflater.inflate(R.layout.dlg_validation, null, false)
            val textDesc: TextView = m_view.findViewById(R.id.tvMessage)
            textDesc.text = msg
            val tvOk: TextView = m_view.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()
                if(intent.getStringExtra("type")!!.equals("login")){
                    startActivity(Intent(this@RegistrationActivity,LoginActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this@RegistrationActivity,LoginMainActivity::class.java))
                    finish()
                }
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
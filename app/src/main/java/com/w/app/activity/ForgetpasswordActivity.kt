package com.w.app.activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.w.app.R
import com.w.app.adaptor.ImageSliderAdaptor
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import kotlinx.android.synthetic.main.activity_forgetpassword.*
import kotlinx.android.synthetic.main.activity_login.viewPager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ForgetpasswordActivity:BaseActivity() {
    var timer: Timer?=null
    private var currentPage = 0
    private var imagelist:ArrayList<Drawable>?=null
    override fun setLayout(): Int {
        return R.layout.activity_forgetpassword
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

    private fun loadPagerImages(imageHase: ArrayList<*>) {
        val adapter = ImageSliderAdaptor(this@ForgetpasswordActivity, imageHase)
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

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSubmit->{
                if (edEmailAddress.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(this@ForgetpasswordActivity, resources.getString(R.string.validation_email))
                } else if (!Common.isValidEmail(edEmailAddress.text.toString())) {
                    Common.alertErrorOrValidationDialog(this@ForgetpasswordActivity, resources.getString(R.string.validation_valid_email))
                } else{
                    val hasmap = HashMap<String, String>()
                    hasmap.put("email", edEmailAddress.text.toString())
                    if (Common.isCheckNetwork(this@ForgetpasswordActivity)) {
                        callApiForgetpassword(hasmap)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@ForgetpasswordActivity,
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

    private fun callApiForgetpassword(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@ForgetpasswordActivity)
        val call= ApiClient.getClient.setForgetPassword(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if(response.code()==200){
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    if(restResponce.getResponseCode().equals("1")){
                        Common.dismissLoadingProgress()
                        successfulDialog(this@ForgetpasswordActivity, restResponce.getResponseMessage())
                    }else if(restResponce.getResponseCode().equals("0")){
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(this@ForgetpasswordActivity,restResponce.getResponseMessage())
                    }
                }
            }
            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(this@ForgetpasswordActivity,resources.getString(R.string.error_msg))
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
                finish()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
package com.w.app.activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_changepassword.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class ChangepasswordActivity : BaseActivity() {
    override fun setLayout(): Int {
        return R.layout.activity_changepassword
    }

    override fun InitView() {

    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSubmit -> {
                if(edOldPass.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.validation_oldpassword))
                }else if(edNewPassword.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.validation_password))
                }else if(edNewPassword.text.toString().length<7){
                    Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.validation_valid_password))
                }else if(edConfirmPassword.text.toString().equals("")){
                    Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.validation_cpassword))
                }else if(!edConfirmPassword.text.toString().equals(edNewPassword.text.toString())){
                    Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.validation_valid_cpassword))
                }else{
                    val hasmap = HashMap<String, String>()
                    hasmap.put("user_id",SharePreference.getStringPref(this@ChangepasswordActivity,SharePreference.userId)!!)
                    hasmap.put("oldpassword", edOldPass.text.toString())
                    hasmap.put("newpassword", edNewPassword.text.toString())
                    if(Common.isCheckNetwork(this@ChangepasswordActivity)){
                        callApiChangepassword(hasmap)
                    }else{
                        Common.alertErrorOrValidationDialog(this@ChangepasswordActivity,resources.getString(R.string.no_internet))
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun callApiChangepassword(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@ChangepasswordActivity)
        val call = ApiClient.getClient.getChangePassword(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if (response.code() == 200) {
                    val restResponse: RestResponse<RestResponseModel> = response.body()!!
                    if (restResponse.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        successfulDialog(
                            this@ChangepasswordActivity,
                            restResponse.getResponseMessage()
                        )
                    } else if (restResponse.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ChangepasswordActivity,
                            restResponse.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ChangepasswordActivity,
                    resources.getString(R.string.error_msg)
                )
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
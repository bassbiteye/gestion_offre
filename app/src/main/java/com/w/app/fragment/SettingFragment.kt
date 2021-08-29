package com.w.app.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.w.app.R
import com.w.app.activity.ChangepasswordActivity
import com.w.app.activity.DashboardActivity
import com.w.app.activity.EditProfileActivity
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseFragmnet
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.dlg_logout.view.*
import kotlinx.android.synthetic.main.fragment_setting.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingFragment : BaseFragmnet() {

    var notificationType = ""
    var name = ""
    var email = ""
    var profile = ""
    override fun setView(): Int {
        return R.layout.fragment_setting
    }

    override fun Init(view: View) {
        if (Common.isCheckNetwork(activity!!)) {
            val hasmap = HashMap<String, String>()
            hasmap.put("user_id",SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
            callApiProfile(hasmap,false)
        } else {
            Common.alertErrorOrValidationDialog(
                activity!!,
                resources.getString(R.string.no_internet)
            )
        }

        tvLogout.setOnClickListener {
            alertLogOutDialog()
        }
        ivEditProfile.setOnClickListener {
            startActivity(Intent(activity,EditProfileActivity::class.java).putExtra("name",name).putExtra("email",email).putExtra("image",profile))
        }
        rlChangePassword.setOnClickListener {
            openActivity(ChangepasswordActivity::class.java)
        }
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }

        ivPushNotificationSwitch.setOnClickListener {
            if (Common.isCheckNetwork(activity!!)) {
                Common.getLog("ishan",notificationType)
                val hasmap = HashMap<String, String>()
                hasmap.put("user_id", SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
                hasmap.put("status",notificationType)
                callApisetNotification(hasmap)
            } else {
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }

    }

    fun alertLogOutDialog() {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(activity!!, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(activity!!)
            val m_view = m_inflater.inflate(R.layout.dlg_logout, null, false)

            val finalDialog: Dialog = dialog
            m_view.tvLogout.setOnClickListener {
                finalDialog.dismiss()
                Common.setLogout(activity!!)

            }
            m_view.tvCancel.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }



    private fun callApisetNotification(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(activity!!)
        val call = ApiClient.getClient.setNotification(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    Common.getLog("ishan",notificationType)
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        if(notificationType.equals("1")) {
                           // ivPushNotificationSwitch.setImageDrawable(resources.getDrawable(R.drawable.ic_switchon))
                            ivPushNotificationSwitch.setImageDrawable(resources.getDrawable(R.drawable.ic_switchon))
                            notificationType = "2"
                        }else if(notificationType.equals("2")){
                            ivPushNotificationSwitch.setImageDrawable(resources.getDrawable(R.drawable.ic_switchoff))
                            notificationType="1"
                        }
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            activity!!,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiProfile(hasmap: HashMap<String, String>,isProfile:Boolean) {
        Common.showLoadingProgress(activity!!)
        val call = ApiClient.getClient.getProfile(hasmap)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!

                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        if(isProfile){
                            Common.isProfileEdit=false
                        }
                        val dataResponse: RestResponseModel = restResponce.getResponseData()!!
                        setProfileData(dataResponse)
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            activity!!,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun setProfileData(dataResponse: RestResponseModel) {
        name=dataResponse.getUsername()!!
        email=dataResponse.getEmail()!!
        profile=dataResponse.getProfile_image()!!
        tvUserName.text = dataResponse.getUsername()
        tvUserEmail.text = dataResponse.getEmail()
        Glide.with(activity!!).load(dataResponse.getProfile_image())
            .placeholder(resources.getDrawable(R.drawable.ic_placeholder)).into(ivProfile)
        notificationType = dataResponse.getNotification()!!

        if (notificationType.equals("1")) {
            ivPushNotificationSwitch.setImageDrawable(resources.getDrawable(R.drawable.ic_switchon))
            notificationType="2"
        } else if (notificationType.equals("2")) {
            ivPushNotificationSwitch.setImageDrawable(resources.getDrawable(R.drawable.ic_switchoff))
            notificationType="1"
        }
    }

    override fun onResume() {
        super.onResume()
        if(Common.isProfileEdit){
            if (Common.isCheckNetwork(activity!!)) {
                val hasmap = HashMap<String, String>()
                hasmap.put("user_id", SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
                callApiProfile(hasmap,true)
            } else {
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }

    }
}
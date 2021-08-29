package com.w.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_editeprofile.*
import kotlinx.android.synthetic.main.dlg_externalstorage.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class EditProfileActivity:BaseActivity() {
    private val SELECT_FILE = 201
    private val REQUEST_CAMERA = 202
    private var mSelectedFileImg: File? = null
    override fun setLayout(): Int {
        return R.layout.activity_editeprofile
    }
    override fun InitView() {
        edEmailAddress.setText(intent.getStringExtra("email"))
        edUserName.setText(intent.getStringExtra("name"))
        Glide.with(this@EditProfileActivity).load(intent.getStringExtra("image")).placeholder(resources.getDrawable(R.drawable.ic_placeholder)).into(ivProfile)
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvUpdate->{
                if (edUserName.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(this@EditProfileActivity, resources.getString(R.string.validation_name))
                } else {
                    if (Common.isCheckNetwork(this@EditProfileActivity)) {
                        mCallApiEditProfile()
                    } else {
                        Common.alertErrorOrValidationDialog(this@EditProfileActivity, resources.getString(R.string.no_internet))
                    }
                }
            }
            R.id.ivGellary->{
                getExternalStoragePermission()
            }

        }
    }

    override fun onBackPressed() {
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data)
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data!!)
            }
        }
    }

    fun getExternalStoragePermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        imageSelectDialog(this@EditProfileActivity)
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        Common.settingDialog(this@EditProfileActivity)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    @SuppressLint("InlinedApi")
    fun imageSelectDialog(act: Activity) {
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
            dialog.setCancelable(true)
            val m_inflater = LayoutInflater.from(act)
            val m_view = m_inflater.inflate(R.layout.dlg_externalstorage, null, false)

            val finalDialog: Dialog = dialog
            m_view.tvSetImageCamera.setOnClickListener {
                finalDialog.dismiss()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(
                    intent,
                    REQUEST_CAMERA
                )
            }
            m_view.tvSetImageGallery.setOnClickListener {
                finalDialog.dismiss()
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                //   intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
            }
            dialog.setContentView(m_view)
            if (!act.isFinishing) dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        var bm: Bitmap? = null
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(this@EditProfileActivity.getContentResolver(), data.data)
                val bytes = ByteArrayOutputStream()
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                mSelectedFileImg = File(Environment.getExternalStorageDirectory(), System.currentTimeMillis().toString() + ".jpeg")
                Common.getLog("ImgPath", "ImagePath>>$mSelectedFileImg")
                val fo: FileOutputStream
                try {
                    mSelectedFileImg!!.createNewFile()
                    fo = FileOutputStream(mSelectedFileImg)
                    fo.write(bytes.toByteArray())
                    fo.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Glide.with(this@EditProfileActivity)
            .load(Uri.parse("file://" + mSelectedFileImg!!.getPath()))
            .into(ivProfile)

    }

    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!["data"] as Bitmap?
        val bytes = ByteArrayOutputStream()
        thumbnail!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        mSelectedFileImg = File(
            Environment.getExternalStorageDirectory(),
            System.currentTimeMillis().toString() + ".jpeg"
        )
        val fo: FileOutputStream
        try {
            mSelectedFileImg!!.createNewFile()
            fo = FileOutputStream(mSelectedFileImg)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Glide.with(this@EditProfileActivity)
            .load(Uri.parse("file://" + mSelectedFileImg!!.getPath()))
            .into(ivProfile)
    }

    private fun mCallApiEditProfile() {
        Common.showLoadingProgress(this@EditProfileActivity)
        var call: Call<RestResponse<RestResponseModel>>?=null
        if(mSelectedFileImg!=null){
            call= ApiClient.getClient.setProfile(Common.setRequestBody(SharePreference.getStringPref(this@EditProfileActivity,SharePreference.userId)!!),Common.setRequestBody(edUserName.text.toString()),Common.setImageUpload("profile_image",mSelectedFileImg!!))
        }else {
            call= ApiClient.getClient.setProfile(Common.setRequestBody(SharePreference.getStringPref(this@EditProfileActivity,SharePreference.userId)!!),Common.setRequestBody(edUserName.text.toString()),null)
        }
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                val loginResponce: RestResponse<RestResponseModel> = response.body()!!
                if(loginResponce.getResponseCode().equals("1")){
                    Common.dismissLoadingProgress()
                    Common.isProfileEdit=true
                    Common.isProfileMainEdit=true
                    successfulDialog(this@EditProfileActivity, loginResponce.getResponseMessage())
                }else if(loginResponce.getResponseCode().equals("0")){
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(this@EditProfileActivity,loginResponce.getResponseMessage())
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@EditProfileActivity,
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
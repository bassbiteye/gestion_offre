package com.w.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.ListResponse
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.CategoryModel
import com.w.app.model.RestResponseModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_addwallpaper.*
import kotlinx.android.synthetic.main.dlg_externalstorage.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class AddWallpaperActivity :BaseActivity() {
    private val SELECT_FILE = 201
    private val REQUEST_CAMERA = 202
    private var mSelectedFileImg: File? = null
    var categoryList: ArrayList<CategoryModel>? = null
    var categorySpList: ArrayList<String>? = null
    var getId: Int = 0
    var getHeigt:Int=0
    var getColor=""
    override fun setLayout(): Int {
       return R.layout.activity_addwallpaper
    }
    override fun InitView() {
        categoryList = ArrayList()
        categorySpList = ArrayList()
        if (Common.isCheckNetwork(this@AddWallpaperActivity)) {
            callApiCategory()
        } else {
            Common.alertErrorOrValidationDialog(
                this@AddWallpaperActivity,
                resources.getString(R.string.no_internet)
            )
        }

        rlSelectCategory.setOnClickListener {
            spCategory.performClick()
            spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if(position!=0){
                        Common.getLog("getPostion", position.toString())
                        getId = categoryList!!.get(position).getId()!!.toInt()
                        tvSelectCategory.text = categorySpList!!.get(position)
                        tvSelectCategory.setTextColor(resources.getColor(R.color.black))
                    }

                }
            }
        }
    }
    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.ivAddWallpaper->{
                getExternalStoragePermission()
            }
            R.id.tvUpload->{
                 if(getId==0){
                     Common.alertErrorOrValidationDialog(this@AddWallpaperActivity, resources.getString(R.string.validation_category))
                 }else if(edWallPaperName.text.toString().equals("")){
                     Common.alertErrorOrValidationDialog(this@AddWallpaperActivity, resources.getString(R.string.validation_wallpapername))
                 }else if(mSelectedFileImg==null){
                     Common.alertErrorOrValidationDialog(this@AddWallpaperActivity, resources.getString(R.string.validation_wallpaper))
                 }else{
                     if (Common.isCheckNetwork(this@AddWallpaperActivity)) {
                         mCallApiAddWallpaper()
                     } else {
                         Common.alertErrorOrValidationDialog(this@AddWallpaperActivity, resources.getString(R.string.no_internet))
                     }
                 }
            }
        }
    }
    override fun onBackPressed() {
        finish()
    }

    private fun callApiCategory() {
        Common.showLoadingProgress(this@AddWallpaperActivity)
        val call = ApiClient.getClient.getCategory()
        call.enqueue(object : Callback<ListResponse<CategoryModel>> {
            override fun onResponse(call: Call<ListResponse<CategoryModel>>, response: Response<ListResponse<CategoryModel>>) {
                if (response.code() == 200) {
                    val restResponce: ListResponse<CategoryModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        val categoryModel = CategoryModel()
                        categoryModel.setId("0")
                        categoryModel.setCategory_name("Select category")
                        categoryModel.setCategory_image("img")
                        categoryList!!.add(0,categoryModel)
                        categorySpList!!.add(0,"Select category")
                        for (i in 0 until restResponce.getResponseData().size){
                            val categoryModelin=restResponce.getResponseData().get(i)
                            categoryList!!.add(categoryModelin)
                            categorySpList!!.add(categoryModelin.getCategory_name()!!)
                        }
                        val adapter = ArrayAdapter(
                            this@AddWallpaperActivity,
                            R.layout.textview_spinner,
                            categorySpList!!
                        ).also {
                            it.setDropDownViewResource(R.layout.textview_spinner)
                        }
                        spCategory.adapter = adapter
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@AddWallpaperActivity,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ListResponse<CategoryModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@AddWallpaperActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
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
                        imageSelectDialog(this@AddWallpaperActivity)
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        Common.settingDialog(this@AddWallpaperActivity)
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
                bm = MediaStore.Images.Media.getBitmap(this@AddWallpaperActivity.getContentResolver(), data.data)
                val bytes = ByteArrayOutputStream()
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                getColor=getDominantColor(bm)
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
        getHeigt=getHeightIMGSize(Uri.fromFile(mSelectedFileImg))
        Glide.with(this@AddWallpaperActivity)
            .load(Uri.parse("file://" + mSelectedFileImg!!.getPath()))
            .into(ivWallpaper)


    }

    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!["data"] as Bitmap?
        val bytes = ByteArrayOutputStream()
        thumbnail!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        getColor=getDominantColor(thumbnail)
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
        getHeigt=getHeightIMGSize(Uri.fromFile(mSelectedFileImg))
        Glide.with(this@AddWallpaperActivity)
            .load(Uri.parse("file://" + mSelectedFileImg!!.getPath()))
            .into(ivWallpaper)
    }

    private fun getHeightIMGSize(uri: Uri): Int {
        val fd: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor?=fd!!.fileDescriptor
        val bitmap: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        val height:Int=bitmap.height
        val width:Int=bitmap.width
        var setHeight:Int=0
        if (width > height){
            setHeight=140
        }else{
            setHeight=310
        }
        fd.close();
        return setHeight
    }

    fun getDominantColor(bitmap: Bitmap?): String {
        val newBitmap = Bitmap.createScaledBitmap(bitmap!!, 1, 1, true)
        val color = newBitmap.getPixel(0, 0)
        newBitmap.recycle()
        return String.format("#%06X", 0xFFFFFF and color)
    }


    private fun mCallApiAddWallpaper() {
        Common.showLoadingProgress(this@AddWallpaperActivity)
        var call: Call<RestResponse<RestResponseModel>>?=null
        if(mSelectedFileImg!=null){
            call= ApiClient.getClient.getUploadwallpaper(Common.setRequestBody(SharePreference.getStringPref(this@AddWallpaperActivity,SharePreference.userId)!!),Common.setRequestBody(getId.toString()),Common.setRequestBody(getColor+"ff"),Common.setRequestBody(getHeigt.toString()),Common.setRequestBody(edWallPaperName.text.toString()),Common.setImageUpload("wallpaper_image",mSelectedFileImg!!))
        }else {
            call= ApiClient.getClient.getUploadwallpaper(Common.setRequestBody(SharePreference.getStringPref(this@AddWallpaperActivity,SharePreference.userId)!!),Common.setRequestBody(getId.toString()),Common.setRequestBody(getColor+"ff"),Common.setRequestBody(getHeigt.toString()),Common.setRequestBody(edWallPaperName.text.toString()),null)
        }
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(call: Call<RestResponse<RestResponseModel>>, response: Response<RestResponse<RestResponseModel>>) {
                val loginResponce: RestResponse<RestResponseModel> = response.body()!!
                if(loginResponce.getResponseCode().equals("1")){
                    Common.dismissLoadingProgress()
                    successfulDialog(this@AddWallpaperActivity, loginResponce.getResponseMessage())
                }else if(loginResponce.getResponseCode().equals("0")){
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(this@AddWallpaperActivity,loginResponce.getResponseMessage())
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@AddWallpaperActivity,
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
                Common.isUploadTrue=true
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
package com.w.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.model.RestResponseModel
import com.w.app.model.WallpaperModel
import com.w.app.utils.Common
import com.w.app.utils.FileUtils
import com.w.app.utils.PhotoEditorActivity
import com.w.app.utils.SharePreference
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.activity_imageshow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.*


class ImageShowActivity : BaseActivity() {
    var mPhotoEditor: PhotoEditor? = null
    var wallpaperList: ArrayList<WallpaperModel>? = null
    open val position: Int? = 0
    var adapter: WallpaperSliderAdaptor? = null
    val temp = 0;
    var mSaveImageUri: Uri? = null


    override fun setLayout(): Int {
        return R.layout.activity_imageshow
    }

    override fun InitView() {
        wallpaperList = intent.getParcelableArrayListExtra("imagelist")
        adapter = WallpaperSliderAdaptor(this@ImageShowActivity, wallpaperList!!)
        viewPager.setAdapter(adapter)
        viewPager.setCurrentItem(intent.getStringExtra("pos")!!.toInt());
    }

    inner class WallpaperSliderAdaptor(
        var context: Activity,
        private val arrayList: ArrayList<WallpaperModel>
    ) : PagerAdapter() {
        var positionGet: Int = 0
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getCount(): Int {
            return arrayList.size
        }

        @SuppressLint("NewApi")
        override fun instantiateItem(view: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(context)
            val itemView = inflater.inflate(R.layout.row_showwallpaper, view, false) as ViewGroup

            val ivWallpaper = itemView.findViewById<ImageView>(R.id.ivWallpaper)
            val iv_btn_wallpaper = itemView.findViewById<ImageView>(R.id.iv_btn_wallpaper)
            val iv_btn_download = itemView.findViewById<ImageView>(R.id.iv_btn_download)
            val iv_btn_share = itemView.findViewById<ImageView>(R.id.iv_btn_share)
            val iv_btn_favourite = itemView.findViewById<ImageView>(R.id.iv_btn_favourite)
            val iv_btn_edit = itemView.findViewById<ImageView>(R.id.iv_btn_edit)
            val ivProfile = itemView.findViewById<ImageView>(R.id.ivProfile)
            val tvUserName = itemView.findViewById<TextView>(R.id.tvUserName)
            val tvWallpaperCategory = itemView.findViewById<TextView>(R.id.tvWallpaperCategory)
            val llAdmin = itemView.findViewById<LinearLayout>(R.id.llAdmin)

            Glide.with(context).load(arrayList[position].getWallpaper_image())
                .placeholder(R.drawable.temp).centerCrop().into(ivWallpaper)

            Glide.with(context).load(arrayList[position].getUser_image())
                .placeholder(R.drawable.temp).centerCrop().into(ivProfile)

            tvUserName.text = arrayList[position].getUser_name()
            tvWallpaperCategory.text = arrayList[position].getCategory_name()
            positionGet = position

            if (wallpaperList!!.get(position).getIsFavourite().equals("1")) {
                iv_btn_favourite.setImageDrawable(resources.getDrawable(R.drawable.ic_fillheart))
                iv_btn_favourite.imageTintList = ColorStateList.valueOf(Color.WHITE)
            } else if (wallpaperList!!.get(position).getIsFavourite().equals("2")) {
                iv_btn_favourite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart))
            }

            llAdmin.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        UserDetailActivity::class.java
                    ).putExtra("wallpaper_id", arrayList[position].getUser_id())
                )
                context.finish()
            }

            iv_btn_wallpaper.setOnClickListener {
                Common.showLoadingProgress(this@ImageShowActivity)
                val wallpaperManager = WallpaperManager.getInstance(getApplicationContext())
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(wallpaperList!!.get(position).getWallpaper_image())
                    .into(object : SimpleTarget<Bitmap?>() {
                        @SuppressLint("MissingPermission")
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                            Common.dismissLoadingProgress()
                            Common.getToast(this@ImageShowActivity, "Set Wallpaper Successfully")
                        }
                    })
            }
            iv_btn_download.setOnClickListener {
                DownloadsImage(0).execute(wallpaperList!!.get(position).getWallpaper_image())
            }

            iv_btn_share.setOnClickListener {
                if (mSaveImageUri == null) {
                    DownloadsImage(1).execute(
                        wallpaperList!!.get(viewPager.currentItem).getWallpaper_image()
                    )
                } else {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri!!))
                    startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
                }
            }
            iv_btn_favourite.setOnClickListener {
                if (SharePreference.getBooleanPref(
                        this@ImageShowActivity,
                        SharePreference.isLogin
                    )
                ) {
                    if (wallpaperList!!.get(position).getIsFavourite().equals("2")) {
                        if (Common.isCheckNetwork(this@ImageShowActivity)) {

                            callApiFavouriteWallPaper(position, iv_btn_favourite)
                        } else {
                            Common.alertErrorOrValidationDialog(
                                this@ImageShowActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                } else {
                    Common.setCommanLogin(this@ImageShowActivity)
                }
            }

            iv_btn_edit.setOnClickListener {
                startActivity(
                    Intent(
                        this@ImageShowActivity,
                        PhotoEditorActivity::class.java
                    ).putExtra(
                        "selectedImagePath",
                        wallpaperList!!.get(viewPager.currentItem).getWallpaper_image()
                    )
                )
            }
            (view as ViewPager).addView(itemView)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View?)
        }
    }


    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
        }
    }

    private fun buildFileProviderUri(uri: Uri): Uri? {
        return Uri.parse(FileUtils.getPath(uri, this@ImageShowActivity))
    }

    private var pDialog: ProgressDialog? = null
    val progress_bar_type = 0
    override fun onCreateDialog(id: Int): Dialog? {
        return when (id) {
            progress_bar_type -> {
                pDialog = ProgressDialog(this)
                pDialog!!.setMessage("Downloading file Please wait...")
                pDialog!!.setIndeterminate(false)
                pDialog!!.setMax(100)
                pDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                pDialog!!.setCancelable(true)
                pDialog!!.show()
                pDialog
            }
            else -> null
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class DownloadsImage(val temp: Int) : AsyncTask<String, String, String>() {
        // private var progressDialog: ProgressDialog? = null
        var path: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            showDialog(progress_bar_type);
        }

        override fun doInBackground(vararg params: String?): String? {
            var count: Int = 0
            try {
                val url = URL(params[0])
                val conection: URLConnection = url.openConnection()
                conection.connect()
                val lenghtOfFile: Int = conection.getContentLength()
                val input: InputStream = BufferedInputStream(url.openStream(), 8192)
                val path: File = Common.getCacheFolder(this@ImageShowActivity)!!
                val imageFile = File(path, System.currentTimeMillis().toString() + ".png")
                mSaveImageUri = Uri.fromFile(imageFile)
                val output: OutputStream = FileOutputStream(imageFile)
                val data = ByteArray(1024)
                var total: Int = 0
                while (input.read(data).also({ count = it }) != -1) {
                    total += count
                    publishProgress((total * 100 / lenghtOfFile).toString())
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()
            } catch (e: Exception) {
                Log.e("Error: ", e.message)
            }
            return null
        }

        override fun onProgressUpdate(vararg progress: String) {
            pDialog!!.progress = progress[0].toInt()
        }

        override fun onPostExecute(aVoid: String?) {
            super.onPostExecute(aVoid)
            dismissDialog(progress_bar_type)
            Common.getToast(this@ImageShowActivity, "Your File Download Successfull")
            if (temp == 1) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri!!))
                startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
            }
        }
    }


    @SuppressLint("NewApi")
    private fun callApiFavouriteWallPaper(pos: Int, ivBtnFavourite: ImageView) {
        Common.showLoadingProgress(this@ImageShowActivity)
        val map = HashMap<String, String>()
        map.put(
            "user_id",
            SharePreference.getStringPref(this@ImageShowActivity, SharePreference.userId)!!
        )
        map.put("wallpaper_id", wallpaperList!!.get(pos).getId()!!)
        map.put("favourite", "1")
        val call = ApiClient.getClient.getFavouriteunFavourite(map)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<RestResponseModel>>,
                response: Response<RestResponse<RestResponseModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        SharePreference.setBooleanPref(
                            this@ImageShowActivity,
                            SharePreference.isFavourite,
                            true
                        )
                        ivBtnFavourite.setImageDrawable(resources.getDrawable(R.drawable.ic_fillheart))
                        ivBtnFavourite.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        wallpaperList!!.get(pos).setIsFavourite("1")
                        adapter!!.notifyDataSetChanged()
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ImageShowActivity,
                            restResponce.getResponseMessage()
                        )

                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@ImageShowActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

}
package com.w.app.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.base.BaseAdaptor
import com.w.app.model.RestResponseModel
import com.w.app.model.WallPaperDetail
import com.w.app.model.WallpaperModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_userdetail.*
import kotlinx.android.synthetic.main.row_item_image.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetailActivity:BaseActivity() {
    var ImageAdaptor: BaseAdaptor<WallpaperModel>? = null
    var wallpaperList = ArrayList<WallpaperModel>()
    var rvUserImage:RecyclerView?=null
    var id: String = ""
    var currentPage: Int = 1
    var isLastPage: Boolean? = false
    var manager: StaggeredGridLayoutManager? = null
    var visibleItemCount: Int = 0;
    var totalItemCount: Int = 0;
    var pastVisibleItem: Int = 0;
    var isLoding: Boolean = true
    override fun setLayout(): Int {
       return R.layout.activity_userdetail
    }

    override fun InitView() {
        rvUserImage=findViewById(R.id.rvUserImage)

        if (Common.isCheckNetwork(this@UserDetailActivity)) {
            callApiDetailWallPaper(true)
        } else {
            Common.alertErrorOrValidationDialog(
                this@UserDetailActivity,
                resources.getString(R.string.no_internet)
            )
        }
        manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvUserImage!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = manager!!.getChildCount();
                    totalItemCount = manager!!.getItemCount();

                    var firstVisibleItems: IntArray? = null
                    firstVisibleItems = manager!!.findFirstVisibleItemPositions(firstVisibleItems)
                    if (firstVisibleItems != null && firstVisibleItems.size > 0) {
                        pastVisibleItem = firstVisibleItems[0]
                    }
                    if (isLoding) {
                        if (visibleItemCount + pastVisibleItem >= totalItemCount) {
                            isLoding = false
                            currentPage ++
                            callApiDetailWallPaper(false)
                        }
                    }
                }
            }
        })
    }

    private fun callApiDetailWallPaper(isFristPage: Boolean) {
        Common.showLoadingProgress(this@UserDetailActivity)
        val map = HashMap<String, String>()
        if(SharePreference.getBooleanPref(this@UserDetailActivity,SharePreference.isLogin)){
            map.put("guest_id",SharePreference.getStringPref(this@UserDetailActivity,SharePreference.userId)!!)
        }else{
            map.put("guest_id","")
        }
        map.put("user_id",intent.getStringExtra("wallpaper_id")!!)
        map.put("pageIndex", currentPage.toString())
        map.put("numberOfRecords", "10")
        val call = ApiClient.getClient.getWallpaperdetail(map)
        call.enqueue(object : Callback<RestResponse<WallPaperDetail>> {
            override fun onResponse(
                call: Call<RestResponse<WallPaperDetail>>,
                response: Response<RestResponse<WallPaperDetail>>
            ) {
                if (response.code() == 200) {
                    var restResponce: RestResponse<WallPaperDetail> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        val wallpaperDetail:WallPaperDetail=restResponce.getResponseData()!!
                        if (wallpaperDetail.getRelated_wallpapers()!!.size>0) {
                            for(i in 0 until wallpaperDetail.getRelated_wallpapers()!!.size){
                                val wallpaperModel=wallpaperDetail.getRelated_wallpapers()!!.get(i)
                                wallpaperList.add(wallpaperModel)
                            }
                            setLatestAdaptor(wallpaperList,isFristPage,wallpaperDetail)
                        }else{
                            if(!isFristPage){
                                isLoding=true
                            }
                        }

                        isLoding=true
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        if(!isFristPage){
                            isLastPage = true
                            isLoding=false
                        }else{
                            Common.alertErrorOrValidationDialog(
                                this@UserDetailActivity,
                                restResponce.getResponseMessage()
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<WallPaperDetail>>, t: Throwable) {
                Common.dismissLoadingProgress()
                isLastPage = true
                isLoding=false
                Common.alertErrorOrValidationDialog(
                    this@UserDetailActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    @SuppressLint("NewApi", "ResourceType")
    fun setLatestAdaptor(
        imageList: ArrayList<WallpaperModel>,
        isFristPage: Boolean,
        wallpaperDetail: WallPaperDetail
    ) {
        if (isFristPage) {
            tvUploadCount.text=wallpaperDetail.getTotal_wallpapers()
            tvLikes.text=wallpaperDetail.getTotal_likes()
            tvViewCount.text=wallpaperDetail.getTotal_views()
            tvAdmin.text=wallpaperDetail.getUser_name()
            Glide.with(this@UserDetailActivity).load(wallpaperDetail.getUser_image()).placeholder(resources.getDrawable(R.drawable.ic_placeholder)).into(ivProfile)
            ImageAdaptor =
                object : BaseAdaptor<WallpaperModel>(this@UserDetailActivity, imageList) {
                    override fun onBindData(
                        holder: RecyclerView.ViewHolder?,
                        `val`: WallpaperModel,
                        position: Int
                    ) {
                        val ivWallpaper: ImageView =
                            holder!!.itemView.findViewById(R.id.ivWallpaper)
                        val tvViewCount: TextView = holder.itemView.findViewById(R.id.tvViewCount)

                        val dimensionInDp = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            wallpaperList.get(position).getWallpaper_height()!!.toInt().toFloat(),resources.displayMetrics
                        )
                        ivWallpaper.getLayoutParams().height = dimensionInDp.toInt()
                        Glide.with(this@UserDetailActivity).load(wallpaperList.get(position).getWallpaper_image()).placeholder(ColorDrawable(Color.parseColor(wallpaperList.get(position).getWallpaper_color().toString().substring(0,7)))).centerInside().into(ivWallpaper)
                        tvViewCount.text = wallpaperList.get(position).getWallpaper_views()
                        holder.itemView.setOnClickListener {
                            if (Common.isCheckNetwork(this@UserDetailActivity)) {
                                callApiCountWallPaper(position)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    this@UserDetailActivity,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        }

                        if(wallpaperList!!.get(position).getIsFavourite().equals("1")){
                            holder.itemView.iv_btn_fevourite.setImageDrawable(resources.getDrawable(R.drawable.ic_fillheart))
                            holder.itemView.iv_btn_fevourite.imageTintList= ColorStateList.valueOf(Color.WHITE)
                        }else if(wallpaperList!!.get(position).getIsFavourite().equals("2")){
                            holder.itemView.iv_btn_fevourite.setImageDrawable(resources.getDrawable(R.drawable.ic_heart))
                        }

                        holder.itemView.iv_btn_fevourite.setOnClickListener {
                            if(SharePreference.getBooleanPref(this@UserDetailActivity,SharePreference.isLogin)){
                                if(wallpaperList.get(position).getIsFavourite().equals("2")){
                                    if (Common.isCheckNetwork(this@UserDetailActivity)) {
                                        callApiFavouriteWallPaper(position)
                                    } else {
                                        Common.alertErrorOrValidationDialog(
                                            this@UserDetailActivity,
                                            resources.getString(R.string.no_internet)
                                        )
                                    }
                                }
                            }else{
                                Common.setCommanLogin(this@UserDetailActivity)
                            }

                        }
                    }

                    override fun setItemLayout(): Int {
                        return R.layout.row_item_image
                    }

                    override fun setNoDataView(): TextView? {
                        return null
                    }

                }
            rvUserImage!!.adapter = ImageAdaptor
            rvUserImage!!.layoutManager = manager
            rvUserImage!!.itemAnimator = DefaultItemAnimator()
            rvUserImage!!.isNestedScrollingEnabled = false
        } else {
            ImageAdaptor!!.notifyDataSetChanged()
        }
    }
    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun callApiFavouriteWallPaper(pos: Int) {
        Common.showLoadingProgress(this@UserDetailActivity)
        val map = HashMap<String, String>()
        map.put("user_id", SharePreference.getStringPref(this@UserDetailActivity, SharePreference.userId)!!)
        map.put("wallpaper_id",wallpaperList.get(pos).getId()!!)
        map.put("favourite","1")
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
                        SharePreference.setBooleanPref(this@UserDetailActivity,SharePreference.isFavourite,true)
                        wallpaperList.get(pos).setIsFavourite("1")
                        ImageAdaptor!!.notifyDataSetChanged()
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@UserDetailActivity,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@UserDetailActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }



    private fun callApiCountWallPaper(pos: Int) {
        Common.showLoadingProgress(this@UserDetailActivity)
        val map = HashMap<String, String>()
        map.put("wallpaper_id",wallpaperList.get(pos).getId()!!)
        val call = ApiClient.getClient.getWallpaperview(map)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<RestResponseModel>>,
                response: Response<RestResponse<RestResponseModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        startActivity(Intent(this@UserDetailActivity,ImageShowActivity::class.java).putExtra("imagelist",wallpaperList).putExtra("pos",pos.toString()))
                        finish()
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@UserDetailActivity,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@UserDetailActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
}
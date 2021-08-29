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
import com.google.android.gms.ads.*
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.ListResponse
import com.w.app.api.RestResponse
import com.w.app.base.BaseActivity
import com.w.app.base.BaseAdaptor
import com.w.app.model.RestResponseModel
import com.w.app.model.WallpaperModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.activity_categorybyimage.*
import kotlinx.android.synthetic.main.activity_categorybyimage.ad
import kotlinx.android.synthetic.main.row_item_image.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryByImageActivity:BaseActivity() {
    var ImageAdaptor: BaseAdaptor<WallpaperModel>? = null
    var wallpaperList = ArrayList<WallpaperModel>()
    var rvCategoryByImage:RecyclerView?=null
    var id: String = ""
    var currentPage: Int = 1
    var isLastPage: Boolean? = false
    var manager: StaggeredGridLayoutManager? = null
    var visibleItemCount: Int = 0;
    var totalItemCount: Int = 0;
    var pastVisibleItem: Int = 0;
    var isLoding: Boolean = true
    var categoryId=""
    override fun setLayout(): Int {
       return R.layout.activity_categorybyimage
    }

    override fun InitView() {
        setAds()
        tvCategoryName.text=intent.getStringExtra("name")!!
        categoryId=intent.getStringExtra("id")!!
        rvCategoryByImage=findViewById(R.id.rvCategoryByImage)

        if (Common.isCheckNetwork(this@CategoryByImageActivity)) {
            callApiCategoryBYWallPaper(true, false)
        } else {
            Common.alertErrorOrValidationDialog(
                this@CategoryByImageActivity,
                resources.getString(R.string.no_internet)
            )
        }

        manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        rvCategoryByImage!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            callApiCategoryBYWallPaper(false, false)
                        }
                    }
                }
            }
        })
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


    private fun callApiCategoryBYWallPaper(isFristPage: Boolean, isFavourite: Boolean) {
        Common.showLoadingProgress(this@CategoryByImageActivity)
        val map = HashMap<String, String>()
        if(SharePreference.getBooleanPref(this@CategoryByImageActivity,SharePreference.isLogin)){
            map.put("user_id",SharePreference.getStringPref(this@CategoryByImageActivity,SharePreference.userId)!!)
        }
        map.put("category_id",categoryId)
        map.put("pageIndex", currentPage.toString())
        map.put("numberOfRecords", "10")
        val call = ApiClient.getClient.getWallpaperbycategory(map)
        call.enqueue(object : Callback<ListResponse<WallpaperModel>> {
            override fun onResponse(
                call: Call<ListResponse<WallpaperModel>>,
                response: Response<ListResponse<WallpaperModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: ListResponse<WallpaperModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        if (restResponce.getResponseData().size > 0) {
                            for(i in 0 until restResponce.getResponseData().size){
                                val wallpaperModel=restResponce.getResponseData().get(i)
                                wallpaperList.add(wallpaperModel)
                            }
                        }
                        if (isFavourite) {
                            SharePreference.setBooleanPref(
                                this@CategoryByImageActivity,
                                SharePreference.isFavourite,
                                false
                            )
                            wallpaperList.clear()
                        }
                        setCategoryByImageAdaptor(wallpaperList,isFristPage)
                        if(wallpaperList.size<10){
                            isLoding=false
                        }else{
                            isLoding=true
                        }
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        if(!isFristPage){
                            isLastPage = true
                        }else{
                            Common.alertErrorOrValidationDialog(
                                this@CategoryByImageActivity,
                                restResponce.getResponseMessage()
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ListResponse<WallpaperModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                isLastPage = true
                Common.alertErrorOrValidationDialog(
                    this@CategoryByImageActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    @SuppressLint("NewApi", "ResourceType")
    fun setCategoryByImageAdaptor(
        imageList: ArrayList<WallpaperModel>, isFristPage: Boolean
    ) {
        if (isFristPage) {
            ImageAdaptor =
                object : BaseAdaptor<WallpaperModel>(this@CategoryByImageActivity, imageList) {
                    override fun onBindData(
                        holder: RecyclerView.ViewHolder?,
                        `val`: WallpaperModel,
                        position: Int
                    ) {
                        val ivWallpaper: ImageView =
                            holder!!.itemView.findViewById(R.id.ivWallpaper)
                        val tvViewCount: TextView = holder.itemView.findViewById(R.id.tvViewCount)

                        val dimensionInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, wallpaperList.get(position).getWallpaper_height()!!.toInt().toFloat(),resources.displayMetrics)
                        ivWallpaper.getLayoutParams().height = dimensionInDp.toInt()
                        Glide.with(this@CategoryByImageActivity).load(wallpaperList.get(position).getWallpaper_image()).placeholder(ColorDrawable(Color.parseColor(wallpaperList.get(position).getWallpaper_color().toString().substring(0,7)))).centerInside().into(ivWallpaper)

                        tvViewCount.text = wallpaperList.get(position).getWallpaper_views()

                        holder.itemView.setOnClickListener {
                            if (Common.isCheckNetwork(this@CategoryByImageActivity)) {
                                callApiCountWallPaper(position)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    this@CategoryByImageActivity,
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
                            if(SharePreference.getBooleanPref(this@CategoryByImageActivity,SharePreference.isLogin)){
                                if(wallpaperList.get(position).getIsFavourite().equals("2")){
                                    if (Common.isCheckNetwork(this@CategoryByImageActivity)) {
                                        callApiFavouriteWallPaper(position)
                                    } else {
                                        Common.alertErrorOrValidationDialog(
                                            this@CategoryByImageActivity,
                                            resources.getString(R.string.no_internet)
                                        )
                                    }
                                }
                            }else{
                                Common.setCommanLogin(this@CategoryByImageActivity)
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
            rvCategoryByImage!!.adapter = ImageAdaptor
            rvCategoryByImage!!.layoutManager = manager
            rvCategoryByImage!!.itemAnimator = DefaultItemAnimator()
            rvCategoryByImage!!.isNestedScrollingEnabled = false
        } else {
            ImageAdaptor!!.notifyDataSetChanged()
        }
    }

    private fun callApiFavouriteWallPaper(pos:Int) {
        Common.showLoadingProgress(this@CategoryByImageActivity)
        val map = HashMap<String, String>()
        map.put("user_id", SharePreference.getStringPref(this@CategoryByImageActivity, SharePreference.userId)!!)
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
                        wallpaperList.get(pos).setIsFavourite("1")
                        ImageAdaptor!!.notifyDataSetChanged()
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@CategoryByImageActivity,
                            restResponce.getResponseMessage()
                        )

                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@CategoryByImageActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (SharePreference.getBooleanPref(this@CategoryByImageActivity, SharePreference.isFavourite)) {
            currentPage = 1
            manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            visibleItemCount = 0;
            totalItemCount = 0;
            pastVisibleItem = 0;
            isLoding = true
            if (Common.isCheckNetwork(this@CategoryByImageActivity)) {
                callApiCategoryBYWallPaper(true, true)
            } else {
                Common.alertErrorOrValidationDialog(
                    this@CategoryByImageActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }

    private fun callApiCountWallPaper(pos: Int) {
        Common.showLoadingProgress(this@CategoryByImageActivity)
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
                        startActivity(Intent(this@CategoryByImageActivity,ImageShowActivity::class.java).putExtra("imagelist",wallpaperList).putExtra("pos",pos.toString()))
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@CategoryByImageActivity,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RestResponseModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@CategoryByImageActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
    private fun setAds() {
        MobileAds.initialize(this@CategoryByImageActivity, "ca-app-pub-8854584886196021/1204732589")
        val adRequest =
            AdRequest.Builder().build()
        ad.loadAd(adRequest)
        ad.setAdListener(object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(errorCode: Int) {
                ad.setVisibility(View.GONE)
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        })
        val adView = AdView(this@CategoryByImageActivity)
        adView.adSize = AdSize.SMART_BANNER
    }
}
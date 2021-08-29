package com.w.app.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.w.app.R
import com.w.app.activity.DashboardActivity
import com.w.app.activity.ImageShowActivity
import com.w.app.api.ApiClient
import com.w.app.api.ListResponse
import com.w.app.api.RestResponse
import com.w.app.base.BaseAdaptor
import com.w.app.base.BaseFragmnet
import com.w.app.model.RestResponseModel
import com.w.app.model.WallpaperModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.fragment_favourite.*
import kotlinx.android.synthetic.main.fragment_favourite.ivMenu
import kotlinx.android.synthetic.main.row_item_image.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavouriteFragment : BaseFragmnet() {
    var ImageAdaptor: BaseAdaptor<WallpaperModel>? = null
    var wallpaperList = ArrayList<WallpaperModel>()
    var rvFavourite: RecyclerView? = null
    var id: String = ""
    var currentPage: Int = 1
    var isLastPage: Boolean? = false
    var manager: StaggeredGridLayoutManager? = null

    var visibleItemCount: Int = 0;
    var totalItemCount: Int = 0;
    var pastVisibleItem: Int = 0;
    var isLoding: Boolean = true
    override fun setView(): Int {
        return R.layout.fragment_favourite
    }

    override fun Init(view: View) {
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        rvFavourite = view.findViewById(R.id.rvFavourite)

        if (Common.isCheckNetwork(activity!!)) {
            callApiFavouriteWallPaper(true,false)
        } else {
            Common.alertErrorOrValidationDialog(
                activity!!,
                resources.getString(R.string.no_internet)
            )
        }

        manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        rvFavourite!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            currentPage++
                            callApiFavouriteWallPaper(false,false)
                        }
                    }
                }
            }
        })
    }

    private fun callApiFavouriteWallPaper(isFristPage: Boolean,isFavourite:Boolean) {
        Common.showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("user_id", SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
        map.put("pageIndex", currentPage.toString())
        map.put("numberOfRecords", "10")
        val call = ApiClient.getClient.getFavouritewallpapers(map)
        call.enqueue(object : Callback<ListResponse<WallpaperModel>> {
            override fun onResponse(
                call: Call<ListResponse<WallpaperModel>>,
                response: Response<ListResponse<WallpaperModel>>
            ) {
                if (response.code() == 200) {
                    rvFavourite!!.visibility = View.VISIBLE
                    tvNoDataFound!!.visibility = View.GONE
                    if (isFavourite) {
                        SharePreference.setBooleanPref(
                            activity!!,
                            SharePreference.isFavourite,
                            false
                        )
                        wallpaperList.clear()
                    }
                    val restResponce: ListResponse<WallpaperModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        if (restResponce.getResponseData().size > 0) {
                            for (i in 0 until restResponce.getResponseData().size) {
                                val wallpaperModel = restResponce.getResponseData().get(i)
                                wallpaperList.add(wallpaperModel)
                            }
                        }
                        setFavouriteWallPaperAdaptor(wallpaperList, isFristPage)
                        isLoding = true
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        if (!isFristPage) {
                            isLastPage = true
                        } else {
                            rvFavourite!!.visibility = View.GONE
                            tvNoDataFound!!.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ListResponse<WallpaperModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                isLastPage = true
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
                rvFavourite!!.visibility = View.GONE
                tvNoDataFound!!.visibility = View.VISIBLE
            }
        })
    }

    @SuppressLint("NewApi", "ResourceType")
    fun setFavouriteWallPaperAdaptor(
        imageList: ArrayList<WallpaperModel>, isFristPage: Boolean
    ) {
        if (isFristPage) {
            ImageAdaptor =
                object : BaseAdaptor<WallpaperModel>(activity!!, imageList) {
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
                            wallpaperList.get(position).getWallpaper_height()!!.toInt().toFloat(),
                            context!!.resources.displayMetrics
                        )
                        ivWallpaper.getLayoutParams().height = dimensionInDp.toInt()
                        Glide.with(activity!!).load(wallpaperList.get(position).getWallpaper_image()).placeholder(ColorDrawable(Color.parseColor(wallpaperList.get(position).getWallpaper_color().toString().substring(0,7)))).centerInside().into(ivWallpaper)

                        tvViewCount.text = wallpaperList.get(position).getWallpaper_views()

                        holder.itemView.setOnClickListener {
                            if (Common.isCheckNetwork(activity!!)) {
                                callApiCountWallPaper(position)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    activity!!,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        }

                        holder.itemView.iv_btn_fevourite.setImageDrawable(resources.getDrawable(R.drawable.ic_fillheart))
                        holder.itemView.iv_btn_fevourite.imageTintList= ColorStateList.valueOf(Color.WHITE)
                        holder.itemView.iv_btn_fevourite.setOnClickListener {

                            if (Common.isCheckNetwork(activity!!)) {
                                callApiisFavouriteWallPaper(position)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    activity!!,
                                    resources.getString(R.string.no_internet)
                                )
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
            rvFavourite!!.adapter = ImageAdaptor
            rvFavourite!!.layoutManager = manager
            rvFavourite!!.itemAnimator = DefaultItemAnimator()
            rvFavourite!!.isNestedScrollingEnabled = false
        } else {
            ImageAdaptor!!.notifyDataSetChanged()
        }
    }

    private fun callApiisFavouriteWallPaper(pos: Int) {
        Common.showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("user_id", wallpaperList.get(pos).getUser_id()!!)
        map.put("wallpaper_id", wallpaperList.get(pos).getId()!!)
        map.put("favourite", "2")
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
                        wallpaperList.removeAt(pos)
                        ImageAdaptor!!.notifyDataSetChanged()
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

    override fun onResume() {
        super.onResume()
        if (SharePreference.getBooleanPref(activity!!, SharePreference.isFavourite)) {
            currentPage = 1
            isLastPage = false
            manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            visibleItemCount = 0;
            totalItemCount = 0;
            pastVisibleItem = 0;
            isLoding = true
            if (Common.isCheckNetwork(activity!!)) {
                callApiFavouriteWallPaper(true, true)
            } else {
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }

    private fun callApiCountWallPaper(pos: Int) {
        Common.showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("wallpaper_id",wallpaperList.get(pos).getId()!!)
        val call = ApiClient.getClient.getWallpaperview(map)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<RestResponseModel>>,
                response: Response<RestResponse<RestResponseModel>>
            ) {
                Common.dismissLoadingProgress()
                startActivity(Intent(activity!!,ImageShowActivity::class.java).putExtra("imagelist",wallpaperList).putExtra("pos",pos.toString()))
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
}
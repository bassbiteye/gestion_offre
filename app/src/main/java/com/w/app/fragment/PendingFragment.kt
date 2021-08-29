package com.w.app.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.w.app.R
import com.w.app.api.ApiClient
import com.w.app.api.ListResponse
import com.w.app.api.RestResponse
import com.w.app.base.BaseAdaptor
import com.w.app.base.BaseFragmnet
import com.w.app.model.RestResponseModel
import com.w.app.model.WallpaperModel
import com.w.app.utils.Common
import com.w.app.utils.SharePreference
import kotlinx.android.synthetic.main.fragment_pending.tvNoDataFound
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PendingFragment:BaseFragmnet() {
    var ImageAdaptor: BaseAdaptor<WallpaperModel>? = null
    var wallpaperList = ArrayList<WallpaperModel>()
    var rvPending:RecyclerView?=null
    var id: String = ""
    var currentPage: Int = 1
    var isLastPage: Boolean? = false
    var manager: StaggeredGridLayoutManager? = null

    var visibleItemCount: Int = 0;
    var totalItemCount: Int = 0;
    var pastVisibleItem: Int = 0;
    var isLoding: Boolean = true
    override fun setView(): Int {
        return R.layout.fragment_pending
    }

    override fun Init(view: View) {
        rvPending=view.findViewById(R.id.rvPending)

        if (Common.isCheckNetwork(activity!!)) {
            callApiPendingWallPaper(true)
        } else {
            Common.alertErrorOrValidationDialog(
                activity!!,
                resources.getString(R.string.no_internet)
            )
        }
        manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvPending!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            callApiPendingWallPaper(false)
                        }
                    }
                }
            }
        })
    }

    private fun callApiPendingWallPaper(isFristPage: Boolean) {
        Common.showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("user_id",SharePreference.getStringPref(activity!!,SharePreference.userId)!!)
        map.put("status","pending")
        map.put("pageIndex",currentPage.toString())
        map.put("numberOfRecords", "10")
        val call = ApiClient.getClient.getMywallpapers(map)
        call.enqueue(object : Callback<ListResponse<WallpaperModel>> {
            override fun onResponse(
                call: Call<ListResponse<WallpaperModel>>,
                response: Response<ListResponse<WallpaperModel>>
            ) {
                if (response.code() == 200) {
                    Common.dismissLoadingProgress()
                    rvPending!!.visibility=View.VISIBLE
                    tvNoDataFound!!.visibility=View.GONE
                    val restResponce:ListResponse<WallpaperModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        if (restResponce.getResponseData().size > 0) {
                            for(i in 0 until restResponce.getResponseData().size){
                                val wallpaperModel=restResponce.getResponseData().get(i)
                                wallpaperList.add(wallpaperModel)
                            }
                        }
                        setPendingAdaptor(wallpaperList,isFristPage)
                        isLoding=true
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        if(!isFristPage){
                            isLastPage = true
                        }else{
                            rvPending!!.visibility=View.GONE
                            tvNoDataFound!!.visibility=View.VISIBLE
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
                rvPending!!.visibility=View.GONE
                tvNoDataFound!!.visibility=View.VISIBLE
            }
        })
    }

    @SuppressLint("NewApi", "ResourceType")
    fun setPendingAdaptor(
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
                        val ivWallpaper: ImageView = holder!!.itemView.findViewById(R.id.ivWallpaper)
                        val ivDelete: ImageView = holder!!.itemView.findViewById(R.id.iv_btn_delete)
                        val tvViewCount: TextView = holder.itemView.findViewById(R.id.tvViewCount)
                        val tvFavouriteCount: TextView = holder.itemView.findViewById(R.id.tvFavouriteCount)

                        val dimensionInDp = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            wallpaperList.get(position).getWallpaper_height()!!.toInt().toFloat(),
                            context!!.resources.displayMetrics
                        )
                        ivWallpaper.getLayoutParams().height = dimensionInDp.toInt()
                        Glide.with(activity!!).load(wallpaperList.get(position).getWallpaper_image()).placeholder(ColorDrawable(Color.parseColor(wallpaperList.get(position).getWallpaper_color().toString().substring(0,7)))).centerInside().into(ivWallpaper)

                        ivDelete.setOnClickListener {
                            if (Common.isCheckNetwork(activity!!)) {
                                callApiDeleteWallPaper(position,wallpaperList.get(position).getId()!!)
                            } else {
                                Common.alertErrorOrValidationDialog(
                                    activity!!,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        }
                        tvViewCount.text = wallpaperList.get(position).getWallpaper_views()
                        tvFavouriteCount.text = wallpaperList.get(position).getWallpaper_likes()
                    }

                    override fun setItemLayout(): Int {
                        return R.layout.row_item_userimage
                    }

                    override fun setNoDataView(): TextView? {
                        return null
                    }

                }
            rvPending!!.adapter = ImageAdaptor
            rvPending!!.layoutManager = manager
            rvPending!!.itemAnimator = DefaultItemAnimator()
            rvPending!!.isNestedScrollingEnabled = false
        } else {
            ImageAdaptor!!.notifyDataSetChanged()
        }
    }

    private fun callApiDeleteWallPaper(pos:Int,strWallPapaerId:String) {
        Common.showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("user_id",SharePreference.getStringPref(activity!!,SharePreference.userId)!!)
        map.put("wallpaper_id",strWallPapaerId)
        val call = ApiClient.getClient.getDeletewallpaper(map)
        call.enqueue(object : Callback<RestResponse<RestResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<RestResponseModel>>,
                response: Response<RestResponse<RestResponseModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<RestResponseModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        successfulDialog(activity!!,restResponce.getResponseMessage(),pos)
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(activity!!,restResponce.getResponseMessage())
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

    fun successfulDialog(
        act: Activity,
        msg: String?,
        pos: Int
    ) {
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
                wallpaperList.removeAt(pos)
                ImageAdaptor!!.notifyDataSetChanged()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}
package com.w.app.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.w.app.R
import com.w.app.activity.CategoryByImageActivity
import com.w.app.activity.DashboardActivity
import com.w.app.api.ApiClient
import com.w.app.api.ListResponse
import com.w.app.base.BaseAdaptor
import com.w.app.base.BaseFragmnet
import com.w.app.model.CategoryModel
import com.w.app.utils.Common
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.ivMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryFragment:BaseFragmnet() {
    var categoryAdaptor:BaseAdaptor<CategoryModel>? = null
    override fun setView(): Int {
       return R.layout.fragment_category
    }
    override fun Init(view: View) {
        setAds()
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        if (Common.isCheckNetwork(activity!!)) {
            callApiCategory()
        } else {
            Common.alertErrorOrValidationDialog(
                activity!!,
                resources.getString(R.string.no_internet)
            )
        }
    }

    private fun callApiCategory() {
        Common.showLoadingProgress(activity!!)
        val call = ApiClient.getClient.getCategory()
        call.enqueue(object : Callback<ListResponse<CategoryModel>> {
            override fun onResponse(call: Call<ListResponse<CategoryModel>>, response: Response<ListResponse<CategoryModel>>) {
                if (response.code() == 200) {
                    val restResponce: ListResponse<CategoryModel> = response.body()!!
                    if (restResponce.getResponseCode().equals("1")) {
                        Common.dismissLoadingProgress()
                        val categoryList=restResponce.getResponseData()
                        setCategoryAdaptor(categoryList)
                    } else if (restResponce.getResponseCode().equals("0")) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            activity!!,
                            restResponce.getResponseMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ListResponse<CategoryModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    @SuppressLint("NewApi", "ResourceType")
    fun setCategoryAdaptor(listCategory: ArrayList<CategoryModel>) {
        categoryAdaptor = object : BaseAdaptor<CategoryModel>(activity!!, listCategory) {
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: CategoryModel,
                position: Int
            ) {
                val ivCategory: ImageView = holder!!.itemView.findViewById(R.id.ivCategory)
                val tvCategoryName: TextView = holder.itemView.findViewById(R.id.tvCategory)
                val llCategory: LinearLayout = holder.itemView.findViewById(R.id.llCategory)

                tvCategoryName.text = listCategory.get(position).getCategory_name()
                Glide.with(activity!!).load(listCategory.get(position).getCategory_image()).placeholder(activity!!.resources.getDrawable(R.drawable.ic_placeholder)).into(ivCategory)
                llCategory.setOnClickListener {
                    startActivity(Intent(activity!!,CategoryByImageActivity::class.java).putExtra("id",listCategory.get(position).getId()).putExtra("name",listCategory.get(position).getCategory_name()))
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_item_category
            }

            override fun setNoDataView(): TextView? {
                return null
            }

        }
        rvCategory.adapter = categoryAdaptor
        rvCategory.layoutManager = GridLayoutManager(activity!!,2, GridLayoutManager.VERTICAL,false)
        rvCategory.itemAnimator = DefaultItemAnimator()
        rvCategory.isNestedScrollingEnabled = false
    }

    private fun setAds() {
        MobileAds.initialize(activity, "ca-app-pub-8854584886196021/1204732589")
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
        val adView = AdView(activity)
        adView.adSize = AdSize.SMART_BANNER
    }
}
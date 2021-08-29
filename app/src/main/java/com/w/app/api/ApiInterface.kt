package com.w.app.api

import com.w.app.model.CategoryModel
import com.w.app.model.RestResponseModel
import com.w.app.model.WallPaperDetail
import com.w.app.model.WallpaperModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    //Login Api 1
    @POST("login.php")
    fun getLogin(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //Registration Api 2
    @POST("register.php")
    fun setRegistration(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //ForgetPassword Api 3
    @POST("forgetpassword.php")
    fun setForgetPassword(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //Chnagepassword  Api 4
    @POST("changepassword.php")
    fun getChangePassword(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //GetProfile Api 5
    @POST("getprofile.php")
    fun getProfile(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //setNotificationStatus Api 6
    @POST("notificationstatus.php")
    fun setNotification(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //Edit Profile Api 7
    @Multipart
    @POST("editprofile.php")
    fun setProfile(@Part("user_id") userId: RequestBody,@Part("username") name: RequestBody, @Part profileimage: MultipartBody.Part?): Call<RestResponse<RestResponseModel>>

    //Get Category Api 8
    @GET("getcategories.php")
    fun getCategory(): Call<ListResponse<CategoryModel>>

    //Get Latest Wallpaper Api 9
    @POST("getlatestwallpapers.php")
    fun getLatestwallpapers(@Body map: HashMap<String, String>): Call<ListResponse<WallpaperModel>>

    //Get Trending Wallpaper Api 10
    @POST("gettrendingwallpapers.php")
    fun getTrendingwallpapers(@Body map: HashMap<String, String>): Call<ListResponse<WallpaperModel>>

    //Get Wallpaper By Category Api 11
    @POST("getwallpaperbycategory.php")
    fun getWallpaperbycategory(@Body map: HashMap<String, String>): Call<ListResponse<WallpaperModel>>

    //Get Favouritewallpapers Api 12
    @POST("getfavouritewallpapers.php")
    fun getFavouritewallpapers(@Body map: HashMap<String, String>): Call<ListResponse<WallpaperModel>>

    //Get My wallpapers Api 13
    @POST("getmywallpapers.php")
    fun getMywallpapers(@Body map: HashMap<String, String>): Call<ListResponse<WallpaperModel>>

    //Get Upload wallpaper Api 14
    @Multipart
    @POST("uploadwallpaper.php")
    fun getUploadwallpaper(@Part("user_id") userId: RequestBody,
                           @Part("category_id") category_id: RequestBody,
                           @Part("wallpaper_color") wallpaper_color: RequestBody,
                           @Part("wallpaper_height") height: RequestBody,
                           @Part("name") name: RequestBody,
                           @Part wallpaperImage: MultipartBody.Part?): Call<RestResponse<RestResponseModel>>

    //Get Delete wallpapers Api 15
    @POST("deletewallpaper.php")
    fun getDeletewallpaper(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //Get Favourite unFavourite wallpapers Api 16
    @POST("favouriteunfavourite.php")
    fun getFavouriteunFavourite(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>

    //Get Wallpaperdetail Api 17
    @POST("userprofile.php")
    fun getWallpaperdetail(@Body map: HashMap<String, String>): Call<RestResponse<WallPaperDetail>>

    //Get wallpaperview Api 18
    @POST("wallpaperview.php")
    fun getWallpaperview(@Body map: HashMap<String, String>): Call<RestResponse<RestResponseModel>>
}
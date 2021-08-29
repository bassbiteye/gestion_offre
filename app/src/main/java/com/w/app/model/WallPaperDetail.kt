package com.w.app.model

class WallPaperDetail {

    private var related_wallpapers: ArrayList<WallpaperModel>?=null

    private var total_wallpapers: String? = null

    private var user_image: String? = null

    private var user_name: String? = null

    private var total_views: String? = null

    private var total_likes: String? = null

    fun getRelated_wallpapers(): ArrayList<WallpaperModel>? {
        return related_wallpapers
    }

    fun setRelated_wallpapers(related_wallpapers: ArrayList<WallpaperModel>?) {
        this.related_wallpapers = related_wallpapers
    }

    fun getTotal_wallpapers(): String? {
        return total_wallpapers
    }

    fun setTotal_wallpapers(total_wallpapers: String?) {
        this.total_wallpapers = total_wallpapers
    }

    fun getUser_image(): String? {
        return user_image
    }

    fun setUser_image(user_image: String?) {
        this.user_image = user_image
    }

    fun getUser_name(): String? {
        return user_name
    }

    fun setUser_name(user_name: String?) {
        this.user_name = user_name
    }

    fun getTotal_views(): String? {
        return total_views
    }

    fun setTotal_views(total_views: String?) {
        this.total_views = total_views
    }

    fun getTotal_likes(): String? {
        return total_likes
    }

    fun setTotal_likes(total_likes: String?) {
        this.total_likes = total_likes
    }

}
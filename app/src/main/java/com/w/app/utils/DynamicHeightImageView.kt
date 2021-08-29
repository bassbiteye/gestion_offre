package com.w.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

@SuppressLint("AppCompatCustomView")
class DynamicHeightImageView : ImageView {

    private var whRatio = 0f

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(context: Context?) : super(context)

    fun setRatio(ratio: Float) {
        whRatio = ratio
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (whRatio != 0f) {
            val width = measuredWidth
            val height = (whRatio * width).toInt()
            setMeasuredDimension(width, height)
        }
    }
}
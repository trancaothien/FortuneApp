package com.cannshine.Fortune.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

class InteractiveScrollView : ScrollView {
    var reachBottom = false

    // Getters & Setters
    var onBottomReachedListener: OnBottomReachedListener? = null

    constructor(context: Context?, attrs: AttributeSet?,
                defStyle: Int) : super(context, attrs, defStyle) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?) : super(context) {}

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        val view = getChildAt(childCount - 1) as View
        val diff = view.bottom - (height + scrollY)
        if (diff <= 0) {
            if (onBottomReachedListener != null && !reachBottom) {
                onBottomReachedListener!!.onBottomReached()
                reachBottom = true
            }
        } else reachBottom = false
        super.onScrollChanged(l, t, oldl, oldt)
    }

    /**
     * Event listener.
     */
    interface OnBottomReachedListener {
        fun onBottomReached()
    }
}
package com.cannshine.fortune.VolleyRequest

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class ApplicationController private constructor(private val mContext: Context) {
    private var requestQueue: RequestQueue?
    val imageLoader: ImageLoader
    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mContext.applicationContext)
        }
        return requestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue()!!.add(req)
    }

    companion object {
        private var mApplicationController: ApplicationController? = null
        @Synchronized
        fun getInstance(context: Context): ApplicationController? {
            if (mApplicationController == null) {
                mApplicationController = ApplicationController(context)
            }
            return mApplicationController
        }
    }

    init {
        requestQueue = getRequestQueue()
        imageLoader = ImageLoader(requestQueue, object : ImageLoader.ImageCache {
            private val cache = LruCache<String, Bitmap>(20)
            override fun getBitmap(url: String): Bitmap? {
                return cache[url]
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        })
    }
}
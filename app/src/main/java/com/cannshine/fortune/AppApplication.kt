package com.cannshine.fortune

import android.app.Application
import com.cannshine.fortune.model.AdsInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppApplication:Application () {
    companion object
    {
        public lateinit var application: AppApplication
        fun getApp():AppApplication
        {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        application=this
    }

}
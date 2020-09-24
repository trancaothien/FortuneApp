package com.cannshine.fortune.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cannshine.fortune.API
import com.cannshine.fortune.model.Status
import com.cannshine.fortune.model.StatusBanner
import retrofit2.Call
import retrofit2.Callback


class SplashViewModel(application: Application) : AndroidViewModel(application) {
    fun getAdsInfotwo(success: (Status?) -> Unit, fail: () -> Unit, act: String, appid: Int, os: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getAdsinfo(act, appid, os)
        call.enqueue(object : Callback<Status> {
            override fun onResponse(call: Call<Status>, response: retrofit2.Response<Status>) {
                if (response.isSuccessful) {
                    success(response.body())
                }

            }

            override fun onFailure(call: Call<Status>, t: Throwable) {
                Log.d("retrofitError", "onFailure: " + t.toString())
                fail()
            }
        })
    }

    fun getBanner(success: (StatusBanner?) -> Unit, fail: () -> Unit, requestBanner: String, appid: Int, type: Int, os: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getBannerAds(requestBanner, appid, type, os)
        call.enqueue(object : Callback<StatusBanner> {
            override fun onResponse(call: Call<StatusBanner>, response: retrofit2.Response<StatusBanner>) {
                if (response.isSuccessful) {
                    success(response.body())
                }
            }

            override fun onFailure(call: Call<StatusBanner>, t: Throwable) {
                fail()
            }

        })
    }

}


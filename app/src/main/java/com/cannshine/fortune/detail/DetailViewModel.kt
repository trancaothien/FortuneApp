package com.cannshine.fortune.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cannshine.fortune.API
import com.cannshine.fortune.model.BannerAds
import com.cannshine.fortune.model.ClickAds
import com.cannshine.fortune.model.StatusBanner
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    fun getBanner(success: (List<BannerAds>) -> Unit, requestBanner: String, appid: Int, type: Int, os: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getBannerAdsDetail(requestBanner, appid, type, os, "ads", 1, "400x400")
        call.enqueue(object : retrofit2.Callback<StatusBanner> {
            override fun onResponse(call: Call<StatusBanner>, response: Response<StatusBanner>) {
                val listBanner = response.body()?.payload
                if (listBanner != null) {
                    success(listBanner)
                }
            }

            override fun onFailure(call: Call<StatusBanner>, t: Throwable) {

            }

        })
    }

    fun clickAds(id: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.clickAds("clickads", id, 1, "android")
        call.enqueue(object : retrofit2.Callback<ClickAds> {
            override fun onResponse(call: Call<ClickAds>, response: retrofit2.Response<ClickAds>) {
                val clickAds = response.body()
            }

            override fun onFailure(call: Call<ClickAds>, t: Throwable) {

            }

        })
    }
}
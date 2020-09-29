package com.cannshine.fortune.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cannshine.fortune.API
import com.cannshine.fortune.model.Status
import com.cannshine.fortune.model.StatusBanner
import com.cannshine.fortune.utils.Global
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
                Log.d("retrofitError", "onFailure: $t")
                fail()
            }
        })
    }

    fun getBanner(success: () -> Unit, fail: () -> Unit, requestBanner: String, appid: Int, type: Int, os: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getBannerAds(requestBanner, appid, type, os)
        call.enqueue(object : Callback<StatusBanner> {
            override fun onResponse(call: Call<StatusBanner>, response: retrofit2.Response<StatusBanner>) {
                if (response.isSuccessful) {
                    val arrayBannerAds = response.body()?.payload
                    if (arrayBannerAds != null) {
                        for (bannerAds in arrayBannerAds) {
                            val link = bannerAds.link
                            val photoLink = bannerAds.photo_link
                            Log.d("linkPhoto", "onCreate: $link")
                            val idAds = bannerAds.id
                            Global.LINK = link
                            Global.PHOTO_LINK = photoLink
                            Global.ID_ADS = idAds.toString()
                        }
                    }
                    success()
                }
            }

            override fun onFailure(call: Call<StatusBanner>, t: Throwable) {
                fail()
            }

        })
    }

}


package com.cannshine.fortune.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cannshine.fortune.API
import com.cannshine.fortune.model.Status
import com.cannshine.fortune.model.StatusBanner
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback


class SplashViewModel(application: Application) :AndroidViewModel(application)
{
    fun getAdsInfotwo(success: (JSONObject) -> Unit, fail: () -> Unit){
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getAdsinfo()
        call.enqueue(object : Callback<Status> {
            override fun onResponse(call: Call<Status>, response: retrofit2.Response<Status>) {
                if (response.isSuccessful) {
                    val result = JSONObject(response.body().toString())
                    success(result)
                }

            }

            override fun onFailure(call: Call<Status>, t: Throwable) {
                Log.d("retrofitError", "onFailure: " + t.toString())
                fail()
            }
        })
    }

    public fun getBanner(success: (JSONObject) -> Unit, fail: () -> Unit){
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getBannerAds()
        call.enqueue(object : Callback<StatusBanner> {
            override fun onResponse(call: Call<StatusBanner>, response: retrofit2.Response<StatusBanner>) {
                if (response.isSuccessful) {
                    val result = JSONObject(response.body().toString())
                    success(result)
                }
            }

            override fun onFailure(call: Call<StatusBanner>, t: Throwable) {
                fail()
            }

        })
    }

}


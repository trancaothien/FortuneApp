package com.cannshine.fortune

import com.cannshine.fortune.model.Status
import com.cannshine.fortune.model.StatusBanner
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

object API {
    const val URL = "https://yeah1app.com/apps/api/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }

    interface AppRepository {
        @Headers("apikey:164e-2d87-5508-2b5c-8e8c-0fbd-12a0-fa68-59dc-4267",
        "Content-Type: application/x-www-form-urlencoded")
        @GET("index.php?act=requestAdNets&appid=1&os=android")
        fun getAdsinfo(): retrofit2.Call<Status>

        @Headers("apikey:164e-2d87-5508-2b5c-8e8c-0fbd-12a0-fa68-59dc-4267",
                "Content-Type: application/x-www-form-urlencoded")
        @GET("index.php?act=requestBanner&type=3&appid=1&os=android")
        fun getBannerAds(): Call<StatusBanner>
    }
}
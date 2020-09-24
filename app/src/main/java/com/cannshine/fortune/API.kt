package com.cannshine.fortune

import com.cannshine.fortune.model.Status
import com.cannshine.fortune.model.StatusBanner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object API {
    private const val URL = "https://yeah1app.com/apps/api/"
    private const val APIKEY = "164e-2d87-5508-2b5c-8e8c-0fbd-12a0-fa68-59dc-4267"
    private fun createClient(): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY // show log request api
        okHttpClient.addInterceptor(logger)
        okHttpClient.addInterceptor {
            val requestBuilder = it.request().newBuilder() // add apikey in header
            requestBuilder.addHeader("apikey", APIKEY)
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded")
            it.proceed(requestBuilder.build())
        }
        return okHttpClient.build()
    }

    private val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

    interface AppRepository {
        @GET("index.php")//?act=requestAdNets&appid=1&os=android
        fun getAdsinfo(@Query("act") act: String, @Query("appid") appid: Int, @Query("os") os: String): Call<Status>

        @GET("index.php")//?act=requestBanner&type=3&appid=1&os=android
        fun getBannerAds(@Query("act") requestBanner: String, @Query("appId") appid: Int, @Query("type") type: Int, @Query("os") os: String): Call<StatusBanner>
    }
}
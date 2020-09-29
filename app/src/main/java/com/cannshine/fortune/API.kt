package com.cannshine.fortune

import com.cannshine.fortune.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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
        fun getAdsinfo(@Query("act") act: String,
                       @Query("appid") appid: Int,
                       @Query("os") os: String
        ): Call<Status>

        @GET("index.php")//?act=requestBanner&type=3&appid=1&os=android
        fun getBannerAds(@Query("act") requestBanner: String,
                         @Query("appId") appid: Int,
                         @Query("type") type: Int,
                         @Query("os") os: String
        ): Call<StatusBanner>

        @FormUrlEncoded
        @POST("index.php")
        fun createUser(@Field("act") act: String,
                       @Field("os") os: String,
                       @Field("deviceid") deviceid: String,
                       @Field("location") location: String,
                       @Field("appid") appid: Int
        ): Call<UserStatus>

        @FormUrlEncoded
        @POST("index.php")
        fun updateFCM(@Field("act") act: String,
                      @Field("userkey") userkey: String,
                      @Field("deviceid") deviceid: String,
                      @Field("fcm") fcm: String
        ): Call<UpdateFCM>

        @FormUrlEncoded
        @POST("index.php")
        fun clickAds(@Field("act") act: String,
                     @Field("bannerid") bannerid: String,
                     @Field("appid") appid: Int,
                     @Field("os") os: String
        ): Call<ClickAds>

        @GET("index.php")
        fun getAppVersion(@Query("act") act: String,
                          @Query("appid") appid: String,
                          @Query("os") os: String
        ): Call<GetAppVersionStatus>

        @FormUrlEncoded
        @POST("index.php")
        fun getBannerAdsDetail(
                @Query("act") requestBanner: String,
                @Query("appid") appid: Int,
                @Query("type") type: Int,
                @Query("os") os: String,
                @Field("action") action: String,
                @Field("type") typeBody: Int,
                @Field("size") size: String
        ): Call<StatusBanner>
    }
}
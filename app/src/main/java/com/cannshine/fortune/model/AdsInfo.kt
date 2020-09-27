package com.cannshine.fortune.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AdsInfo(
        @SerializedName("id") var id: Int,
        @SerializedName("code") var code: String,
        @SerializedName("ads_info") var ads_info: String)

data class Status(
        @SerializedName("payload") var payload: List<AdsInfo>,
        @SerializedName("error") var error: Int,
        @SerializedName("message") var message: String)

data class StartAppData(@SerializedName("app_id") val appId: String,
                        @SerializedName("dev_id") val devId: String)

data class AdmobData(
        @SerializedName("app_id") val appId: String,
        @SerializedName("banner_id") val bannerId: String,
        @SerializedName("interstitial_id") val interstitialId: String)
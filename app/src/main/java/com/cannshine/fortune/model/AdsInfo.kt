package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AdsInfo(
        @SerializedName("id")
        var id: Int,
        @SerializedName("code")
        var code: String,
        @SerializedName("ads_info")
        var ads_info: String)

class Status(
        @SerializedName("payload")
        var payload: List<AdsInfo>,
        @SerializedName("error")
        var error: Int,
        @SerializedName("message")
        var message: String
)
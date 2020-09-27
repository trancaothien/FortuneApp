package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

class ClickAds(
        @SerializedName("error") var error: Int,
        @SerializedName("message") var message: String,
        @SerializedName("payload") var payload: String)
package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

data class UpdateFCM(
        @SerializedName("error") var error: Int,
        @SerializedName("message") var message: String,
        @SerializedName("payload") var payload: String)
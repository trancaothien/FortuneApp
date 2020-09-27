package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

class BodyUser (@SerializedName("deviceid") var deviceid: String,
                @SerializedName("location") var location: String,
                @SerializedName("appid") var appid: String)
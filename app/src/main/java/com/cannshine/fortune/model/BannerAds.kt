package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

class BannerAds(
        @SerializedName("id")
        var id: Int,
        @SerializedName("name")
        var name: String,
        @SerializedName("link")
        var link: String,
        @SerializedName("photo_link")
        var photo_link: String,
        @SerializedName("type")
        var type: Int,
        @SerializedName("is_active")
        var is_active: Int) {
}

class StatusBanner(
        @SerializedName("payload")
        var payload: List<BannerAds>,
        @SerializedName("error")
        var error: Int,
        @SerializedName("message")
        var message: String
)
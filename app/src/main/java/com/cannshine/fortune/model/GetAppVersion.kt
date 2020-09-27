package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

data class GetAppVersionStatus(@SerializedName("result") var result: Boolean,
                          @SerializedName("payload") var payload: AppVersionInfo,
                          @SerializedName("message") var message: String)

data class AppVersionInfo(@SerializedName("app_id") var app_id: String,
                     @SerializedName("platform") var platform: String,
                     @SerializedName("version_check") var version_check: String,
                     @SerializedName("version_name") var version_name: String,
                     @SerializedName("need_update") var need_update: String,
                     @SerializedName("store_url") var store_url: String
)

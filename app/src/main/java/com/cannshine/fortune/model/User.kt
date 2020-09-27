package com.cannshine.fortune.model

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("user_id") var user_id: String,
        @SerializedName("user_key") var user_key: String
)

data class UserStatus(
        @SerializedName("result") var result: Boolean,
        @SerializedName("payload") var payload: User,
        @SerializedName("message") var message: String
)
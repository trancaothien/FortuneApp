package com.cannshine.fortune.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Hexagram(
        var h_ID: String? = "",
        var number: String? = "",
        var h_name: String? = "",
        var h_mean: String? = "",
        var h_description: String? = "",
        var h_content: String? = "",
        var h_wao1: String? = "",
        var h_wao2: String? = "",
        var h_wao3: String? = "",
        var h_wao4: String? = "",
        var h_wao5: String? = "",
        var h_wao6: String? = ""
)
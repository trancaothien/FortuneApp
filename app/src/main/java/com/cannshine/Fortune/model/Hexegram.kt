package com.cannshine.Fortune.model

class Hexegram {
    var h_ID: String? = null
    var number = 0
    var h_name: String? = null
    var h_mean: String? = null
    var h_description: String? = null
    var h_content: String? = null
    var h_wao1: String? = null
    var h_wao2: String? = null
    var h_wao3: String? = null
    var h_wao4: String? = null
    var h_wao5: String? = null
    var h_wao6: String? = null

    constructor(h_ID: String?, h_name: String?, h_mean: String?, h_description: String?,
                h_content: String?, h_wao1: String?, h_wao2: String?, h_wao3: String?,
                h_wao4: String?, h_wao5: String?, h_wao6: String?) {
        this.h_ID = h_ID
        this.h_name = h_name
        this.h_mean = h_mean
        this.h_description = h_description
        this.h_content = h_content
        this.h_wao1 = h_wao1
        this.h_wao2 = h_wao2
        this.h_wao3 = h_wao3
        this.h_wao4 = h_wao4
        this.h_wao5 = h_wao5
        this.h_wao6 = h_wao6
    }

    constructor() {}
}
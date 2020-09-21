package com.cannshine.Fortune.utils

class Global {
    companion object{
        // apikey
        const val APIKEY = "164e-2d87-5508-2b5c-8e8c-0fbd-12a0-fa68-59dc-4267"

        // url
        const val URL_SPLASH_GET_ADS = "https://yeah1app.com/apps/api/index.php?act=requestAdNets&appid=1&os=android"
        const val URL_MAIN_CREATE_USER = "http://yeah1app.com/apps/api/index.php"
        const val URL_MAIN_GET_VERSION = "https://yeah1app.com/apps/api/index.php?act=checkAppVersion&appid=1&os=android"
        const val URL_DETAIL_GET_ADS_BANNER = "http://yeah1app.com/apps/api/index.php?act=requestBanner&type=2&appid=1&os=android"
        const val URL_DETAIL_CLICK_ADS = "http://yeah1app.com/apps/api/index.php"
        const val URL_UPDATE_FCM = "http://yeah1app.com/apps/api/index.php"
        const val URL_SPLASH_GET_BANNER = "http://yeah1app.com/apps/api/index.php?act=requestBanner&type=3&appid=1&os=android"

        // userSharedPreferences
        const val KEY_USER = "user"
        const val K_USERID = "userId"
        const val K_USERKEY = "userKey"

        // adsSharedPreferences
        const val KEY_ADMOB = "admob"
        const val KEY_STARTAPP = "startapp"
        const val ADMOB_APP_ID = "admobAppId"
        const val ADMOB_BANNER_ID = "admobBannerId"
        const val ADMOB_INTERSTITIAL_ID = "admobInterstitialId"
        const val STARTAPP_APP_ID = "startAppId"
        const val STARTAPP_DEV_ID = "startAppDevId"

        // deviceId
        const val KEY_DEVICE = "device"
        const val K_DEVICE_ID = "deviceId"

        //token
        const val FLAG_TOKEN = "token"
        const val K_TOKEN = "ktoken"
        const val FCM = "fcm"

        // link and photoLink of Ads Gieo-que-nhanh
        var LINK: String? = null
        var PHOTO_LINK: String? = null
        var ID_ADS: String? = null
    }
}
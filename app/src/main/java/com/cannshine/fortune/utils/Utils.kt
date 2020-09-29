package com.cannshine.fortune.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.provider.Settings
import android.util.Log

class Utils {
    companion object{
        fun saveUserInfo(context: Context, key: String?, userId: String, userKey: String) {
            Log.d("user", "saveUserInfo: $userId $userKey")
            val sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Global.K_USERID, userId)
            editor.putString(Global.K_USERKEY, userKey)
            editor.apply()
        }

        fun getUserInfo(context: Context, key: String?): Boolean {
            val sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString(Global.K_USERID, "")
            val userKey = sharedPreferences.getString(Global.K_USERKEY, "")
            Log.d("userCheck", "getUserInfo: $userId $userKey")
            return if (userId == "" && userKey == "") {
                false
            } else true
        }

        fun admobSaveKey(context: Context, adsKey: String?, appId: String?, bannerId: String?, interstitialId: String?) {
            val editor = context.getSharedPreferences(adsKey, Context.MODE_PRIVATE).edit()
            editor.putString(Global.ADMOB_APP_ID, appId)
            editor.putString(Global.ADMOB_BANNER_ID, bannerId)
            editor.putString(Global.ADMOB_INTERSTITIAL_ID, interstitialId)
            editor.apply()
        }

        fun startappSaveKey(context: Context, adsKey: String?, appId: String?, devId: String?) {
            val editor = context.getSharedPreferences(adsKey, Context.MODE_PRIVATE).edit()
            editor.putString(Global.STARTAPP_APP_ID, appId)
            editor.putString(Global.STARTAPP_DEV_ID, devId)
            editor.apply()
        }

        fun getAdsInfo(context: Context, adsKey: String?): Map<String, String?> {
            val preferences = context.getSharedPreferences(adsKey, Context.MODE_PRIVATE)
            return preferences.all as Map<String, String?>
        }

        @SuppressLint("HardwareIds")
        fun getDeviceId(context: Context): String? {
            val m_androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val preferences = context.getSharedPreferences(Global.KEY_DEVICE, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            val deviceId = preferences.getString(Global.K_DEVICE_ID, "")
            return if (deviceId == "") {
                editor.putString(Global.K_DEVICE_ID, m_androidId)
                editor.apply()
                m_androidId
            } else {
                deviceId
            }
        }

        fun getFlagToken(context: Context): Map<String, String?> {
            val preferences = context.getSharedPreferences(Global.FLAG_TOKEN, Context.MODE_PRIVATE)
            return preferences.all as Map<String, String?>
        }

        fun setFlagToken(context: Context, value: String) {
            Log.d("flagtoken", "setFlagToken: $value")
            val editor = context.getSharedPreferences(Global.FLAG_TOKEN, Context.MODE_PRIVATE).edit()
            editor.putString(Global.K_TOKEN, value)
            editor.apply()
        }

        fun saveNewToken(context: Context, token: String?) {
            val editor = context.getSharedPreferences(Global.FLAG_TOKEN, Context.MODE_PRIVATE).edit()
            editor.putString(Global.FCM, token)
            editor.apply()
        }

        fun getNewToken(context: Context): String? {
            val preferences = context.getSharedPreferences(Global.FLAG_TOKEN, Context.MODE_PRIVATE)
            val token = preferences.getString(Global.FCM, "")
            Log.d("newtoken", "getNewToken: $token")
            return token
        }

        fun getFontType(context: Context): Typeface{
            return Typeface.createFromAsset(context.assets, "UTM Azuki.ttf")
        }
    }
}
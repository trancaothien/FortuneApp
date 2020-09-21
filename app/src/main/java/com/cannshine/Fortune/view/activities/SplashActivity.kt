package com.cannshine.Fortune.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.cannshine.Fortune.model.AdsInfo
import com.cannshine.Fortune.utils.CheckInternet
import com.cannshine.Fortune.utils.Global
import com.cannshine.Fortune.utils.Utils
import com.cannshine.Fortune.R
import com.cannshine.Fortune.VolleyRequest.ApplicationController
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var myTimer: CountDownTimer? = null
    var finishSplashTime = false
    var finishGetAdsInfo = false
    var finishGetBanner = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //chuyển qua MainMenuActivity
        starActivity()
        if (CheckInternet.isConnected(this)) {
            //lấy thông tin gieo quẻ nhanh
            banner
            // getAdsinfo
            adsinfo
        } else {
            if (finishSplashTime) {
                goToMainMenu()
            }
        }
    }

    fun starActivity() {
        myTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                if (CheckInternet.isConnected(this@SplashActivity)) {
                    if (finishGetAdsInfo == true && finishGetBanner == true) {
                        goToMainMenu()
                    } else {
                        finishSplashTime = true
                    }
                } else {
                    goToMainMenu()
                }
            }
        }.start()
    }

    private val adsinfo: Unit
        private get() {
            val stringRequest: StringRequest = object : StringRequest(Method.GET, Global.URL_SPLASH_GET_ADS, Response.Listener { response ->
                Log.d("responseSplash", "onResponse: $response")
                try {
                    readRequest(response)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (finishSplashTime == true && finishGetBanner == true) {
                    goToMainMenu()
                } else {
                    finishGetAdsInfo = true
                }
            }, Response.ErrorListener { error ->
                Log.d("responseSplashError", "onResponse: $error")
                if (finishSplashTime == true && finishGetBanner == true) {
                    goToMainMenu()
                } else {
                    finishGetAdsInfo = true
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["act"] = "requestAdNets"
                    params["appid"] = "1"
                    params["os"] = "android"
                    return params
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["apikey"] = Global.APIKEY
                    return headers
                }
            }
            ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
        }

    @Throws(JSONException::class)
    private fun readRequest(response: String) {
        var arrayAds: ArrayList<AdsInfo>? = null
        val jsonObject = JSONObject(response)
        val jsonArray = jsonObject.optJSONArray("payload")
        for (i in 0 until jsonArray.length()) {
            var jsonObjectAds: JSONObject? = null
            jsonObjectAds = jsonArray.opt(i) as JSONObject
            if (jsonObjectAds != null) {
                val id = jsonObjectAds.optInt("id")
                val code = jsonObjectAds.optString("code")
                val ads_info = jsonObjectAds.optString("ads_info").trim { it <= ' ' }
                val ads = AdsInfo(id, code, ads_info)
                arrayAds = ArrayList()
                arrayAds.add(ads)
            }
        }
        if (arrayAds != null) {
            for (i in arrayAds.indices) {
                val code = arrayAds[i].code.trim { it <= ' ' }
                if (code == "admob") {
                    val `object` = JSONObject(arrayAds[i].ads_info)
                    val abmobAppId = `object`.optString("app_id")
                    val admobBannerId = `object`.optString("banner_id")
                    val admobIntertitalId = `object`.optString("interstitial_id")
                    if (abmobAppId != null && admobBannerId != null && admobIntertitalId != null) {
                        Utils.admobSaveKey(this, Global.KEY_ADMOB, abmobAppId, admobBannerId, admobIntertitalId)
                    }
                }
                if (code == "startapp") {
                    val `object` = JSONObject(arrayAds[i].ads_info)
                    val startAppId = `object`.optString("app_id")
                    val startAppDevId = `object`.optString("dev_id")
                    if (startAppId != null && startAppDevId != null) {
                        Utils.startappSaveKey(this, Global.KEY_STARTAPP, startAppId, startAppDevId)
                    }
                }
            }
        }
    }

    fun goToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val banner: Unit
        private get() {
            val stringRequest: StringRequest = object : StringRequest(Method.GET, Global.URL_SPLASH_GET_BANNER, Response.Listener { response ->
                Log.d("gieo que nhanh", "onResponse: $response")
                val infoPhoto: List<String>?
                try {
                    infoPhoto = readRequestBanner(response)
                    if (infoPhoto != null) {
                        Global.LINK = infoPhoto[0]
                        Global.PHOTO_LINK = infoPhoto[1]
                        Global.ID_ADS = infoPhoto[2]
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (finishGetAdsInfo && finishSplashTime) {
                    goToMainMenu()
                } else {
                    finishGetBanner = true
                }
            }, Response.ErrorListener {
                if (finishGetAdsInfo == true && finishSplashTime == true) {
                    goToMainMenu()
                } else {
                    finishGetBanner = true
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val param = HashMap<String, String>()
                    param["apikey"] = Global.APIKEY
                    return param
                }
            }
            ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
        }

    @Throws(JSONException::class)
    private fun readRequestBanner(response: String): List<String>? {
        var infoPhoto: MutableList<String>? = null
        val adsObject = JSONObject(response)
        val adsArray = adsObject.optJSONArray("payload")
        for (i in 0 until adsArray.length()) {
            val adsDetail = adsArray.opt(i) as JSONObject
            if (adsDetail != null) {
                infoPhoto = ArrayList()
                val link = adsDetail.optString("link").trim { it <= ' ' }
                val photoLink = adsDetail.optString("photo_link").trim { it <= ' ' }
                val idAds = adsDetail.optString("id").trim { it <= ' ' }
                infoPhoto.add(link)
                infoPhoto.add(photoLink)
                infoPhoto.add(idAds)
            }
        }
        return infoPhoto
    }
}
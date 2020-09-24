package com.cannshine.fortune.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cannshine.fortune.model.AdsInfo
import com.cannshine.fortune.utils.CheckInternet
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.R
import com.cannshine.fortune.base.BaseActivity
import com.cannshine.fortune.mainmenu.MainMenuActivity
import com.cannshine.fortune.model.AdmobData
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import kotlin.collections.ArrayList

class SplashActivity : BaseActivity() {
    private var myTimer: CountDownTimer? = null
    var finishSplashTime = false
    var finishGetAdsInfo = false
    var finishGetBanner = false
    lateinit var splashViewModel: SplashViewModel
    override fun getContentView(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        //chuyển qua MainMenuActivity
        starActivity()
        if (CheckInternet.isConnected(this)) {
            //lấy thông tin gieo quẻ nhanh
            splashViewModel.getBanner(success = {
                it?.let {
                    val arrayBannerAds = it.payload
                    for (bannerAds in arrayBannerAds) {
                        val link = bannerAds.link
                        val photoLink = bannerAds.photo_link
                        Log.d("linkPhoto", "onCreate: " + link)
                        val idAds = bannerAds.id
                        Global.LINK = link
                        Global.PHOTO_LINK = photoLink
                        Global.ID_ADS = idAds.toString()
                    }
                    if (finishGetAdsInfo && finishSplashTime) {
                        goToMainMenu()
                    } else {
                        finishGetBanner = true
                    }
                }

            }, fail = {
                if (finishGetAdsInfo && finishSplashTime) {
                    goToMainMenu()
                } else {
                    finishGetBanner = true
                }
            }, "requestBanner", 1, 3, "android")

            splashViewModel.getAdsInfotwo(success = {
                it?.let {
                    var arrayAds: ArrayList<AdsInfo>? = ArrayList()
                    val payloads = it.payload
                    for (adsInfo in payloads) {
                        val id = adsInfo.id
                        val code = adsInfo.code
                        val ads_info = adsInfo.ads_info
                        val ads = AdsInfo(id, code, ads_info)
                        arrayAds = ArrayList()
                        arrayAds.add(ads)
                    }
                    if (arrayAds != null) {
                        for (i in arrayAds.indices) {
                            val code = arrayAds[i].code.trim { it <= ' ' }
                            if (code == "admob") {
                                val admob = Gson().fromJson(arrayAds[i].ads_info, AdmobData::class.java)
                                Log.e("adsInfo", admob.toString())
                                val abmobAppId = admob.appId
                                val admobBannerId = admob.bannerId
                                val admobIntertitalId = admob.interstitialId
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
                    if (finishSplashTime && finishGetBanner) {
                        goToMainMenu()
                    } else {
                        finishGetAdsInfo = true
                    }
                }

            }, fail = {
                if (finishSplashTime && finishGetBanner) {
                    goToMainMenu()
                } else {
                    finishGetAdsInfo = true;
                }
            }, "requestAdNets", 1, "android")
        } else {
            if (finishSplashTime) {
                goToMainMenu()
            }
        }
    }

    private fun starActivity() {
        myTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                if (CheckInternet.isConnected(this@SplashActivity)) {
                    if (finishGetAdsInfo && finishGetBanner) {
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


    fun goToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
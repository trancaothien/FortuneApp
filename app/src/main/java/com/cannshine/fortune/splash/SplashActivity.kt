package com.cannshine.fortune.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.cannshine.fortune.AppApplication
import com.cannshine.fortune.model.AdsInfo
import com.cannshine.fortune.utils.CheckInternet
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.R
import com.cannshine.fortune.base.BaseActivity
import com.cannshine.fortune.mainmenu.MainMenuActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
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
        splashViewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        //chuyển qua MainMenuActivity
        starActivity()
        if (CheckInternet.isConnected(this)) {
            //lấy thông tin gieo quẻ nhanh
            splashViewModel.getBanner(success = {
                val arrayBannerAds = it.optJSONArray("payload")
                for (i: Int in 0..arrayBannerAds.length()) {
                    var bannerObject: JSONObject? = arrayBannerAds.optJSONObject(i)
                    if (bannerObject != null) {
                        val link = bannerObject.optString("link").trim()
                        val photoLink = bannerObject.optString("photo_link").trim()
                        Log.d("linkPhoto", "onCreate: " + link)
                        val idAds = bannerObject.optString("id").trim()
                        Global.LINK = link
                        Global.PHOTO_LINK = photoLink
                        Global.ID_ADS = idAds
                    }
                }
                if (finishGetAdsInfo && finishSplashTime) {
                    goToMainMenu()
                } else {
                    finishGetBanner = true
                }
            }, fail = {
                if (finishGetAdsInfo == true && finishSplashTime == true) {
                    goToMainMenu()
                } else {
                    finishGetBanner = true
                }
            })

            splashViewModel.getAdsInfotwo(success = {
                var arrayAds: ArrayList<AdsInfo>? = ArrayList()
                val jsonArray = it.optJSONArray("payload")
                for (i in 0..jsonArray.length()) {
                    var adsInfo: JSONObject? = jsonArray.optJSONObject(i)
                    if (adsInfo != null) {
                        if (adsInfo != null) {
                            val id = adsInfo.optInt("id")
                            val code = adsInfo.optString("code")
                            val ads_info = adsInfo.optString("ads_info").trim()
                            val ads = AdsInfo(id, code, ads_info)
                            arrayAds = ArrayList()
                            arrayAds.add(ads)
                        }
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
                if (finishSplashTime == true && finishGetBanner == true) {
                    goToMainMenu()
                } else {
                    finishGetAdsInfo = true
                }
            }, fail = {
                if (finishSplashTime == true && finishGetBanner == true) {
                    goToMainMenu();
                } else {
                    finishGetAdsInfo = true;
                }
            })
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


    fun goToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
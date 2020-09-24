package com.cannshine.fortune.model

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class AdsManager {
    fun createAd(context: Context?, myInterstitialId: String?) {
        ad = InterstitialAd(context)
        ad!!.adUnitId = myInterstitialId
        ad!!.loadAd(AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build())
    }

    companion object {
        private var singleton: AdsManager? = null
        var ad: InterstitialAd? = null
        val instance: AdsManager?
            get() {
                if (singleton == null) {
                    singleton = AdsManager()
                }
                return singleton
            }
    }
}
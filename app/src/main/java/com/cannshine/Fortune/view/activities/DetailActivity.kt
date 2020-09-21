package com.cannshine.Fortune.view.activities

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import com.cannshine.Fortune.db.Database
import com.cannshine.Fortune.view.customView.InteractiveScrollView.OnBottomReachedListener
import com.cannshine.Fortune.R
import com.cannshine.Fortune.VolleyRequest.ApplicationController
import com.cannshine.Fortune.model.AdsManager
import com.cannshine.Fortune.model.Hexegram
import com.cannshine.Fortune.utils.CheckInternet
import com.cannshine.Fortune.utils.Global
import com.cannshine.Fortune.utils.Utils
import com.cannshine.Fortune.view.customView.InteractiveScrollView
import com.google.android.gms.ads.*
import com.startapp.android.publish.adsCommon.StartAppAd
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DetailActivity : AppCompatActivity() {
    var svContent: InteractiveScrollView? = null
    var imgBack: ImageView? = null
    var btnShare: ImageView? = null
    var btnClose: ImageView? = null
    var bgBanner: ImageView? = null
    var imgBanner: NetworkImageView? = null
    var imageLoader: ImageLoader? = null
    var txvTitle: TextView? = null
    var txvContent: TextView? = null
    var txvTieuDe: TextView? = null
    var hao1: ImageView? = null
    var hao2: ImageView? = null
    var hao3: ImageView? = null
    var hao4: ImageView? = null
    var hao5: ImageView? = null
    var hao6: ImageView? = null
    var constraintLayout: ConstraintLayout? = null
    var data = Database(this)
    var hexegram = Hexegram()
    private val startAppAd = StartAppAd(this)
    var adsManager = AdsManager.instance
    var ad = AdsManager.ad
    var infoAds: List<String>? = null
    var broadcastReceiver: BroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val admobInfo = Utils.getAdsInfo(this, Global.KEY_ADMOB)
        val appId = admobInfo[Global.ADMOB_APP_ID]
        val interstitialId = admobInfo[Global.ADMOB_INTERSTITIAL_ID]
        if (CheckInternet.isConnected(this)) {
            // quảng cáo banner của admob
            if (appId != null) {
                adsAdmobBanner(appId, admobInfo[Global.ADMOB_BANNER_ID])
            }
        }
        txvContent = findViewById<View>(R.id.txv_giaixam) as TextView
        imgBack = findViewById<View>(R.id.btn_back) as ImageView
        txvTitle = findViewById<View>(R.id.txv_title_dl) as TextView
        txvTieuDe = findViewById<View>(R.id.txv_tieude) as TextView
        btnShare = findViewById<View>(R.id.btn_share_dl) as ImageView
        hao1 = findViewById<View>(R.id.img_h1) as ImageView
        hao2 = findViewById<View>(R.id.img_h2) as ImageView
        hao3 = findViewById<View>(R.id.img_h3) as ImageView
        hao4 = findViewById<View>(R.id.img_h4) as ImageView
        hao5 = findViewById<View>(R.id.img_h5) as ImageView
        hao6 = findViewById<View>(R.id.img_h6) as ImageView
        imgBanner = findViewById<View>(R.id.img_ads) as NetworkImageView
        btnClose = findViewById(R.id.btn_close)
        svContent = findViewById(R.id.sv_content)
        constraintLayout = findViewById<View>(R.id.constrainLayout) as ConstraintLayout
        bgBanner = findViewById(R.id.bg_banner)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showAlertMessage(intent.getStringExtra("body"), intent.getStringExtra("title"))
            }
        }

        // set default cho ads va btn close
        bgBanner?.setVisibility(View.GONE)
        imgBanner!!.visibility = View.GONE
        btnClose?.setVisibility(View.GONE)

        //Lấy thông tin của quảng cáo
        banner

        // hiển thị quảng cáo full màn hình
        showAdsFullScreen()

        // cuộn suống cuối
        scrollToBottom()

        // trở về màn hình chính
        clickBtnBack()

        // click btn share
        setBtnShare()
        val fontTitle = Typeface.createFromAsset(this.assets, "UTM Azuki.ttf")
        txvTitle!!.setTypeface(fontTitle)
        val intent = intent
        val idHexegram = intent.getStringExtra("key_1")
        val h1 = intent.getStringExtra("hao_1")
        val h2 = intent.getStringExtra("hao_2")
        val h3 = intent.getStringExtra("hao_3")
        val h4 = intent.getStringExtra("hao_4")
        val h5 = intent.getStringExtra("hao_5")
        val h6 = intent.getStringExtra("hao_6")

        //setLine
        setLineHexegram(h1, h2, h3, h4, h5, h6)
        hexegram = data.getValues(idHexegram)
        val title = hexegram.h_name
        txvTitle!!.textSize = 15f
        txvTitle!!.text = title
        var tieuDe = ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<html><head>"
                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"  />"
                + "<head><body>")
        tieuDe += hexegram.h_mean + "<body><html>"
        txvTieuDe!!.text = Html.fromHtml(tieuDe)
        var customHtml = ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<html><head>"
                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"  />"
                + "<head><body>")
        customHtml += hexegram.h_content +
                hexegram.h_wao1 + hexegram.h_wao2 +
                hexegram.h_wao3 + hexegram.h_wao4 +
                hexegram.h_wao5 + hexegram.h_wao6 +
                "<body><html>"
        txvContent!!.text = Html.fromHtml(customHtml)

        //Load quảng cáo mỗi khi tắt
        if (ad != null) {
            if (CheckInternet.isConnected(this@DetailActivity)) {
                ad!!.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        if (CheckInternet.isConnected(this@DetailActivity)) {
                            ad!!.loadAd(AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build())
                        }
                    }
                }
            }
        }
    }

    private fun clickBtnBack() {
        imgBack!!.setOnClickListener {
            val intent = Intent(this@DetailActivity, MainMenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Lấy quảng cáo
    private val banner: Unit
        private get() {
            val stringRequest: StringRequest = object : StringRequest(Method.GET, Global.URL_DETAIL_GET_ADS_BANNER, Response.Listener { response ->
                Log.d("reponseGetBanner", "onResponse: $response")
                try {
                    infoAds = readBanner(response)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> Log.d("reponseGetBannerError", "onResponse: $error") }) {
                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    val bodys = "&action=ads&type=1&size=400x400"
                    return bodys.toByteArray(charset("utf-8"))
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

    // sự kiện khi click vào adsbanner
    private fun clickAds(idAds: String) {
        val stringRequest: StringRequest = object : StringRequest(Method.POST, Global.URL_DETAIL_CLICK_ADS,
                Response.Listener { response -> Log.d("responseClickAds", "onResponse: $response") },
                Response.ErrorListener { error -> Log.d("responseClickAdsError", "onResponse: $error") }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["apikey"] = Global.APIKEY
                return headers
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val bodys = "&act=clickads&bannerid=$idAds&appid=1&os=android"
                Log.d("bodys", "getBody: $bodys")
                return bodys.toByteArray(charset("utf-8"))
            }
        }
        ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
    }

    //button share
    fun setBtnShare() {
        btnShare!!.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "https://sites.google.com/view/boidich-policy/loi-tua"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Quan Thánh Linh Xăm")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }

    // setImageLine on Top
    fun setLineHexegram(h1: String, h2: String, h3: String, h4: String, h5: String, h6: String) {
        if (h1 == "dynamic_1") {
            hao1!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h1 == "normal_1") {
            hao1!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h1 == "dynamic_0") {
            hao1!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao1!!.setImageResource(R.mipmap.line_normal_0)
        }
        if (h2 == "dynamic_1") {
            hao2!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h2 == "normal_1") {
            hao2!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h2 == "dynamic_0") {
            hao2!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao2!!.setImageResource(R.mipmap.line_normal_0)
        }
        if (h3 == "dynamic_1") {
            hao3!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h3 == "normal_1") {
            hao3!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h3 == "dynamic_0") {
            hao3!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao3!!.setImageResource(R.mipmap.line_normal_0)
        }
        if (h4 == "dynamic_1") {
            hao4!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h4 == "normal_1") {
            hao4!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h4 == "dynamic_0") {
            hao4!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao4!!.setImageResource(R.mipmap.line_normal_0)
        }
        if (h5 == "dynamic_1") {
            hao5!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h5 == "normal_1") {
            hao5!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h5 == "dynamic_0") {
            hao5!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao5!!.setImageResource(R.mipmap.line_normal_0)
        }
        if (h6 == "dynamic_1") {
            hao6!!.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h6 == "normal_1") {
            hao6!!.setImageResource(R.mipmap.line_normal_1)
        } else if (h6 == "dynamic_0") {
            hao6!!.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            hao6!!.setImageResource(R.mipmap.line_normal_0)
        }
    }

    public override fun onResume() {
        super.onResume()
        //        startAppAd.onResume();
        val filter = IntentFilter("sendMessageBroadcast")
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, filter)
    }

    public override fun onPause() {
        super.onPause()
        //        startAppAd.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver!!)
    }

    //hienthiQuangCao
    fun adsAdmobBanner(appId: String?, bannerId: String?) {
        val relativeLayout = findViewById<RelativeLayout>(R.id.admobBanner2)
        val mAdView = AdView(this)
        MobileAds.initialize(this, appId)
        mAdView.adSize = AdSize.BANNER
        mAdView.adUnitId = bannerId
        relativeLayout.addView(mAdView)
        val adRequest = AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build()
        mAdView.loadAd(adRequest)
    }

    //random hiển thị quảng cáo
    fun ramdomAds() {
        val rd = Random()
        val x: Int
        x = rd.nextInt(10)
        if (x % 2 == 0) {
            if (ad!!.isLoaded) {
                ad!!.show()
                checkAds = 1
            } else {
                ad!!.loadAd(AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build())
                StartAppAd.disableAutoInterstitial()
                startAppAd.showAd() // show the ad
                startAppAd.loadAd() // load the next ad
                checkAds = 0
            }
        } else {
            //Quảng cáo StartApp
            StartAppAd.disableAutoInterstitial()
            startAppAd.showAd() // show the ad
            startAppAd.loadAd() // load the next ad
            checkAds = 0
        }
    }

    @Throws(JSONException::class)
    private fun readBanner(response: String): List<String>? {
        var array: MutableList<String>? = null
        val jsonObject = JSONObject(response)
        val payload = jsonObject.optJSONArray("payload")
        for (i in 0 until payload.length()) {
            val info = payload.opt(i) as JSONObject
            if (info != null) {
                val link = info.optString("link")
                val phto_link = info.optString("photo_link")
                val idAds = info.optString("id")
                array = ArrayList()
                array.add(link)
                array.add(phto_link)
                array.add(idAds)
            }
        }
        return array
    }

    private fun clickCloseAds() {
        btnClose!!.setOnClickListener {
            bgBanner!!.visibility = View.GONE
            imgBanner!!.visibility = View.GONE
            btnClose!!.visibility = View.GONE
            imgBack!!.isEnabled = true
            btnShare!!.isEnabled = true
        }
    }

    private fun clickAdsBanner(link: String, idAds: String) {
        imgBanner!!.setOnClickListener {
            bgBanner!!.visibility = View.GONE
            imgBanner!!.visibility = View.GONE
            btnClose!!.visibility = View.GONE
            imgBack!!.isEnabled = true
            btnShare!!.isEnabled = true
            //request api
            clickAds(idAds)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(link)
            startActivity(intent)
        }
    }

    private fun showAlertMessage(body: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage(body)
                .setTitle(title)
                .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun onBackPressed() {
        val intent = Intent(this@DetailActivity, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun scrollToBottom() {
        if (CheckInternet.isConnected(this)) {
            svContent!!.onBottomReachedListener = object : OnBottomReachedListener {
                override fun onBottomReached() {
                    val link = infoAds!![0]
                    val photoLink = infoAds!![1]
                    val idAds = infoAds!![2]
                    if (link != null && photoLink != null) {
                        bgBanner!!.visibility = View.VISIBLE
                        imgBanner!!.visibility = View.VISIBLE
                        btnClose!!.visibility = View.VISIBLE
                        imgBack!!.isEnabled = false
                        btnShare!!.isEnabled = false
                        imageLoader = ApplicationController.getInstance(this@DetailActivity)?.imageLoader
                        imageLoader?.get(photoLink, ImageLoader.getImageListener(imgBanner, 0, 0))
                        imgBanner!!.setImageUrl("url", imageLoader)
                        // click vào quảng cáo
                        clickAdsBanner(link, idAds)
                        // click vào btnclose
                        clickCloseAds()
                    }
                }
            }
        }
    }

    private fun showAdsFullScreen() {
        if (checkAds == -1) {
            // ramdom quảng cáo admob vs startapp
            ramdomAds()
        } else {
            if (checkAds == 0) {
                if (ad != null) {
                    if (ad!!.isLoaded) {
                        ad!!.show()
                    } else {
                        ad!!.loadAd(AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build())
                        StartAppAd.disableAutoInterstitial()
                        startAppAd.showAd() // show the ad
                        startAppAd.loadAd() // load the next ad
                    }
                }
            }
            if (checkAds == 1) {
                //Quảng cáo StartApp
                StartAppAd.disableAutoInterstitial()
                startAppAd.showAd() // show the ad
                startAppAd.loadAd() // load the next ad
            }
        }
    }

    companion object {
        var checkAds = -1
    }
}
@file:Suppress("DEPRECATION")

package com.cannshine.fortune.detail

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.cannshine.fortune.db.Database
import com.cannshine.fortune.customView.InteractiveScrollView.OnBottomReachedListener
import com.cannshine.fortune.R
import com.cannshine.fortune.base.BaseActivity
import com.cannshine.fortune.mainmenu.MainMenuActivity
import com.cannshine.fortune.model.AdsManager
import com.cannshine.fortune.model.Hexegram
import com.cannshine.fortune.utils.CheckInternet
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.databinding.ActivityDetailBinding
import com.cannshine.fortune.model.BannerAds
import com.google.android.gms.ads.*
import com.startapp.android.publish.adsCommon.StartAppAd
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailActivity : BaseActivity() {
//    var imageLoader: ImageLoader? = null
    var data = Database(this)
    var hexegram = Hexegram()
    private val startAppAd = StartAppAd(this)
    var ad = AdsManager.ad
    var infoAds: List<BannerAds>? = ArrayList()
    var broadcastReceiver: BroadcastReceiver? = null
    lateinit var detailViewModel: DetailViewModel
    lateinit var binding: ActivityDetailBinding
    override fun getContentView(): Int {
        return R.layout.activity_detail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailViewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        val admobInfo = Utils.getAdsInfo(this, Global.KEY_ADMOB)
        val appId = admobInfo[Global.ADMOB_APP_ID]
        if (CheckInternet.isConnected(this)) {
            // quảng cáo banner của admob
            if (appId != null) {
                adsAdmobBanner(appId, admobInfo[Global.ADMOB_BANNER_ID])
            }
        }
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showAlertMessage(intent.getStringExtra("body"), intent.getStringExtra("title"))
            }
        }

        // set default cho ads va btn close
        binding.bgBanner.setVisibility(View.GONE)
        binding.imgAds.visibility = View.GONE
        binding.btnClose.setVisibility(View.GONE)

        //Lấy thông tin của quảng cáo
        detailViewModel.getBanner(
                success =
                { infoAds = listOf(it[0]) }, "requestBanner", 1, 2, "android"
        )

        // hiển thị quảng cáo full màn hình
        showAdsFullScreen()

        // cuộn suống cuối
        scrollToBottom()

        // trở về màn hình chính
        clickBtnBack()

        // click btn share
        setBtnShare()
        val fontTitle = Utils.getFontType(this)
        binding.txvTitleDl.typeface = fontTitle
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
        hexegram = data.getValues(idHexegram)!!
        val title = hexegram.h_name
        binding.txvTitleDl.textSize = 15f
        binding.txvTitleDl.text = title
        var tieuDe = ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<html><head>"
                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"  />"
                + "<head><body>")
        tieuDe += hexegram.h_mean + "<body><html>"
        binding.txvTieuDe.text = Html.fromHtml(tieuDe)
        var customHtml = ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<html><head>"
                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"  />"
                + "<head><body>")
        customHtml += hexegram.h_content +
                hexegram.h_wao1 + hexegram.h_wao2 +
                hexegram.h_wao3 + hexegram.h_wao4 +
                hexegram.h_wao5 + hexegram.h_wao6 +
                "<body><html>"
        binding.txvGiaiXam.text = Html.fromHtml(customHtml)

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
        binding.btnBack.setOnClickListener {
            val intent = Intent(this@DetailActivity, MainMenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    //button share
    fun setBtnShare() {
        binding.btnShareDL.setOnClickListener {
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
            binding.imgH1.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h1 == "normal_1") {
            binding.imgH1.setImageResource(R.mipmap.line_normal_1)
        } else if (h1 == "dynamic_0") {
            binding.imgH1.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH1.setImageResource(R.mipmap.line_normal_0)
        }
        if (h2 == "dynamic_1") {
            binding.imgH2.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h2 == "normal_1") {
            binding.imgH2.setImageResource(R.mipmap.line_normal_1)
        } else if (h2 == "dynamic_0") {
            binding.imgH2.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH2.setImageResource(R.mipmap.line_normal_0)
        }
        if (h3 == "dynamic_1") {
            binding.imgH3.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h3 == "normal_1") {
            binding.imgH3.setImageResource(R.mipmap.line_normal_1)
        } else if (h3 == "dynamic_0") {
            binding.imgH3.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH3.setImageResource(R.mipmap.line_normal_0)
        }
        if (h4 == "dynamic_1") {
            binding.imgH4.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h4 == "normal_1") {
            binding.imgH4.setImageResource(R.mipmap.line_normal_1)
        } else if (h4 == "dynamic_0") {
            binding.imgH4.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH4.setImageResource(R.mipmap.line_normal_0)
        }
        if (h5 == "dynamic_1") {
            binding.imgH5.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h5 == "normal_1") {
            binding.imgH5.setImageResource(R.mipmap.line_normal_1)
        } else if (h5 == "dynamic_0") {
            binding.imgH5.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH5.setImageResource(R.mipmap.line_normal_0)
        }
        if (h6 == "dynamic_1") {
            binding.imgH6.setImageResource(R.mipmap.line_dynamic_1)
        } else if (h6 == "normal_1") {
            binding.imgH6.setImageResource(R.mipmap.line_normal_1)
        } else if (h6 == "dynamic_0") {
            binding.imgH6.setImageResource(R.mipmap.line_dynamic_0)
        } else {
            binding.imgH6.setImageResource(R.mipmap.line_normal_0)
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

    private fun clickCloseAds() {
        binding.btnClose.setOnClickListener {
            binding.bgBanner.visibility = View.GONE
            binding.imgAds.visibility = View.GONE
            binding.btnClose.visibility = View.GONE
            binding.btnBack.isEnabled = true
            binding.btnShareDL.isEnabled = true
        }
    }

    private fun clickAdsBanner(link: String, idAds: String) {
        binding.imgAds.setOnClickListener {
            binding.bgBanner.visibility = View.GONE
            binding.imgAds.visibility = View.GONE
            binding.btnClose.visibility = View.GONE
            binding.btnBack.isEnabled = true
            binding.btnShareDL.isEnabled = true
            //request api
            detailViewModel.clickAds(idAds)
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

    private fun scrollToBottom() {
        if (CheckInternet.isConnected(this)) {
            binding.svContent.onBottomReachedListener = object : OnBottomReachedListener {
                override fun onBottomReached() {
                    val link = infoAds!![0].link
                    val photoLink = infoAds!![0].photo_link
                    val idAds = infoAds!![0].id
                    binding.bgBanner.visibility = View.VISIBLE
                    binding.imgAds.visibility = View.VISIBLE
                    binding.btnClose.visibility = View.VISIBLE
                    binding.btnBack.isEnabled = false
                    binding.btnShareDL.isEnabled = false

                    Glide.with(applicationContext).load(photoLink).into(binding.imgAds)

                    // click vào quảng cáo
                    clickAdsBanner(link, idAds.toString())
                    // click vào btnclose
                    clickCloseAds()
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
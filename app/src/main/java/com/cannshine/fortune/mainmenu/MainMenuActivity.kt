package com.cannshine.fortune.mainmenu

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import com.cannshine.fortune.db.Database
import com.cannshine.fortune.BuildConfig
import com.cannshine.fortune.R
import com.cannshine.fortune.VolleyRequest.ApplicationController
import com.cannshine.fortune.base.BaseActivity
import com.cannshine.fortune.model.AdsManager
import com.cannshine.fortune.model.Hexegram
import com.cannshine.fortune.utils.CheckInternet
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.detail.DetailActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.startapp.android.publish.adsCommon.StartAppAd
import com.startapp.android.publish.adsCommon.StartAppSDK
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MainMenuActivity : BaseActivity() {
    var imgTortoise: ImageView? = null
    var btnStart: ImageView? = null
    var imgCoin1: ImageView? = null
    var imgCoin2: ImageView? = null
    var imgCoin3: ImageView? = null
    var imgLine1: ImageView? = null
    var imgLine2: ImageView? = null
    var imgLine3: ImageView? = null
    var imgLine4: ImageView? = null
    var imgLine5: ImageView? = null
    var imgLine6: ImageView? = null
    var btnShare: ImageView? = null
    var imgDisk: ImageView? = null
    var btnSound: ImageView? = null
    var txvCount: TextView? = null
    var txvTitle: TextView? = null
    var btnGieoQueNhanh: NetworkImageView? = null
    var imageLoader: ImageLoader? = null
    var mediaShakeTortoise: MediaPlayer? = null
    var mediaUpCoin: MediaPlayer? = null
    var shakeT: AnimationDrawable? = null
    var downTortoise: Animation? = null
    var upCoin1: Animation? = null
    var upCoin2: Animation? = null
    var upCoin3: Animation? = null
    var upTortoise: Animation? = null
    private val SOUND_INFO = "info"
    private val KEY_SOUND = "keySound"
    var myCountDownTimer: CountDownTimer? = null
    var myCountDownTimer2: CountDownTimer? = null
    var count = 1
    var temp: String? = null
    var h1: String? = null
    var h2: String? = null
    var h3: String? = null
    var h4: String? = null
    var h5: String? = null
    var h6: String? = null
    var iDHexegram = ""
    var flag = ""
    var idHexe = ""
    var arrayLimitCoin = IntArray(6)
    var sound = 1
    var data = Hexegram()
    var dataHexegram = Database(this)
    var arrayList = ArrayList<Int>()
    var broadcastReceiver: BroadcastReceiver? = null
    override fun getContentView(): Int {
        return R.layout.activity_main_menu
    }
    lateinit var mainMenuViewModel: MainMenuViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainMenuViewModel = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)

        // Admob Banner
        if (CheckInternet.isConnected(this)) {
            val admobInfo = Utils.getAdsInfo(this, Global.KEY_ADMOB)
            val appId = admobInfo[Global.ADMOB_APP_ID]!!.trim { it <= ' ' }
            Log.d("admob", "onCreate: $appId")
            if (appId != null) {
                val relativeLayout = findViewById<RelativeLayout>(R.id.admobBanner)
                val mAdView = AdView(this)
                MobileAds.initialize(this, appId)
                mAdView.adSize = AdSize.BANNER
                mAdView.adUnitId = admobInfo[Global.ADMOB_BANNER_ID]
                relativeLayout.addView(mAdView)
                val adRequest = AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build()
                mAdView.loadAd(adRequest)
            }

            //Quảng cáo StartApp
            val startappInfo = Utils.getAdsInfo(this, Global.KEY_STARTAPP)
            val startappId = startappInfo[Global.STARTAPP_APP_ID]
            Log.d("startapp", "onCreate: $startappId")
            if (startappId != null) {
                StartAppSDK.init(this, startappId, true)
                StartAppAd.disableSplash()
            }

            //Load quảng cáo show full ở DetailActivity
            val admobIntertitialId = Utils.getAdsInfo(this, Global.KEY_ADMOB)
            val interstitialId = admobIntertitialId[Global.ADMOB_INTERSTITIAL_ID]
            if (interstitialId != null) {
                val adsManager = AdsManager.instance
                adsManager?.createAd(this, interstitialId)
            }
        } else {
            val relativeLayout = findViewById<RelativeLayout>(R.id.admobBanner)
            relativeLayout.visibility = View.GONE
        }

        // Hỏi cấp quyền Write
        askPermissionAndWrite()
        val checkUser = Utils.getUserInfo(this, Global.KEY_USER)
        if (checkUser == false) {
            val isneedUpdate = Utils.getFlagToken(this)
            val needUpdate = isneedUpdate[Global.K_TOKEN]
            if (needUpdate == null) {
                mainMenuViewModel.createUser(success = {
                    readRequestCreateUser(it, "1")
                }, fail = {
                    Utils.setFlagToken(this@MainMenuActivity, "1")
                })
            } else {
                mainMenuViewModel.createUser(success = {
                    readRequestCreateUser(it, needUpdate)
                }, fail = {
                    Utils.setFlagToken(this@MainMenuActivity, "1")
                })
            }
        } else {
            val token = Utils.getFlagToken(this)
            val updateFlag = token[Global.K_TOKEN]
            val deviceId = Utils.getDeviceId(this)
            //            if(updateFlag == null){
//                Utils.setFlagToken(this, "1");
//                SharedPreferences s = this.getSharedPreferences(Global.KEY_USER, MODE_PRIVATE);
//                String userKey = s.getString(Global.K_USERKEY, "");
//                updateFCM(deviceId, userKey, "");
//            }
//            else
            if (updateFlag != null) {
                if (updateFlag == "1") {
                    val s = getSharedPreferences(Global.KEY_USER, MODE_PRIVATE)
                    val userKey = s.getString(Global.K_USERKEY, "")
                    val newToken = Utils.getNewToken(this)
                    updateFCM(deviceId!!, userKey, newToken!!)
                }
            }
        }
        appVersion
        mediaShakeTortoise = MediaPlayer.create(this@MainMenuActivity, R.raw.sowhexagram)
        mediaUpCoin = MediaPlayer.create(this@MainMenuActivity, R.raw.coin)
        btnStart = findViewById<View>(R.id.btn_done) as ImageView
        btnSound = findViewById<View>(R.id.btn_sound) as ImageView
        imgDisk = findViewById<View>(R.id.img_disk) as ImageView
        imgTortoise = findViewById<View>(R.id.img_tortoise) as ImageView
        imgCoin1 = findViewById<View>(R.id.img_coin1) as ImageView
        imgCoin2 = findViewById<View>(R.id.img_coin2) as ImageView
        imgCoin3 = findViewById<View>(R.id.img_coin3) as ImageView
        imgLine1 = findViewById<View>(R.id.img_line1) as ImageView
        imgLine2 = findViewById<View>(R.id.img_line2) as ImageView
        imgLine3 = findViewById<View>(R.id.img_line3) as ImageView
        imgLine4 = findViewById<View>(R.id.img_line4) as ImageView
        imgLine5 = findViewById<View>(R.id.img_line5) as ImageView
        imgLine6 = findViewById<View>(R.id.img_line6) as ImageView
        btnShare = findViewById<View>(R.id.btn_share) as ImageView
        txvCount = findViewById<View>(R.id.txv_count) as TextView
        txvTitle = findViewById<View>(R.id.txv_title) as TextView
        btnGieoQueNhanh = findViewById(R.id.btn_gieo_que_nhanh)
        val fontTxv = Typeface.createFromAsset(this.assets, "UTM Azuki.ttf")
        txvTitle!!.setTypeface(fontTxv)
        txvCount!!.setTypeface(fontTxv)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showAlertMessage(intent.getStringExtra("body"), intent.getStringExtra("title"))
            }
        }
        setInvisibleCoin()
        if (Global.LINK != null && Global.PHOTO_LINK != null) {
            setBtnGieoQueNhanh(Global.PHOTO_LINK)
            clickBtnGieoQueNhanh(Global.LINK!!, Global.ID_ADS!!)
        } else {
            btnGieoQueNhanh?.setVisibility(View.GONE)
        }

        // Kiểm tra xem đã có setting trước khi đó chưa
        val shared = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
        val string_temp = shared.getString(KEY_SOUND, "")!!.trim { it <= ' ' }
        if (string_temp == "1" == false && string_temp == "0" == false) {
            setImageSound()
        } else if (string_temp == "1") {
            btnSound!!.setImageResource(R.mipmap.btn_unmute)
            mediaUpCoin?.setVolume(1f, 1f)
            mediaShakeTortoise?.setVolume(1f, 1f)
        } else {
            btnSound!!.setImageResource(R.mipmap.btn_mute)
            mediaUpCoin?.setVolume(0f, 0f)
            mediaShakeTortoise?.setVolume(0f, 0f)
        }

        // sự kiện nhấn nút mute
        clickBtnSound()
        btnStart!!.setOnClickListener {
            val check = askPermission(REQUEST_ID_WRITE_PERMISSION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            initAnimation()
            if (check == true) {
                arrayList = arrayNumber()
                count += 1
                if (count <= 6) {
                    imgTortoise!!.startAnimation(downTortoise)
                } else if (count == 7) {
                    imgTortoise!!.startAnimation(downTortoise)
                } else if (count == 8) {
                    val intent = Intent(this@MainMenuActivity, DetailActivity::class.java)
                    intent.putExtra("key_1", idHexe)
                    intent.putExtra("hao_1", h1)
                    intent.putExtra("hao_2", h2)
                    intent.putExtra("hao_3", h3)
                    intent.putExtra("hao_4", h4)
                    intent.putExtra("hao_5", h5)
                    intent.putExtra("hao_6", h6)
                    startActivity(intent)
                }
            } else {
                askPermissionAndWrite()
            }
        }
        buttomShare()
    }

    fun setInvisibleCoin() {
        imgCoin1!!.visibility = View.INVISIBLE
        imgCoin2!!.visibility = View.INVISIBLE
        imgCoin3!!.visibility = View.INVISIBLE
    }

    fun setInvisibleCoin_UpCoin() {
        imgCoin1!!.clearAnimation()
        imgCoin2!!.clearAnimation()
        imgCoin3!!.clearAnimation()
        imgCoin1!!.visibility = View.INVISIBLE
        imgCoin2!!.visibility = View.INVISIBLE
        imgCoin3!!.visibility = View.INVISIBLE
    }

    fun setVisibleCoin() {
        imgCoin1!!.visibility = View.VISIBLE
        imgCoin2!!.visibility = View.VISIBLE
        imgCoin3!!.visibility = View.VISIBLE
    }

    fun setImageCoin() {
        if (arrayList[0] == 0) {
            imgCoin1!!.setImageResource(R.mipmap.icon_coin_0)
            if (arrayList[1] == 0) {
                imgCoin2!!.setImageResource(R.mipmap.icon_coin_0)
                if (arrayList[2] == 0) {
                    imgCoin3!!.setImageResource(R.mipmap.icon_coin_0)
                } else imgCoin3!!.setImageResource(R.mipmap.icon_coin_1)
            } else {
                imgCoin2!!.setImageResource(R.mipmap.icon_coin_1)
                if (arrayList[2] == 0) {
                    imgCoin3!!.setImageResource(R.mipmap.icon_coin_0)
                } else imgCoin3!!.setImageResource(R.mipmap.icon_coin_1)
            }
        } else {
            imgCoin1!!.setImageResource(R.mipmap.icon_coin_1)
            if (arrayList[1] == 0) {
                imgCoin2!!.setImageResource(R.mipmap.icon_coin_0)
                if (arrayList[2] == 0) {
                    imgCoin3!!.setImageResource(R.mipmap.icon_coin_0)
                } else imgCoin3!!.setImageResource(R.mipmap.icon_coin_1)
            } else {
                imgCoin2!!.setImageResource(R.mipmap.icon_coin_1)
                if (arrayList[2] == 0) {
                    imgCoin3!!.setImageResource(R.mipmap.icon_coin_0)
                } else imgCoin3!!.setImageResource(R.mipmap.icon_coin_1)
            }
        }
    }

    fun setLine(line: Int) {
        val c1 = arrayList[0]
        val c2 = arrayList[1]
        val c3 = arrayList[2]
        val yin = 0
        val yang = 1
        val type: Int
        val isDynamic: Boolean
        if (c1 == c2 && c2 == c3) {
            isDynamic = true
            type = if (c1 == yang) {
                yang
            } else {
                yin
            }
        } else {
            isDynamic = false
            type = if (c1 == yang && c2 == yang || c1 == yang && c3 == yang || c2 == yang && c3 == yang) {
                yang
            } else yin
        }
        //idhexegram
        if (isDynamic == true) {
            flag = flag + "1"
            iDHexegram = if (type == 1) {
                iDHexegram + "1"
            } else {
                iDHexegram + "0"
            }
        } else {
            flag = flag + "0"
            iDHexegram = if (type == 1) {
                iDHexegram + "1"
            } else {
                iDHexegram + "0"
            }
        }

        //setImageLine
        if (line == 1) {
            h1 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine1!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine1!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine1!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine1!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 2) {
            h2 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine2!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine2!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine2!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine2!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 3) {
            h3 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine3!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine3!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine3!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine3!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 4) {
            h4 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine4!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine4!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine4!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine4!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 5) {
            h5 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine5!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine5!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine5!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine5!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else {
            h6 = if (isDynamic == true) {
                if (type == yang) {
                    imgLine6!!.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    imgLine6!!.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    imgLine6!!.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    imgLine6!!.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        }

        //return IDHexegram
    }

    //todo: setTitle in mvvm(viewModel)
    fun setTitle() {
        val idDaoNguoc = reverseID(iDHexegram)
        val flagHD = reverseID(flag)
        var kyTu: String
        var flag: String
        for (i in 0 until idDaoNguoc.length) {
            flag = flagHD[i].toString()
            kyTu = idDaoNguoc[i].toString()
            if (flag == "1") {
                kyTu = if (kyTu == "0") {
                    "1"
                } else {
                    "0"
                }
            }
            idHexe = idHexe + kyTu
        }
        data = dataHexegram.getValues(idHexe)!!
        val name = data.h_name
        txvTitle!!.textSize = 15f
        txvTitle!!.text = name
    }

    //dao nguoc chuoi
    fun reverseID(id: String?): String {
        return StringBuffer(id!!).reverse().toString()
    }

    fun buttomShare() {
        btnShare!!.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "https://sites.google.com/view/boidich-policy/loi-tua"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Quan Thánh Linh Xăm")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }

    private fun askPermissionAndWrite() {
        val canWrite = askPermission(REQUEST_ID_WRITE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //
        if (canWrite) {
            createDB()
        }
    }

    // Với Android Level >= 23 bạn phải hỏi người dùng cho phép các quyền với thiết bị
    private fun askPermission(requestId: Int, permissionName: String): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {

            // Kiểm tra quyền
            val permission = ActivityCompat.checkSelfPermission(this@MainMenuActivity, permissionName)
            if (permission != PackageManager.PERMISSION_GRANTED) {

                // Nếu không có quyền, cần nhắc người dùng cho phép.
                ActivityCompat.requestPermissions(this@MainMenuActivity, arrayOf(permissionName), requestId)
                return false
            }
        }
        return true
    }

    // Khi yêu cầu hỏi người dùng được trả về (Chấp nhận hoặc không chấp nhận).
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Chú ý: Nếu yêu cầu bị hủy, mảng kết quả trả về là rỗng.
        if (grantResults.size > 0) {
            when (requestCode) {
                REQUEST_ID_WRITE_PERMISSION -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        createDB()
                    }
                }
            }
        } else {
        }
    }

    fun createDB() {
        dataHexegram.createDB()
    }

    fun arrayNumber(): ArrayList<Int> {
        val rD = Random()
        val list = ArrayList<Int>()
        val listCoin = ArrayList<Int>()
        var even = 50
        var odd = 50
        for (i in 0..99) {
            var rand = random()
            if (even == 0) {
                rand = 0
                even -= 1
            } else if (odd == 0) {
                rand = 1
                odd -= 1
            } else {
                if (rand == 0) {
                    even -= 1
                } else {
                    odd -= 1
                }
            }
            list.add(rand)
        }
        val i1 = rD.nextInt(100)
        val i2 = rD.nextInt(100)
        val i3 = rD.nextInt(100)
        val c1 = list[i1]
        val c2 = list[i2]
        val c3 = list[i3]
        listCoin.add(c1)
        listCoin.add(c2)
        listCoin.add(c3)
        return listCoin
    }

    private fun random(): Int {
        val temp: Int
        val random = Random()
        temp = random.nextInt(10)
        return if (temp % 2 == 0) {
            0
        } else {
            1
        }
    }

    fun initAnimationDownTortoise(distance: Int) {
        downTortoise = TranslateAnimation(0F, 0F, 0F, distance.toFloat())
        downTortoise?.setFillAfter(true)
        downTortoise?.setDuration(1000)
        downTortoise?.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                btnStart!!.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animation) {
                imgTortoise!!.setImageResource(R.drawable.shaketortoise)
                shakeT = imgTortoise!!.drawable as AnimationDrawable
                shakeT!!.start()
                mediaShakeTortoise!!.start()
                //                mediaShakeTortoise.prepareAsync();
                myCountDownTimer = object : CountDownTimer(2000, 1000) {
                    override fun onTick(l: Long) {}
                    override fun onFinish() {
                        shakeT!!.stop()
                        mediaShakeTortoise!!.stop()
                        try {
                            mediaShakeTortoise!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        setImageCoin()
                        setVisibleCoin()
                        imgCoin1!!.startAnimation(upCoin1)
                        imgCoin2!!.startAnimation(upCoin2)
                        imgCoin3!!.startAnimation(upCoin3)
                        temp = Integer.toString(count)
                        if (count <= 6) {
                            txvCount!!.text = "Hào $temp"
                        }
                        if (count == 7) {
                            txvCount!!.text = ""
                            btnStart!!.setImageResource(R.mipmap.button_show)
                        }
                        myCountDownTimer2 = object : CountDownTimer(3000, 1000) {
                            override fun onTick(l: Long) {}
                            override fun onFinish() {
                                imgTortoise!!.startAnimation(upTortoise)
                            }
                        }
                        myCountDownTimer2?.start()
                    }
                }
                myCountDownTimer?.start()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun initAnimationUpTortoise(distance: Int) {
        upTortoise = TranslateAnimation(0F, 0F, distance.toFloat(), 0F)
        upTortoise?.setDuration(1000)
        upTortoise?.setFillAfter(true)
        upTortoise?.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                setInvisibleCoin_UpCoin()
                btnStart!!.isEnabled = true
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun initAnimationUpCoin(arrayLimitCoin: IntArray, widthDisk: Int, distanceCoin1_Disk: Int,
                            distanceCoin2_Disk: Int, distanceCoin3_Disk: Int) {
        upCoin1 = TranslateAnimation(0F, (-widthDisk / 6 + arrayLimitCoin[0]).toFloat(), 0F,
                (-distanceCoin2_Disk + arrayLimitCoin[1]).toFloat())
        upCoin2 = TranslateAnimation(0F, (0 + arrayLimitCoin[2]).toFloat(), 0F,
                (-distanceCoin1_Disk + arrayLimitCoin[3]).toFloat())
        upCoin3 = TranslateAnimation(0F, (widthDisk / 6 + arrayLimitCoin[4]).toFloat(), 0F,
                (-distanceCoin3_Disk + arrayLimitCoin[5]).toFloat())
        upCoin1?.setDuration(1000)
        upCoin2?.setDuration(1000)
        upCoin3?.setDuration(1000)
        upCoin1?.setFillAfter(true)
        upCoin2?.setFillAfter(true)
        upCoin3?.setFillAfter(true)
        upCoin1?.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mediaUpCoin!!.start()
                //                mediaUpCoin.prepareAsync();
            }

            override fun onAnimationEnd(animation: Animation) {
                mediaUpCoin!!.stop()
                try {
                    mediaUpCoin!!.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                setLine(count - 1)
                if (count == 7) {
//                    mainMenuViewModel.setTitle(flag, iDHexegram)
                    setTitle()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun randomLimitCoin(): IntArray {
        val arrayLimit = IntArray(6)
        val rd = Random()
        var i: Int
        i = 0
        while (i <= 5) {
            val temp = rd.nextInt(30)
            arrayLimit[i] = temp
            i++
        }
        i = 0
        while (i <= 5) {
            if (arrayLimit[i] % 2 == 0) {
                arrayLimit[i] = arrayLimit[i] * 1
            } else arrayLimit[i] = arrayLimit[i] * -1
            i++
        }
        return arrayLimit
    }

    fun initAnimation() {
        arrayLimitCoin = randomLimitCoin()

        // lấy tọa độ y của imgDisk
        val location = IntArray(2)
        imgDisk!!.getLocationOnScreen(location)
        val yImgDisk = location[1]

        // Lấy tọa độ y của BtnStart
        val location2 = IntArray(2)
        btnStart!!.getLocationOnScreen(location2)
        val yBtnStart = location2[1]
        val heightDisk = imgDisk!!.height
        val widthDisk = imgDisk!!.width
        val heightCoin = imgCoin1!!.height

        // Lấy tọa độ y của ImgCoin
        val yImgCoin = yBtnStart - heightCoin

        // Khoảng cách của các đồng xu đến ImgDisk( Điểm cố định)
        val distanceCoin1_Disk = yImgCoin - yImgDisk - heightDisk / 5
        val distanceCoin2_Disk = yImgCoin - yImgDisk - 3 * heightDisk / 6
        val distanceCoin3_Disk = yImgCoin - yImgDisk - 3 * heightDisk / 6

        // Khoảng cách khi di chuyển mai rùa
        val distance = yBtnStart - yImgDisk - heightDisk / 2
        initAnimationDownTortoise(distance)
        initAnimationUpTortoise(distance)
        initAnimationUpCoin(arrayLimitCoin, widthDisk, distanceCoin1_Disk, distanceCoin2_Disk, distanceCoin3_Disk)
    }

    // click Button Sound
    fun clickBtnSound() {
        btnSound!!.setOnClickListener {
            val share = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
            val string_temp = share.getString(KEY_SOUND, "")
            if (string_temp == "1") {
                sound = 0
                mediaShakeTortoise!!.setVolume(0f, 0f)
                mediaUpCoin!!.setVolume(0f, 0f)
                val pref: SharedPreferences
                pref = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString(KEY_SOUND, "" + sound)
                editor.commit()
                btnSound!!.setImageResource(R.mipmap.btn_mute)
            } else if (string_temp == "0") {
                sound = 1
                mediaShakeTortoise!!.setVolume(1f, 1f)
                mediaUpCoin!!.setVolume(1f, 1f)
                btnSound!!.setImageResource(R.mipmap.btn_unmute)
                val pref: SharedPreferences
                pref = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString(KEY_SOUND, "" + sound)
                editor.commit()
            }
        }
    }

    // set default image Btn Sound
    fun setImageSound() {
        val pref: SharedPreferences
        pref = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(KEY_SOUND, "" + sound)
        editor.commit()
        btnSound!!.setImageResource(R.mipmap.btn_unmute)
    }

    //todo: this
    fun createUser(needUpdateFCM: String) {
        val country = this.resources.configuration.locale.country
        val deviceId = Utils.getDeviceId(this)
        val stringRequest: StringRequest = object : StringRequest(Method.POST, Global.URL_MAIN_CREATE_USER, Response.Listener { response ->
            Log.d("voll", "onResponse: $response")
            try {
                readRequestCreateUser(response, needUpdateFCM)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { Utils.setFlagToken(this@MainMenuActivity, "1") }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["apikey"] = Global.APIKEY
                //                params.put("Content-Type", "application/json");
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val body = "&act=newuser&os=android&deviceid=$deviceId&location=$country&appid=1"
                return body.toByteArray(charset("utf-8"))
            }
        }
        ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
    }

    val appVersion: Unit
        get() {
            val stringRequest: StringRequest = object : StringRequest(Method.GET, Global.URL_MAIN_GET_VERSION, Response.Listener { response ->
                Log.d("responseGetAppVersion", "onResponse: $response")
                try {
                    readGetAppVersion(response)
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error -> Log.d("responseGetAppVersion", "onResponse: $error") }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["apikey"] = Global.APIKEY
                    headers["Content-Type"] = "application/x-www-form-urlencoded"
                    return headers
                }
            }
            ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
        }

    @Throws(JSONException::class)
    private fun readRequestCreateUser(response: String, needUpdateFCM: String) {
        val jsonObject = JSONObject(response)
        val info_user = jsonObject.optJSONObject("payload")
        val user_id = info_user.optString("user_id")
        val user_key = info_user.optString("user_key")
        if (user_id != null && user_key != null) {
            Utils.saveUserInfo(this, Global.KEY_USER, user_id, user_key)
            if (needUpdateFCM == "1") {
                val newToken = Utils.getNewToken(this)
                updateFCM(Utils.getDeviceId(this)!!, user_key, newToken!!)
            }
        }
    }

    @Throws(JSONException::class, PackageManager.NameNotFoundException::class)
    private fun readGetAppVersion(response: String) {
        val json = JSONObject(response)
        val info = json.optJSONObject("payload")
        val versionName = info.optString("version_name")
        val versionCode = info.optString("version_check")
        val linkUpdate = info.optString("store_url")
        val need_update = info.optInt("need_update")
        if (versionCode != null && linkUpdate != null) {
            if (versionCode != BuildConfig.VERSION_CODE.toString()) showAlertUpdate(versionName, linkUpdate, need_update)
        }
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun showAlertUpdate(versionName: String, link: String, update: Int) {
        val manager = this.packageManager
        val info = manager.getPackageInfo(
                this.packageName, 0)
        val version = info.versionName

//        if(!versionName.equals(version)){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("Ứng dụng đang có phiên bản $versionName. Vui lòng cập nhật!")
                .setTitle("Cập Nhật Ứng Dụng")
                .setPositiveButton("UPDATE") { dialog, id ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(link)
                    startActivity(i)
                }
        if (update == 0) {
            builder.setNegativeButton("CANCEL") { dialog, which -> dialog.dismiss() }
        }
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        //        }
    }

    private fun showAlertMessage(message: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun updateFCM(deviceId: String, userKey: String?, newToken: String) {
        val stringRequest: StringRequest = object : StringRequest(Method.POST, Global.URL_UPDATE_FCM, Response.Listener { response ->
            Log.d("fcm", "onResponse: $response")
            try {
                val jsonObject = JSONObject(response)
                val requestError = jsonObject.optInt("error")
                if (requestError == 0) {
                    Utils.setFlagToken(this@MainMenuActivity, "0")
                } else Utils.setFlagToken(this@MainMenuActivity, "1")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Log.d("fcmerror", "onErrorResponse: $error")
            Utils.setFlagToken(this@MainMenuActivity, "1")
        }) {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val body = "&act=updatefcm&userkey=$userKey&deviceid=$deviceId&fcm=$newToken"
                return body.toByteArray(charset("utf-8"))
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["apikey"] = Global.APIKEY
                return headers
            }
        }
        ApplicationController.getInstance(this)?.addToRequestQueue(stringRequest)
    }

    //    public void restoreMain(){
    //        count = 1;
    //        imgLine1.setImageResource(R.mipmap.line_bg);
    //        imgLine2.setImageResource(R.mipmap.line_bg);
    //        imgLine3.setImageResource(R.mipmap.line_bg);
    //        imgLine4.setImageResource(R.mipmap.line_bg);
    //        imgLine5.setImageResource(R.mipmap.line_bg);
    //        imgLine6.setImageResource(R.mipmap.line_bg);
    //        btnStart.setImageResource(R.mipmap.button_done);
    //        txvTitle.setText("Thành tâm khấn nguyện!");
    //        txvTitle.setTextSize(20);
    //        txvCount.setText("Hào 1");
    //        txvCount.setTextSize(15);
    //        idHexe = "";
    //        flag = "";
    //        iDHexegram = "";
    //    }
    override fun onResume() {
        super.onResume()
        //restoreMain();
        val filter = IntentFilter("sendMessageBroadcast")
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver!!)
    }

    private fun setBtnGieoQueNhanh(photoLink: String?) {
        if (photoLink != null) {
            imageLoader = ApplicationController.getInstance(this)?.imageLoader
            imageLoader?.get(photoLink, ImageLoader.getImageListener(btnGieoQueNhanh, 0, 0))
            btnGieoQueNhanh!!.setImageUrl("url", imageLoader)
        }
    }

    private fun clickBtnGieoQueNhanh(url: String, idAds: String) {
        btnGieoQueNhanh!!.setOnClickListener {
            Log.d("idAds", "idAds $idAds")
            clickAds(idAds)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun clickAds(idAds: String) {
        val stringRequest: StringRequest = object : StringRequest(Method.POST, Global.URL_DETAIL_CLICK_ADS, Response.Listener { response -> Log.d("responseClickAds", "onResponse: $response") }, Response.ErrorListener { error -> Log.d("responseClickAdsError", "onResponse: $error") }) {
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

    companion object {
        private const val REQUEST_ID_WRITE_PERMISSION = 2
    }
}
@file:Suppress("DEPRECATION")

package com.cannshine.fortune.mainmenu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
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
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.cannshine.fortune.BuildConfig
import com.cannshine.fortune.db.Database
import com.cannshine.fortune.R
import com.cannshine.fortune.base.BaseActivity
import com.cannshine.fortune.databinding.ActivityMainMenuBinding
import com.cannshine.fortune.model.AdsManager
import com.cannshine.fortune.utils.CheckInternet
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.detail.DetailActivity
import com.cannshine.fortune.firebase.GetValuesToFirebase
import com.cannshine.fortune.model.Hexagram
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.startapp.android.publish.adsCommon.StartAppAd
import com.startapp.android.publish.adsCommon.StartAppSDK
import java.io.IOException
import java.util.*

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainMenuActivity : BaseActivity() {
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
    var data = Hexagram()
    var dataHexegram = Database(this)
    var arrayList = ArrayList<Int>()
    var broadcastReceiver: BroadcastReceiver? = null
    lateinit var binding: ActivityMainMenuBinding
    override fun getContentView(): Int {
        return R.layout.activity_main_menu
    }
    lateinit var mainMenuViewModel: MainMenuViewModel
    lateinit var getValuesToFirebase: GetValuesToFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainMenuViewModel = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu)
        // Admob Banner
        if (CheckInternet.isConnected(this)) {
            val admobInfo = Utils.getAdsInfo(this, Global.KEY_ADMOB)
            val appId = admobInfo[Global.ADMOB_APP_ID]!!.trim { it <= ' ' }
            Log.d("admob", "onCreate: $appId")
            val relativeLayout = findViewById<RelativeLayout>(R.id.admobBanner)
            val mAdView = AdView(this)
            MobileAds.initialize(this, appId)
            mAdView.adSize = AdSize.BANNER
            mAdView.adUnitId = admobInfo[Global.ADMOB_BANNER_ID]
            relativeLayout.addView(mAdView)
            val adRequest = AdRequest.Builder().addTestDevice("B852C1784AC94383A068EC6C168A15F8").build()
            mAdView.loadAd(adRequest)

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

        mainMenuViewModel.requestUser()
        mainMenuViewModel.getVersion {
            if (it?.version_check != null) {
                if (it.version_check != BuildConfig.VERSION_CODE.toString()) showAlertUpdate(it.version_name, it.store_url, it.need_update)
            }
        }
        mediaShakeTortoise = MediaPlayer.create(this@MainMenuActivity, R.raw.sowhexagram)
        mediaUpCoin = MediaPlayer.create(this@MainMenuActivity, R.raw.coin)
        val fontTxv = Utils.getFontType(this)
        binding.txvTitle.setTypeface(fontTxv)
        binding.txvCount.setTypeface(fontTxv)
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
            binding.btnGieoQueNhanh.setVisibility(View.GONE)
        }

        // Kiểm tra xem đã có setting trước khi đó chưa
        val shared = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
        val string_temp = shared.getString(KEY_SOUND, "")!!.trim { it <= ' ' }
        if (string_temp == "1" == false && string_temp == "0" == false) {
            setImageSound()
        } else if (string_temp == "1") {
            binding.btnSound.setImageResource(R.drawable.btn_unmute)
            mediaUpCoin?.setVolume(1f, 1f)
            mediaShakeTortoise?.setVolume(1f, 1f)
        } else {
            binding.btnSound.setImageResource(R.drawable.btn_mute)
            mediaUpCoin?.setVolume(0f, 0f)
            mediaShakeTortoise?.setVolume(0f, 0f)
        }

        // sự kiện nhấn nút mute
        clickBtnSound()
        binding.btnDone.setOnClickListener {
            val check = askPermission(REQUEST_ID_WRITE_PERMISSION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            initAnimation()
            if (check == true) {
                arrayList = mainMenuViewModel.arrayNumber()
                count += 1
                if (count <= 6) {
                    binding.imgTortoise.startAnimation(downTortoise)
                } else if (count == 7) {
                    binding.imgTortoise.startAnimation(downTortoise)
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
        binding.imgCoin1.visibility = View.INVISIBLE
        binding.imgCoin2.visibility = View.INVISIBLE
        binding.imgCoin3.visibility = View.INVISIBLE
    }

    fun setInvisibleCoin_UpCoin() {
        binding.imgCoin1.clearAnimation()
        binding.imgCoin2.clearAnimation()
        binding.imgCoin3.clearAnimation()
        binding.imgCoin1.visibility = View.INVISIBLE
        binding.imgCoin2.visibility = View.INVISIBLE
        binding.imgCoin3.visibility = View.INVISIBLE
    }

    fun setVisibleCoin() {
        binding.imgCoin1.visibility = View.VISIBLE
        binding.imgCoin2.visibility = View.VISIBLE
        binding.imgCoin3.visibility = View.VISIBLE
    }

    fun setImageCoin() {
        if (arrayList[0] == 0) {
            binding.imgCoin1.setImageResource(R.drawable.icon_coin_0)
            if (arrayList[1] == 0) {
                binding.imgCoin2.setImageResource(R.drawable.icon_coin_0)
                if (arrayList[2] == 0) {
                    binding.imgCoin3.setImageResource(R.drawable.icon_coin_0)
                } else binding.imgCoin3.setImageResource(R.drawable.icon_coin_1)
            } else {
                binding.imgCoin2.setImageResource(R.drawable.icon_coin_1)
                if (arrayList[2] == 0) {
                    binding.imgCoin3.setImageResource(R.drawable.icon_coin_0)
                } else binding.imgCoin3.setImageResource(R.drawable.icon_coin_1)
            }
        } else {
            binding.imgCoin1.setImageResource(R.drawable.icon_coin_1)
            if (arrayList[1] == 0) {
                binding.imgCoin2.setImageResource(R.drawable.icon_coin_0)
                if (arrayList[2] == 0) {
                    binding.imgCoin3.setImageResource(R.drawable.icon_coin_0)
                } else binding.imgCoin3.setImageResource(R.drawable.icon_coin_1)
            } else {
                binding.imgCoin2.setImageResource(R.drawable.icon_coin_1)
                if (arrayList[2] == 0) {
                    binding.imgCoin3.setImageResource(R.drawable.icon_coin_0)
                } else binding.imgCoin3.setImageResource(R.drawable.icon_coin_1)
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
                    binding.imgLine1.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine1.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine1.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine1.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 2) {
            h2 = if (isDynamic == true) {
                if (type == yang) {
                    binding.imgLine2.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine2.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine2.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine2.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 3) {
            h3 = if (isDynamic == true) {
                if (type == yang) {
                    binding.imgLine3.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine3.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine3.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine3.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 4) {
            h4 = if (isDynamic == true) {
                if (type == yang) {
                    binding.imgLine4.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine4.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine4.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine4.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else if (line == 5) {
            h5 = if (isDynamic == true) {
                if (type == yang) {
                    binding.imgLine5.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine5.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine5.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine5.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        } else {
            h6 = if (isDynamic == true) {
                if (type == yang) {
                    binding.imgLine6.setImageResource(R.mipmap.line_dynamic_1)
                    "dynamic_1"
                } else {
                    binding.imgLine6.setImageResource(R.mipmap.line_dynamic_0)
                    "dynamic_0"
                }
            } else {
                if (type == yang) {
                    binding.imgLine6.setImageResource(R.mipmap.line_normal_1)
                    "normal_1"
                } else {
                    binding.imgLine6.setImageResource(R.mipmap.line_normal_0)
                    "normal_0"
                }
            }
        }

        //return IDHexegram
    }

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
        //data = dataHexegram.getValues(idHexe)!!
        mainMenuViewModel.getTitleHexagram(success = {
            data = it
            val name = data.h_name
            binding.txvTitle.textSize = 15f
            binding.txvTitle.text = name
        }, idHexe)

    }

    //dao nguoc chuoi
    fun reverseID(id: String?): String {
        return StringBuffer(id!!).reverse().toString()
    }

    fun buttomShare() {
        binding.btnShare.setOnClickListener {
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
        if (canWrite) {
            mainMenuViewModel.getChildCountHexagram {
            if (it != 64){ // 64 là số lượng xăm trong db
                createDB()
                // Lưu data vào realTimeDB
                getValuesToFirebase = GetValuesToFirebase(dataHexegram)
                getValuesToFirebase.writeHexagram() }
            }
        }
    }

    // Với Android Level >= 23 bạn phải hỏi người dùng cho phép các quyền với thiết bị
    @Suppress("SameParameterValue")
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
                        mainMenuViewModel.getChildCountHexagram {
                            if (it != 64){ // 64 là số lượng xăm trong db
                                createDB()
                                // Lưu data vào realTimeDB
                                getValuesToFirebase = GetValuesToFirebase(dataHexegram)
                                getValuesToFirebase.writeHexagram()
                            }
                        }
                    }
                }
            }
        }
    }


    fun initAnimationDownTortoise(distance: Int) {
        downTortoise = TranslateAnimation(0F, 0F, 0F, distance.toFloat())
        downTortoise?.setFillAfter(true)
        downTortoise?.setDuration(1000)
        downTortoise?.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.btnDone.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.imgTortoise.setImageResource(R.drawable.shaketortoise)
                shakeT = binding.imgTortoise.drawable as AnimationDrawable
                shakeT!!.start()
                mediaShakeTortoise!!.start()
                //                mediaShakeTortoise.prepareAsync();
                myCountDownTimer = object : CountDownTimer(2000, 1000) {
                    override fun onTick(l: Long) {}
                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        shakeT!!.stop()
                        mediaShakeTortoise!!.stop()
                        mediaShakeTortoise!!.prepare()
                        setImageCoin()
                        setVisibleCoin()
                        binding.imgCoin1.startAnimation(upCoin1)
                        binding.imgCoin2.startAnimation(upCoin2)
                        binding.imgCoin3.startAnimation(upCoin3)
                        temp = Integer.toString(count)
                        if (count <= 6) {
                            binding.txvCount.text = "Hào $temp"
                        }
                        if (count == 7) {
                            binding.txvCount.text = ""
                            binding.btnDone.setImageResource(R.drawable.button_show)
                        }
                        myCountDownTimer2 = object : CountDownTimer(3000, 1000) {
                            override fun onTick(l: Long) {}
                            override fun onFinish() {
                                binding.imgTortoise.startAnimation(upTortoise)
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
                binding.btnDone.isEnabled = true
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
        binding.imgDisk.getLocationOnScreen(location)
        val yImgDisk = location[1]

        // Lấy tọa độ y của BtnStart
        val location2 = IntArray(2)
        binding.btnDone.getLocationOnScreen(location2)
        val yBtnStart = location2[1]
        val heightDisk = binding.imgDisk.height
        val widthDisk = binding.imgDisk.width
        val heightCoin = binding.imgCoin1.height

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
        binding.btnSound.setOnClickListener {
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
                editor.apply()
                binding.btnSound.setImageResource(R.drawable.btn_mute)
            } else if (string_temp == "0") {
                sound = 1
                mediaShakeTortoise!!.setVolume(1f, 1f)
                mediaUpCoin!!.setVolume(1f, 1f)
                binding.btnSound.setImageResource(R.drawable.btn_unmute)
                val pref: SharedPreferences
                pref = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString(KEY_SOUND, "" + sound)
                editor.apply()
            }
        }
    }

    // set default image Btn Sound
    fun setImageSound() {
        val pref: SharedPreferences
        pref = getSharedPreferences(SOUND_INFO, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(KEY_SOUND, "" + sound)
        editor.apply()
        binding.btnSound.setImageResource(R.drawable.btn_unmute)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun showAlertUpdate(versionName: String, link: String, update: String) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("Ứng dụng đang có phiên bản $versionName. Vui lòng cập nhật!")
                .setTitle("Cập Nhật Ứng Dụng")
                .setPositiveButton("UPDATE") { dialog, id ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(link)
                    startActivity(i)
                }
        if (update == "0") {
            builder.setNegativeButton("CANCEL") { dialog, which -> dialog.dismiss() }
        }
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
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
//            imageLoader = ApplicationController.getInstance(this)?.imageLoader
//            imageLoader?.get(photoLink, ImageLoader.getImageListener(btnGieoQueNhanh, 0, 0))
//            btnGieoQueNhanh!!.setImageUrl("url", imageLoader)
            Glide.with(applicationContext).load(photoLink).into(binding.btnGieoQueNhanh)

        }
    }

    private fun clickBtnGieoQueNhanh(url: String, idAds: String) {
        binding.btnGieoQueNhanh.setOnClickListener {
            Log.d("idAds", "idAds $idAds")
            mainMenuViewModel.clickAds(idAds)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    companion object {
        private const val REQUEST_ID_WRITE_PERMISSION = 2
    }

    fun createDB() {
        dataHexegram.createDB()
    }
}
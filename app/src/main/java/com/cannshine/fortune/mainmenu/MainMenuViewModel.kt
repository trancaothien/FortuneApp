package com.cannshine.fortune.mainmenu

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cannshine.fortune.API
import com.cannshine.fortune.AppApplication
import com.cannshine.fortune.model.*
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import java.util.*

@Suppress("DEPRECATION")
class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    fun requestUser(){
        val checkUser = false //Utils.getUserInfo(AppApplication.application, Global.KEY_USER)
        if (checkUser == false) {
            val isneedUpdate = Utils.getFlagToken(AppApplication.application)
            val needUpdate = isneedUpdate[Global.K_TOKEN]
            if (needUpdate == null) {
                createUser("0")
            } else {
                createUser("1" /*needUpdate*/)
            }
        } else {
            val token = Utils.getFlagToken(AppApplication.application)
            val updateFlag = token[Global.K_TOKEN]
            val deviceId = Utils.getDeviceId(AppApplication.application)
            if (updateFlag != null) {
                if (updateFlag == "1") {
                    val s: SharedPreferences = AppApplication.application.getSharedPreferences(Global.KEY_USER, MODE_PRIVATE)
                    val userKey = s.getString(Global.K_USERKEY, "")
                    val newToken = Utils.getNewToken(AppApplication.application)
                    if (deviceId != null && userKey != null && newToken != null) {
                        updateFCM(deviceId, userKey, newToken)
                    }
                }
            }
        }
    }

    fun createUser(isNeedUpdate: String){
        val country = AppApplication.application.resources.configuration.locale.country
        val deviceId = Utils.getDeviceId(AppApplication.application)
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.createUser("newuser", "android", deviceId.toString(), country, 1)
        call.enqueue(object : Callback<UserStatus> {
            override fun onResponse(call: Call<UserStatus>, response: retrofit2.Response<UserStatus>) {
                val user = response.body()?.payload
                val user_id = user?.user_id
                val user_key = user?.user_key
                if (user_id != null && user_key != null) {
                    Utils.saveUserInfo(AppApplication.application, Global.KEY_USER, user_id, user_key)
                    if (isNeedUpdate == "1") {
                        val newToken = Utils.getNewToken(AppApplication.application)
                        updateFCM(Utils.getDeviceId(AppApplication.application)!!, user_key, newToken!!)
                    }
                }
            }

            override fun onFailure(call: Call<UserStatus>, t: Throwable) {
                Log.d("crateUserError", "onFailure: " + t.toString())
            }

        })
    }

    fun updateFCM(deviceId: String, userKey: String, newToken: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.updateFCM("updatefcm", userKey, deviceId, newToken)

        call.enqueue(object : Callback<UpdateFCM>{
            override fun onResponse(call: Call<UpdateFCM>, response: retrofit2.Response<UpdateFCM>) {
                val updateFCM = response.body()
                if (updateFCM?.error == 0){
                    Utils.setFlagToken(AppApplication.application, "0")
                }else{
                    Utils.setFlagToken(AppApplication.application, "1")
                }
            }

            override fun onFailure(call: Call<UpdateFCM>, t: Throwable) {
                Utils.setFlagToken(AppApplication.application, "1")
            }

        })
    }

    fun clickAds(id: String){
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.clickAds("clickads", id, 1, "android")
        call.enqueue(object : Callback<ClickAds>{
            override fun onResponse(call: Call<ClickAds>, response: retrofit2.Response<ClickAds>) {
//                val clickAds = response.body()
            }

            override fun onFailure(call: Call<ClickAds>, t: Throwable) {

            }

        })
    }

    fun getVersion(succes: (AppVersionInfo?) -> Unit){
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.getAppVersion("checkAppVersion", "1", "android")
        call.enqueue(object : Callback<GetAppVersionStatus> {
            override fun onResponse(call: Call<GetAppVersionStatus>, response: retrofit2.Response<GetAppVersionStatus>) {
                val appVersionInfo = response.body()?.payload
                succes(appVersionInfo)
            }

            override fun onFailure(call: Call<GetAppVersionStatus>, t: Throwable) {
                Log.d("getAppVersionFail", "onFailure: ${t.toString()}")
            }

        })
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

    fun getTitleHexagram(success: (Hexagram) -> Unit,idHexagram: String){
        val databaseReference = Firebase.database.reference.child("hexagram").child(idHexagram)
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                success(snapshot.getValue(Hexagram::class.java)!!)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}
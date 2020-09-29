package com.cannshine.fortune.firebase

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cannshine.fortune.API
import com.cannshine.fortune.AppApplication
import com.cannshine.fortune.splash.SplashActivity
import com.cannshine.fortune.utils.Global
import com.cannshine.fortune.utils.Utils
import com.cannshine.fortune.R
import com.cannshine.fortune.model.UpdateFCM
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback

class MyFirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // handle a notification payload.
        if (remoteMessage.notification != null) {
            if (isAppRunning(this, "com.cannshine.Fortune")) {
                Log.d(TAG, "Message Notification Body: $remoteMessage")
                val body = remoteMessage.notification!!.body
                val title = remoteMessage.notification!!.title
                val intent = Intent("sendMessageBroadcast")
                intent.putExtra("body", body)
                intent.putExtra("title", title)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            } else {
                sendNotification(remoteMessage)
            }
        }
    }

    override fun onNewToken(s: String) {
        Utils.setFlagToken(this, "1")
        Utils.saveNewToken(this, s)
        val deviceId = Utils.getDeviceId(this@MyFirebaseService)
        val sharedPreferences = getSharedPreferences(Global.KEY_USER, MODE_PRIVATE)
        val userKey = sharedPreferences.getString(Global.K_USERKEY, "")
        if (deviceId != null && userKey != null){
            if (userKey == "" == false) updateFCM(deviceId, userKey, s)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    fun updateFCM(deviceId: String, userKey: String, token: String) {
        val request = API.buildService(API.AppRepository::class.java)
        val call = request.updateFCM("updatefcm", userKey, deviceId, token)

        call.enqueue(object : Callback<UpdateFCM> {
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

    companion object {
        private const val TAG = "MyFirebaseService"
    }
}
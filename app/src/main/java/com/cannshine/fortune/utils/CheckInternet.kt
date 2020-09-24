package com.cannshine.fortune.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class CheckInternet {
    companion object{
        fun isConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED) {
                // is connected
                true
            } else false
        }
    }
}
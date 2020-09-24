package com.cannshine.fortune

import android.app.Application
import androidx.multidex.MultiDex

class AppApplication : Application() {
    companion object {
        public lateinit var application: AppApplication
        fun getApp(): AppApplication {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        MultiDex.install(this)

    }

}
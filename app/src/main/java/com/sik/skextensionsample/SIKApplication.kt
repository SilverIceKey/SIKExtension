package com.sik.skextensionsample

import android.app.Application
import ch.qos.logback.classic.Level
import com.sik.sikcore.SIKCore
import com.sik.sikcore.log.LogUtils

class SIKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SIKCore.init(this)
        LogUtils.setLogLevel(packageName, Level.INFO)
    }
}
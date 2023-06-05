package com.sik.skextensionsample

import android.app.Application
import com.sik.sikroute.RouteManager

class SIKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RouteManager.instance.init(DefaultRouteConfig::class)
    }
}
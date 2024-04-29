package com.sik.skextensionsample

import android.app.Application
import com.sik.sikcore.SIKCore

class SIKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SIKCore.init(this)
    }
}
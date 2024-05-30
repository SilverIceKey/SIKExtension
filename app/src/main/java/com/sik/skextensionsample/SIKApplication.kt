package com.sik.skextensionsample

import android.app.Application
import com.sik.sikcore.SIKCore
import com.sik.sikcore.explain.AnnotationScanner

class SIKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SIKCore.init(this)
        AnnotationScanner.scan(ScanConfiguration::class)
    }
}
package com.sik.skextensionsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.sik.sikcore.data.GlobalDataTempStore
import com.sik.sikcore.log.LogUtils
import kotlin.math.log

class MainActivity : ComponentActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.i("onCreate")
    }
}
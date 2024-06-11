package com.sik.skextensionsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.timer.TimerLiveData

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TimerLiveData(1000, 60000, true).observe(this) {
            logger.i("计时器:${it / 1000}")
        }
    }
}
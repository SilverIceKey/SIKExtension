package com.sik.skextensionsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)

    @field:LogInfo(description = "请求地址")
    val http = "http://127.0.0.1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.i(this::class)
        logger.i(http)
        logger.copyLogFileToPublicStorage()
    }
}
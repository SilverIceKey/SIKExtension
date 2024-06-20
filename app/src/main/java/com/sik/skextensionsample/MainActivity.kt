package com.sik.skextensionsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.siksensors.FingerConfig
import com.sik.siksensors.FingerErrorEnum
import com.sik.siksensors.FingerUtils

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.finger).setOnClickListener {
            FingerUtils.authenticateFingerprint(fingerConfig = FingerConfig.defaultConfig.apply {
                useSystemDialog = false
            }) {
                logger.i("${it.message}")
            }
        }
    }
}
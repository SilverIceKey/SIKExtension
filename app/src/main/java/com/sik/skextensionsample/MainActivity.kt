package com.sik.skextensionsample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.generator.PasswordGenerator
import com.sik.sikcore.log.LogUtils
import com.sik.siksensors.fingerprints.FingerPrintsConfig
import com.sik.siksensors.fingerprints.FingerPrintsUtils
import kotlin.concurrent.thread
import kotlin.system.measureNanoTime

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    private var totalTimeInNano = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.generate_password).setOnClickListener {
            val password = PasswordGenerator.generatePassword(16)
            findViewById<TextView>(R.id.password).text = password
        }
    }
}
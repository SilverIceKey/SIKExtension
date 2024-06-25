package com.sik.skextensionsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.device.VibratorUtils
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.extension.toJson
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.timer.TimerLiveData

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    private var isVibrate:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.generate_password).setOnClickListener {
            if (isVibrate){
                VibratorUtils.cancel()
                isVibrate = false
            }else{
                VibratorUtils.checkPermission{
                    logger.i("权限:${it}")
                    logger.i("${VibratorUtils.hasVibrator()}")
                    logger.i("${VibratorUtils.vibratorIds().toJson()}")
                    VibratorUtils.pattern.apply {
                        add(300)
                        add(300)
                        add(300)
                        add(300)
                    }
                    VibratorUtils.amplitudes.apply {
                        add(0)
                        add(255)
                        add(0)
                        add(100)
                    }
                    logger.i("开始短震")
//                VibratorUtils.vibrate(1000)
                    VibratorUtils.vibrate(VibratorUtils.VibrateMode.INFINITE)
//                TimerLiveData(1000, 2000).observe(this) {
//                    logger.i("频率震动")
//                    TimerLiveData(1000, 3000).observe(this) {
//                        logger.i("停止震动")
//                        VibratorUtils.cancel()
//                    }
//                }
                    isVibrate = true
                }
            }
        }
    }
}
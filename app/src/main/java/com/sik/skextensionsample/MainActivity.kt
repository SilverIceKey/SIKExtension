package com.sik.skextensionsample

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import ch.qos.logback.classic.Level
import com.sik.sikcore.activity.NightModeChangeListener
import com.sik.sikcore.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : ComponentActivity() {
    private val logger: Logger = LoggerFactory.getLogger(MainActivity::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置内容
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.generate_code).setOnClickListener {
            if (LogUtils.getLogLevel(packageName) == Level.INFO) {
                LogUtils.setLogLevel(packageName, Level.DEBUG)
            } else {
                LogUtils.setLogLevel(packageName, Level.INFO)
            }
            logger.debug("设置日志等级: {}", LogUtils.getLogLevel(packageName))
        }
    }

    @NightModeChangeListener
    fun nightModeChangeListener(nightMode: Int) {
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            logger.info("深色模式已启动")
        } else if (nightMode == Configuration.UI_MODE_NIGHT_NO) {
            logger.debug("深色模式已关闭")
        }
    }
}
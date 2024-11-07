package com.sik.skextensionsample

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import ch.qos.logback.classic.Level
import com.sik.sikcore.activity.NightModeChangeListener
import com.sik.sikcore.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import com.sik.siknet.NetUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : AppCompatActivity() {
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
        PermissionUtils.checkAndRequestPermissions(
            listOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
        ) {
            if (it) {
                NetUtil.connectToWifi("yangtian5G", "88921469") {
                }
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
}
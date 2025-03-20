package com.sik.skextensionsample

import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.window.embedding.SplitController
import ch.qos.logback.classic.Level
import com.sik.sikcore.activity.NightModeChangeListener
import com.sik.sikcore.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
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
        logger.info("分屏支持:${SplitController.getInstance(this).splitSupportStatus == SplitController.SplitSupportStatus.SPLIT_AVAILABLE}")
        findViewById<Button>(R.id.generate_code).setOnClickListener {
            if (LogUtils.getLogLevel(packageName) == Level.INFO) {
                LogUtils.setLogLevel(packageName, Level.DEBUG)
            } else {
                LogUtils.setLogLevel(packageName, Level.INFO)
            }
            logger.debug("设置日志等级: {}", LogUtils.getLogLevel(packageName))
        }
        findViewById<Button>(R.id.split).setOnClickListener {
            startActivity(Intent(this, SecActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
        }
        findViewById<Button>(R.id.split_another_app).setOnClickListener {
            val intent = Intent().apply {
//                setComponent(ComponentName("com.google.android.calculator", "com.android.calculator2.Calculator"))
                setComponent(ComponentName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2"))
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "文件管理器不可用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        logger.info("配置转换")
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
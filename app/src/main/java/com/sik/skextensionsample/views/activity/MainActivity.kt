package com.sik.skextensionsample.views.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sik.sikandroid.activity.NightModeChangeListener
import com.sik.sikandroid.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.skextensionsample.R
import com.sik.skextensionsample.data.FeatureEntry
import com.sik.skextensionsample.databinding.ActivityMainBinding
import com.sik.skextensionsample.views.adapter.FeatureAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var adapter: FeatureAdapter

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 设置内容
        val featureList = listOf(
            FeatureEntry("基础功能", "最简单的调用方式") {

            },
            FeatureEntry("动画示例", "展示动画相关接口") {

            },

            FeatureEntry("加解密实例", "展示加解密相关接口") {
                startActivity(Intent(this, EncryptorActivity::class.java))
            }
            // 可继续添加
        )

        adapter = FeatureAdapter(featureList)
        findViewById<RecyclerView>(R.id.featureList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
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
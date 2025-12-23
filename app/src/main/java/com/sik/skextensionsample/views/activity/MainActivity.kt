package com.sik.skextensionsample.views.activity

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sik.sikandroid.activity.NightModeAware
import com.sik.sikandroid.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.skextensionsample.R
import com.sik.skextensionsample.data.FeatureEntry
import com.sik.skextensionsample.databinding.ActivityMainBinding
import com.sik.skextensionsample.views.adapter.FeatureAdapter

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : BaseActivity<ActivityMainBinding>(), NightModeAware {

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
            FeatureEntry(
                "下载示例", "展示下载相关接口", listOf(
                    FeatureEntry("下载图片", "下载图片") {
                        startActivity(Intent(this, DownloadActivity::class.java))
                    }
                )),

            FeatureEntry(
                "加解密实例", "展示加解密相关接口", listOf(
                FeatureEntry("AES", "AES加解密") {
                    startActivity(Intent(this, EncryptorActivity::class.java))
                }
            ))
            // 可继续添加
        )

        adapter = FeatureAdapter()
        findViewById<RecyclerView>(R.id.featureList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        adapter.setData(featureList)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i("MainActivity","配置转换")
    }

    override fun onNightModeChanged(mode: Int) {
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            Log.i("MainActivity","深色模式已启动")
        } else if (mode == Configuration.UI_MODE_NIGHT_NO) {
            Log.i("MainActivity","深色模式已关闭")
        }
    }
}
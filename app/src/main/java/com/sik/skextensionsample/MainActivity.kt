package com.sik.skextensionsample

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikimage.WatermarkConfig
import com.sik.sikimage.addWatermark

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.i(this::class)
        logger.i(ObjectDemo.demoField)
        logger.i(ObjectDemo.demoField2)
        findViewById<ImageView>(R.id.image).apply {
            var image = BitmapFactory.decodeResource(resources, R.drawable.aa)
            image = image.addWatermark(WatermarkConfig().apply {
                text = mutableListOf("无证")
            })
            setImageBitmap(image)
        }
    }
}
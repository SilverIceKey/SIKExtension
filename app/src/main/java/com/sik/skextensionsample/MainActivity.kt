package com.sik.skextensionsample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikimage.BarCodeUtils
import com.sik.sikimage.QRCodeUtils

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    private var isVibrate: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val codeData = findViewById<EditText>(R.id.code_data)
        val qrCode = findViewById<ImageView>(R.id.qr_code)
        val barCode = findViewById<ImageView>(R.id.bar_code)
        findViewById<Button>(R.id.generate_code).setOnClickListener {
            qrCode.setImageBitmap(QRCodeUtils.createQRCode(codeData.text.toString(), 200, withInfo = true))
            barCode.setImageBitmap(BarCodeUtils.createBarCode(codeData.text.toString(), 200, 100, withInfo = true))
        }
    }
}
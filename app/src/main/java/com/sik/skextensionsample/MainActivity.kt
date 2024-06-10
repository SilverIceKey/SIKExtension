package com.sik.skextensionsample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.extension.file
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import com.sik.sikencrypt.MessageDigestTypes
import com.sik.sikencrypt.MessageDigestUtils
import com.sik.sikencrypt.message_digest.MD5MessageDigest
import com.sik.sikencrypt.message_digest.SM3MessageDigest

@LogInfo(description = "进入主界面")
class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionUtils.requestAllFilesAccessPermission {
            findViewById<ImageView>(R.id.image).apply {
                this.load("/sdcard/Documents/123.jpg")
            }
        }
        findViewById<ImageView>(R.id.image).apply {
            this.load("/sdcard/Documents/123.jpg")
        }
        logger.i(MessageDigestUtils.getMode(MessageDigestTypes.SM3).digestToHex("/sdcard/Documents/123.jpg".file().readBytes()))
        logger.i(MessageDigestUtils.getMode(MessageDigestTypes.MD5).digestToHex("/sdcard/Documents/123.jpg".file().readBytes()))
        logger.i(MessageDigestUtils.getMode(MessageDigestTypes.SHA256).digestToHex("/sdcard/Documents/123.jpg".file().readBytes()))
    }
}
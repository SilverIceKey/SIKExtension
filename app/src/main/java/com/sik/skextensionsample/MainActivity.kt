package com.sik.skextensionsample

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import com.sik.sikencrypt.EncryptUtils
import com.sik.sikencrypt.MessageDigestTypes
import com.sik.sikencrypt.MessageDigestUtils

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
        logger.i(
            MessageDigestUtils.getMode(MessageDigestTypes.SM3)
                .digestFile("/sdcard/Documents/123.jpg")
        )
        logger.i(
            MessageDigestUtils.getMode(MessageDigestTypes.MD5)
                .digestFile("/sdcard/Documents/123.jpg")
        )
        logger.i(
            MessageDigestUtils.getMode(MessageDigestTypes.SHA256)
                .digestFile("/sdcard/Documents/123.jpg")
        )


        EncryptUtils.getAlgorithm(AESEncryptConfig())
            .encryptFile("/sdcard/Documents/123.jpg", "/sdcard/Documents/456AES.jpg")
        EncryptUtils.getAlgorithm(AESEncryptConfig())
            .decryptFromFile("/sdcard/Documents/456AES.jpg", "/sdcard/Documents/789AES.jpg")
        EncryptUtils.getAlgorithm(DESEncryptConfig())
            .encryptFile("/sdcard/Documents/123.jpg", "/sdcard/Documents/456DES.jpg")
        EncryptUtils.getAlgorithm(DESEncryptConfig())
            .decryptFromFile("/sdcard/Documents/456DES.jpg", "/sdcard/Documents/789DES.jpg")
        EncryptUtils.getAlgorithm(DESedeEncryptConfig())
            .encryptFile("/sdcard/Documents/123.jpg", "/sdcard/Documents/456DESede.jpg")
        EncryptUtils.getAlgorithm(DESedeEncryptConfig())
            .decryptFromFile("/sdcard/Documents/456DESede.jpg", "/sdcard/Documents/789DESede.jpg")
        EncryptUtils.getAlgorithm(SM4EncryptConfig())
            .encryptFile("/sdcard/Documents/123.jpg", "/sdcard/Documents/456SM4.jpg")
        EncryptUtils.getAlgorithm(SM4EncryptConfig())
            .decryptFromFile("/sdcard/Documents/456SM4.jpg", "/sdcard/Documents/789SM4.jpg")
    }
}
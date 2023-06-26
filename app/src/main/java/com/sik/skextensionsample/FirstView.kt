package com.sik.skextensionsample

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.sik.sikcore.log.LogUtils
import com.sik.sikencrypt.EncryptUtils
import com.sik.sikencrypt.MessageDigestTypes
import com.sik.sikencrypt.MessageDigestUtils
import com.sik.sikroute.BaseView
import java.io.File

class FirstView : BaseView() {
    override fun initViewModel() {

    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Composable
    override fun InitView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clickable(true) {
//                    navController.navigate("sec")
//                    NettyClientUtils.instance.connect(CustomNettyConfig())
                    iRoute.startActivity(SecActivity::class.java)
                }
        ) {
            Text(text = "第一个页面")
        }
        LogUtils.i("第一个页面")
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            iRoute.startActivityUseIntent(intent)
        } else {
            val file = File("/sdcard/Documents/123.jpg")
            val tempFile1 = File("/sdcard/Documents/1234.jpg")
            val tempFile2 = File("/sdcard/Documents/1235.jpg")
            LogUtils.i(
                "信息摘要${
                    MessageDigestUtils.getMode(MessageDigestTypes.SM3).digestToHex(file.readBytes())
                }"
            )
            val encryptConfig = EncryptConfig()
            LogUtils.i("AES密钥:${String(encryptConfig.key())}")
            LogUtils.i("AES偏移:${String(encryptConfig.iv() ?: ByteArray(0))}")
            val iEncrypt = EncryptUtils.getAlgorithm(encryptConfig)
            tempFile1.writeBytes(iEncrypt.encryptToByteArray(file.readBytes()))
            tempFile2.writeBytes(iEncrypt.decryptFromByteArray(tempFile1.readBytes()))
        }
    }
}
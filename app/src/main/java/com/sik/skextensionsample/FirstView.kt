package com.sik.skextensionsample

import android.os.Build
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
import com.sik.sikroute.BaseView

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
//                    iRoute.startActivity(SecActivity::class.java)
                    aesTest()
                }
        ) {
            Text(text = "第一个页面")
        }
        aesTest()
//        if (!Environment.isExternalStorageManager()) {
//            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//            iRoute.startActivityUseIntent(intent)
//        } else {
//            val file = File("/sdcard/Documents/123.jpg")
//            val tempFile1 = File("/sdcard/Documents/1234.jpg")
//            val tempFile2 = File("/sdcard/Documents/1235.jpg")
//            LogUtils.i(
//                "信息摘要${
//                    MessageDigestUtils.getMode(MessageDigestTypes.SM3).digestToHex(file.readBytes())
//                }"
//            )
//            val encryptConfig = EncryptConfig()
//            LogUtils.i("AES密钥:${String(encryptConfig.key())}")
//            LogUtils.i("AES偏移:${String(encryptConfig.iv() ?: ByteArray(0))}")
//            val iEncrypt = EncryptUtils.getAlgorithm(encryptConfig)
//            tempFile1.writeBytes(iEncrypt.encryptToByteArray(file.readBytes()))
//            tempFile2.writeBytes(iEncrypt.decryptFromByteArray(tempFile1.readBytes()))
//        }
    }

    private fun aesTest() {
        val encryptConfig = EncryptConfig()
        LogUtils.i("AES密钥:${String(encryptConfig.key())}")
        LogUtils.i("AES偏移:${String(encryptConfig.iv() ?: ByteArray(0))}")
        val iEncrypt = EncryptUtils.getAlgorithm(encryptConfig)
        val encryptResult = iEncrypt.encryptToBase64("123".toByteArray())
        LogUtils.i("AES加密123:${encryptResult}")
        LogUtils.i("AES加密123:${iEncrypt.decryptFromBase64(encryptResult)}")
    }
}
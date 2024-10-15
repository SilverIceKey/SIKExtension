package com.sik.skextensionsample

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter
import com.sik.sikcore.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikencrypt.EncryptUtils
import com.sik.sikimage.QRCodeUtils
import com.sik.sikmedia.audio_process.AudioProcessor
import java.io.File

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : ComponentActivity() {
    private val audioProcessor = AudioProcessor()
    private var errmsg = mutableStateOf("")
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置内容
        setContent {
            var decodeStr by remember { mutableStateOf("") }
            Scaffold { contentPadding ->
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(File("/sdcard/1.png")),
                        contentDescription = ""
                    )
                    Text(text = decodeStr)
                    Button(onClick = {
                        EncryptUtils.getAlgorithm(EncryptorCBCConfig.encryptorConfig).encryptFile("/sdcard/1.pdf","/sdcard/1.yt")
                        EncryptUtils.getAlgorithm(EncryptorCBCConfig.encryptorConfig).decryptFromFile("/sdcard/1.yt","/sdcard/2.pdf")
                    }) {
                        Text(text = "加解密")
                    }
                }
            }
        }
    }
}
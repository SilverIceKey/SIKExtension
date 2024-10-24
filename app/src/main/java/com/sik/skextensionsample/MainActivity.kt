package com.sik.skextensionsample

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.sik.sikcore.activity.NightModeChangeListener
import com.sik.sikcore.activity.SecureActivity
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.log.LogUtils
import com.sik.sikmedia.audio_process.AudioProcessor

@LogInfo(description = "进入主界面")
@SecureActivity
class MainActivity : ComponentActivity() {
    private val audioProcessor = AudioProcessor()
    private var errmsg = mutableStateOf("")
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置内容
        setContentView(R.layout.activity_main)
//        setContent {
//            var decodeStr by remember { mutableStateOf("") }
//            Scaffold { contentPadding ->
//                Column(
//                    modifier = Modifier
//                        .padding(contentPadding)
//                        .fillMaxSize(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Image(
//                        painter = rememberAsyncImagePainter(File("/sdcard/1.png")),
//                        contentDescription = ""
//                    )
//                    Text(text = decodeStr)
//                    Button(onClick = {
//                        EncryptUtils.getAlgorithm(EncryptorCBCConfig.encryptorConfig)
//                            .encryptFile("/sdcard/1.pdf", "/sdcard/1.yt")
//                        EncryptUtils.getAlgorithm(EncryptorCBCConfig.encryptorConfig)
//                            .decryptFromFile("/sdcard/1.yt", "/sdcard/2.pdf")
//                    }) {
//                        Text(text = "加解密")
//                    }
//                }
//            }
//        }
    }

    @NightModeChangeListener
    fun nightModeChangeListener(nightMode: Int) {
        if (nightMode==Configuration.UI_MODE_NIGHT_YES){
            logger.i("深色模式已启动")
        }else if (nightMode==Configuration.UI_MODE_NIGHT_NO){
            logger.i("深色模式已关闭")
        }
    }
}
package com.sik.skextensionsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sik.sikcore.data.GlobalDataTempStore
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.file.FileUtils
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.thread.ThreadUtils
import com.sik.sikmedia.audio_process.AudioProcessException
import com.sik.sikmedia.audio_process.AudioProcessor
import com.sik.sikmedia.audio_process.ProcessedAudio
import com.sik.sikmedia.audio_process.SimpleSnoreDetector

@LogInfo(description = "进入主界面")
class MainActivity : ComponentActivity() {
    private val audioProcessor = AudioProcessor()
    private var errmsg = mutableStateOf("")
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置内容
        setContent {
            val detectedStatus = remember { mutableStateOf("检测中") }
            val detectedProgress = remember { mutableIntStateOf(0) }
            Scaffold { contentPadding ->
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = detectedStatus.value)
                    Text(text = "${detectedProgress.value}%")
                    Text(text = "错误:${errmsg.value}")
                    Button(onClick = {
                        GlobalDataTempStore.getInstance().saveData("123", "1111")
                    }) {
                        Text(text = "保存数据")
                    }
                    Button(onClick = {
                        ThreadUtils.runOnIO {
                            stressTest()
                        }
                    }) {
                        Text(text = "测试")
                    }
                    Button(onClick = {
                        logger.i("${GlobalDataTempStore.getInstance().hasData("123")}")
                        logger.i("${GlobalDataTempStore.getInstance().getData("123", false)}")
                        errmsg.value = (GlobalDataTempStore.getInstance().getData("123")
                            ?: "暂无数据").toString()
                    }) {
                        Text(text = "获取数据")
                    }
                }
            }
            LaunchedEffect(key1 = Unit) {
                // 添加打鼾检测器
                val snoreDetector = SimpleSnoreDetector().apply {
                    setOnDetectProgressListener { status, progress ->
                        detectedStatus.value = status
                        detectedProgress.intValue = progress
                    }
                }
                audioProcessor.addAnalyzer(snoreDetector)
            }
        }

        // 处理传入的Intent
        handleSendIntent(intent)
    }

    private fun stressTest() {
        val store = GlobalDataTempStore.getInstance()
        for (i in 0 until 100000) {
            val key = "key_$i"
            val value = "value_$i"
            store.saveData(key, value)
            if (i % 1000 == 0) {
                runOnUiThread {
                    errmsg.value = "Saved $i entries"
                }
            }
        }

        println("Starting to retrieve data")
        for (i in 0 until 100000) {
            val key = "key_$i"
            store.getData(key)
            if (i % 1000 == 0) {
                runOnUiThread {
                    errmsg.value = "Retrieved $i entries"
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSendIntent(intent)
    }


    private fun handleSendIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    handleSendSingleFile(uri)
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                    handleSendMultipleFiles(uris)
                }
            }
        }
    }

    private fun handleSendSingleFile(uri: Uri) {
        // 处理单个音频文件
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val outputFilePath = "${filesDir.absolutePath}/output.wav"

        audioProcessor.processAudioFile(
            FileUtils.getFileFromUri(uri)?.absolutePath ?: "",
            outputFilePath,
            object : AudioProcessor.AudioProcessorCallback {
                override fun onSuccess(processedAudio: ProcessedAudio) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Snore detected", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(exception: AudioProcessException) {
                    runOnUiThread {
                        errmsg.value = exception.message ?: ""
                        Toast.makeText(
                            this@MainActivity,
                            "Processing failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun handleSendMultipleFiles(uris: List<Uri>) {
        // 处理多个音频文件
        for (uri in uris) {
            handleSendSingleFile(uri)
        }
    }
}
package com.sik.skextensionsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.file.FileUtils
import com.sik.sikmedia.audio_process.AudioProcessException
import com.sik.sikmedia.audio_process.AudioProcessor
import com.sik.sikmedia.audio_process.ProcessedAudio
import com.sik.sikmedia.audio_process.SimpleSnoreDetector

@LogInfo(description = "进入主界面")
class MainActivity : ComponentActivity() {
    private val audioProcessor = AudioProcessor()
    private var snoreDetected by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 添加打鼾检测器
        val snoreDetector = SimpleSnoreDetector()
        audioProcessor.addAnalyzer(snoreDetector)

        // 设置内容
        setContent {
            Scaffold { contentPadding ->
                Column(
                    modifier = Modifier.padding(contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = if (snoreDetected) "Snore detected!" else "No snore detected.")
                }
            }
        }

        // 处理传入的Intent
        handleSendIntent(intent)
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
                    snoreDetected = true
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Snore detected", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(exception: AudioProcessException) {
                    snoreDetected = false
                    runOnUiThread {
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
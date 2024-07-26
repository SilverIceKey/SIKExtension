package com.sik.sikmedia.audio_process

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.sik.sikcore.log.LogUtils
import java.io.File
import java.io.InputStream

// 定义一个数据类，用于表示处理后的音频文件
data class ProcessedAudio(val filePath: String, val inputStream: InputStream)

// 定义一个音频处理类，包含音频文件处理的逻辑
class AudioProcessor {
    private val logger = LogUtils.getLogger(AudioProcessor::class)

    // 定义回调接口，用于处理音频处理成功或失败的回调
    interface AudioProcessorCallback {
        fun onSuccess(processedAudio: ProcessedAudio) // 成功回调，返回处理后的音频文件
        fun onFailure(exception: AudioProcessException) // 失败回调，返回异常信息
    }

    // 定义一个列表，用于存储动态添加的分析器
    private val analyzers = mutableListOf<AudioAnalyzer>()

    // 添加分析器
    fun addAnalyzer(analyzer: AudioAnalyzer) {
        analyzers.add(analyzer)
    }

    // 移除分析器
    fun removeAnalyzer(analyzer: AudioAnalyzer) {
        analyzers.remove(analyzer)
    }

    // 检测文件格式，并转换成统一的数据结构
    fun processAudioFile(inputFilePath: String, outputFilePath: String, callback: AudioProcessorCallback) {
        val file = File(inputFilePath)
        if (!file.exists()) {
            logger.e("文件不存在")
            // 如果文件不存在，调用失败回调
            callback.onFailure(AudioProcessException(AudioProcessError.FILE_NOT_FOUND, "File not found"))
            return
        }

        // 检查文件后缀
        val validExtensions = listOf("mp3", "wav", "m4a", "flac") // 定义支持的音频文件格式
        val fileExtension = file.extension.lowercase() // 获取文件后缀
        if (fileExtension !in validExtensions) {
            // 如果文件格式无效，调用失败回调
            logger.e("文件格式不支持")
            callback.onFailure(AudioProcessException(AudioProcessError.INVALID_FILE_FORMAT, "Invalid file format"))
            return
        }

        // 转换文件格式
        convertToWav(inputFilePath, outputFilePath, object : AudioProcessorCallback {
            override fun onSuccess(processedAudio: ProcessedAudio) {
                try {
                    // 转换成功后，获取转换后的文件输入流
                    val outputFile = File(outputFilePath)
                    val inputStream = outputFile.inputStream()
                    val processedAudioWithStream = ProcessedAudio(outputFilePath, inputStream)
                    logger.i("开始调用分析器")
                    // 调用所有添加的分析器
                    analyzers.forEach {
                        it.analyze(inputStream) // 注意，这里需要重新打开流
                    }
                    // 调用成功回调
                    callback.onSuccess(processedAudioWithStream)
                } catch (e: Exception) {
                    callback.onFailure(AudioProcessException(AudioProcessError.UNKNOWN_ERROR, e.message ?: "Unknown error"))
                }
            }

            override fun onFailure(exception: AudioProcessException) {
                // 转换失败，调用失败回调
                callback.onFailure(exception)
            }
        })
    }

    // 使用 FFmpegKit 将输入文件转换为 WAV 格式
    private fun convertToWav(inputFilePath: String, outputFilePath: String, callback: AudioProcessorCallback) {
        val command = "-i $inputFilePath $outputFilePath" // 构建 FFmpeg 命令

        // 异步执行 FFmpeg 命令
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                // 如果转换成功，调用成功回调
                callback.onSuccess(ProcessedAudio(outputFilePath, File(outputFilePath).inputStream())) // 传递文件路径和输入流
            } else {
                // 如果转换失败，调用失败回调，并传递错误信息
                callback.onFailure(AudioProcessException(AudioProcessError.CONVERSION_FAILED, session.failStackTrace ?: "Conversion failed"))
            }
        }
    }
}

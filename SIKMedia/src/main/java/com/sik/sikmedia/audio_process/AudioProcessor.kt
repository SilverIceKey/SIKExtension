package com.sik.sikmedia.audio_process

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

// 定义一个数据类，用于表示处理后的音频文件
data class ProcessedAudio(val filePath: String, val inputStream: InputStream)

//超时
private const val TIMEOUT_US = 10_000L

// 定义一个音频处理类，包含音频文件处理的逻辑
class AudioProcessor {

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
    fun processAudioFile(
        inputFilePath: String,
        outputFilePath: String,
        callback: AudioProcessorCallback
    ) {
        val file = File(inputFilePath)
        if (!file.exists()) {
            Log.i("AudioProcessor","文件不存在")
            // 如果文件不存在，调用失败回调
            callback.onFailure(
                AudioProcessException(
                    AudioProcessError.FILE_NOT_FOUND,
                    "File not found"
                )
            )
            return
        }

        // 检查文件后缀
        val validExtensions = listOf("mp3", "wav", "m4a", "flac") // 定义支持的音频文件格式
        val fileExtension = file.extension.lowercase() // 获取文件后缀
        if (fileExtension !in validExtensions) {
            // 如果文件格式无效，调用失败回调
            Log.i("AudioProcessor","文件格式不支持")
            callback.onFailure(
                AudioProcessException(
                    AudioProcessError.INVALID_FILE_FORMAT,
                    "Invalid file format"
                )
            )
            return
        }

        // 转换文件格式
        convertToWav(inputFilePath, outputFilePath, object : AudioProcessorCallback {
            override fun onSuccess(processedAudio: ProcessedAudio) {
                try {
                    // 转换成功后，获取转换后的文件输入流
                    val outputFile = File(outputFilePath)
                    val dataLength = outputFile.length()
                    val inputStream = outputFile.inputStream()
                    val processedAudioWithStream = ProcessedAudio(outputFilePath, inputStream)
                    Log.i("AudioProcessor","开始调用分析器")
                    // 调用所有添加的分析器
                    analyzers.forEach {
                        it.setDataLength(dataLength)
                        it.analyze(inputStream) // 注意，这里需要重新打开流
                    }
                    // 调用成功回调
                    callback.onSuccess(processedAudioWithStream)
                } catch (e: Exception) {
                    callback.onFailure(
                        AudioProcessException(
                            AudioProcessError.UNKNOWN_ERROR,
                            e.message ?: "Unknown error"
                        )
                    )
                }
            }

            override fun onFailure(exception: AudioProcessException) {
                // 转换失败，调用失败回调
                callback.onFailure(exception)
            }
        })
    }

    /**
     * 将任意音频文件解码为 WAV（PCM 16bit LE）格式，不依赖 FFmpeg
     */
    private fun convertToWav(
        inputFilePath: String,
        outputFilePath: String,
        callback: AudioProcessorCallback
    ) {
        try {
            // ——1. 准备 MediaExtractor 读取源文件——
            val extractor = MediaExtractor().apply {
                setDataSource(inputFilePath)
            }
            // 找到第一个音频轨道
            val trackIndex = (0 until extractor.trackCount)
                .firstOrNull { idx ->
                    extractor.getTrackFormat(idx).getString(MediaFormat.KEY_MIME)
                        ?.startsWith("audio/") == true
                } ?: throw AudioProcessException(
                AudioProcessError.INVALID_FORMAT,
                "No audio track found in $inputFilePath"
            )
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            // ——2. 配置解码器——
            val codec = MediaCodec.createDecoderByType(mime).apply {
                configure(format, null, null, 0)
                start()
            }

            // ——3. 准备输出文件 & 写入 WAV 头占位——
            val outFile = File(outputFilePath).apply {
                if (exists()) delete()
                parentFile?.mkdirs()
            }
            val fos = FileOutputStream(outFile)
            writeWavHeader(fos, channelCount, sampleRate, 16, 0)

            // ——4. 解码音频数据到 PCM 并写入——
            val bufferInfo = MediaCodec.BufferInfo()
            var sawEOS = false
            var totalPcmBytes = 0L

            while (!sawEOS) {
                // 输入端：feed extractor 数据
                val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inIdx >= 0) {
                    val inputBuf = codec.getInputBuffer(inIdx)!!
                    val sampleSize = extractor.readSampleData(inputBuf, 0)
                    if (sampleSize < 0) {
                        // 数据读尽，标记 EOS
                        codec.queueInputBuffer(
                            inIdx, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                    } else {
                        val pts = extractor.sampleTime
                        codec.queueInputBuffer(inIdx, 0, sampleSize, pts, 0)
                        extractor.advance()
                    }
                }

                // 输出端：获取 PCM 数据
                val outIdx = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                when {
                    outIdx >= 0 -> {
                        val outputBuf = codec.getOutputBuffer(outIdx)!!
                        val chunk = ByteArray(bufferInfo.size)
                        outputBuf.get(chunk)
                        fos.write(chunk)
                        totalPcmBytes += chunk.size
                        codec.releaseOutputBuffer(outIdx, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            sawEOS = true
                        }
                    }

                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        // 这里可获取最终格式：codec.outputFormat
                    }
                }
            }

            // ——5. 清理资源 & 更新 WAV 头实际长度——
            codec.stop(); codec.release()
            extractor.release()
            fos.close()
            updateWavHeader(File(outputFilePath), totalPcmBytes.toInt())

            // 成功，回调并返回文件流
            callback.onSuccess(
                ProcessedAudio(outputFilePath, FileInputStream(outFile))
            )
        } catch (e: AudioProcessException) {
            callback.onFailure(e)
        } catch (e: IOException) {
            callback.onFailure(
                AudioProcessException(
                    AudioProcessError.IO_ERROR,
                    "I/O error: ${e.message}"
                )
            )
        } catch (e: Exception) {
            callback.onFailure(
                AudioProcessException(
                    AudioProcessError.DECODE_ERROR,
                    "Decoding error: ${e.message}"
                )
            )
        }
    }

    /** 写入一个占位 WAV 头，稍后用 updateWavHeader() 填充长度 */
    private fun writeWavHeader(
        out: FileOutputStream,
        channels: Int,
        sampleRate: Int,
        bitsPerSample: Int,
        pcmDataSize: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44).apply {
            // RIFF chunk descriptor
            putString(0, "RIFF")
            putLEInt(4, 36 + pcmDataSize)       // file size - 8
            putString(8, "WAVE")
            // fmt sub-chunk
            putString(12, "fmt ")
            putLEInt(16, 16)                    // PCM header size
            putLEShort(20, 1)                   // Audio format = PCM
            putLEShort(22, channels.toShort())
            putLEInt(24, sampleRate)
            putLEInt(28, byteRate)
            putLEShort(32, (channels * bitsPerSample / 8).toShort()) // block align
            putLEShort(34, bitsPerSample.toShort())
            // data sub-chunk
            putString(36, "data")
            putLEInt(40, pcmDataSize)
        }
        out.write(header)
    }

    /** 更新 WAV 头中的数据大小字段（offset 4 和 40） */
    private fun updateWavHeader(file: File, pcmDataSize: Int) {
        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(4)
            raf.writeIntLE(36 + pcmDataSize)
            raf.seek(40)
            raf.writeIntLE(pcmDataSize)
        }
    }

// ———— ByteArray & RandomAccessFile 辅助扩展 ————

    private fun ByteArray.putString(offset: Int, s: String) {
        s.toByteArray(Charsets.US_ASCII).copyInto(this, offset)
    }

    private fun ByteArray.putLEInt(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
        this[offset + 2] = ((value shr 16) and 0xFF).toByte()
        this[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun ByteArray.putLEShort(offset: Int, value: Short) {
        this[offset] = (value.toInt() and 0xFF).toByte()
        this[offset + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
    }

    private fun RandomAccessFile.writeIntLE(value: Int) {
        writeByte(value and 0xFF)
        writeByte((value shr 8) and 0xFF)
        writeByte((value shr 16) and 0xFF)
        writeByte((value shr 24) and 0xFF)
    }
}

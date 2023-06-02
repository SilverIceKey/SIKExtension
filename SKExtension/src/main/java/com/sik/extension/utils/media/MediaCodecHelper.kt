package com.sk.skextension.utils.media

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.sik.extension.createNewFile
import com.sik.extension.fileInputStream
import com.sik.extension.fileOutputStream
import com.sk.skextension.utils.file.FileCreateException
import java.io.File
import java.io.FileNotFoundException
import kotlin.concurrent.thread

/**
 * 编码帮助类
 */
class MediaCodecHelper {

    companion object {
        val INSTANCE: MediaCodecHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaCodecHelper()
        }
    }

    ////////////////////音频转换相关//////////////////////
    /**
     * 转换格式
     * @see MediaCodecFormat.Audio
     */
    private var audioFormatType: String = ""

    /**
     * 音频码率，默认44100
     */
    private var sampleRate: Int = 44100

    /**
     * 声道，默认双声道
     */
    private var channelCount: Int = 2

    /**
     * 编码格式
     */
    private var audioFormat: MediaFormat? = null

    /**
     * 编码器
     */
    private var encodeCodec: MediaCodec? = null

    /**
     * 设置音频编码格式
     */
    fun setAudioFormatType(audioFormatType: String): MediaCodecHelper {
        this.audioFormatType = audioFormatType
        return this
    }

    /**
     * 设置音频码率
     */
    fun setSampleRate(sampleRate: Int): MediaCodecHelper {
        this.sampleRate = sampleRate
        return this
    }

    /**
     * 设置声道
     */
    fun setChannelCount(channelCount: Int): MediaCodecHelper {
        this.channelCount = channelCount
        return this
    }

    /**
     * 开始音频格式转换
     */
    fun startAudioConvert(sourceFile: String, targetFile: String) {
        if (audioFormatType.isEmpty()) {
            throw NullPointerException("音频格式类型为空，请先设置音频格式")
        }
        if (sourceFile.isEmpty() || !File(sourceFile).exists()) {
            throw FileNotFoundException("pcm音频文件不存在或路径为空")
        }
        if (!targetFile.createNewFile()) {
            throw FileCreateException("目标文件创建失败，请检查权限或文件路径")
        }
        build()
        runConvert(sourceFile, targetFile)
    }

    private fun build() {
        audioFormat = MediaFormat.createAudioFormat(audioFormatType, sampleRate, channelCount)
        audioFormat?.let {
            //设置AAC类型
            it.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            //设置比特率
            it.setInteger(MediaFormat.KEY_BIT_RATE, AudioFormat.ENCODING_PCM_16BIT)
            //设置转换缓冲区
            it.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 8 * 8)
        }
        //构建编码器
        encodeCodec = MediaCodec.createEncoderByType(audioFormatType)
        encodeCodec?.let {
            //渲染配置
            it.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }
    }

    private fun runConvert(sourceFile: String, targetFile: String) {
        thread {
            val encodeBufferInfo = MediaCodec.BufferInfo()
            val fis = sourceFile.fileInputStream()
            val fos = targetFile.fileOutputStream()
            encodeCodec?.start()
            encodeCodec?.setCallback(object:MediaCodec.Callback(){
                /**
                 * 转码完成
                 */
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {

                }

                /**
                 * 完成转码
                 */
                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    index: Int,
                    info: MediaCodec.BufferInfo
                ) {

                }

                /**
                 * 转码报错
                 */
                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                }

                /**
                 * 输出格式变化
                 */
                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

                }

            })
            //pcm数据缓冲大小
            val pcmData = ByteArray(1024 * 8 * 8)
            while (true) {
                val inputBuffers = encodeCodec?.inputBuffers
                val inputIndex = encodeCodec?.dequeueInputBuffer(0)
                if (inputIndex != -1) {
                    val inputBuffer = inputIndex?.let { inputBuffers?.get(it) }
                    val size = fis?.read(pcmData)
                    size?.let {
                        if (it < 0) {
                            encodeCodec?.queueInputBuffer(inputIndex!!,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM)

                        } else {
                            inputBuffer?.limit(it)
                        }
                    }
                }
            }
        }
    }

    ////////////////////视频转换相关//////////////////////
    /**
     * 格式转换
     * @see MediaCodecFormat.Video
     */
    var videoFormat: String = ""

    /**
     * 开始视频格式转换
     */
    fun startVideoConvert() {

    }
}
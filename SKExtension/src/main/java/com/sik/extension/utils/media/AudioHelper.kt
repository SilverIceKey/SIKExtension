package com.sk.skextension.utils.media

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.blankj.utilcode.util.FileUtils
import com.sik.extension.fileOutputStream
import com.sik.sikcore.SKExtension
import com.sk.skextension.utils.date.TimeUtil
import java.io.File
import java.io.OutputStream
import kotlin.concurrent.thread

/**
 * 音频帮助文件
 */
class AudioHelper {
    /**
     * 保存路径
     */
    var savePath = ""
    companion object {
        val INSTANCE: AudioHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioHelper()
        }
    }

    init {
        savePath = com.sik.sikcore.SKExtension.getApplication().externalCacheDir?.absolutePath?:"/sdcard/Audio/"
    }

    ////////////////////////////////使用MediaRecord//////////////////////////////////////////////
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    /**
     * 开始录音
     */
    fun startRecord(onSuccess: (filePath: String) -> Unit = {}) {
        if (isRecording) {
            return
        }
        var filePath = savePath
        if (!filePath?.endsWith(File.separator)!!) {
            filePath += File.separator
        }
        val fileName = "${TimeUtil.instance.nowString("yyyy-MM-dd-HH-mm-ss-SSS")}.mp3"
        val file = filePath + fileName
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(filePath + fileName)
        if (!FileUtils.isFileExists(filePath)) {
            File(filePath).mkdirs()
        }
        if (!FileUtils.isFileExists(file)) {
            FileUtils.createOrExistsFile(file)
        }
        onSuccess(filePath + fileName)
        mediaRecorder?.prepare()
        mediaRecorder?.start()
        isRecording = true
    }

    /**
     * 暂停录音
     */
    fun pauseRecord(onPaused: () -> Unit = {}) {
        if (mediaRecorder == null) {
            return
        }
        mediaRecorder?.pause()
        onPaused()
    }

    /**
     * 恢复录音
     */
    fun resumeRecord(onResumed: () -> Unit = {}) {
        if (mediaRecorder == null) {
            return
        }
        mediaRecorder?.resume()
        onResumed()
    }

    /**
     * 停止录音
     */
    fun stopRecord(onStoped: () -> Unit = {}) {
        if (mediaRecorder == null) {
            return
        }
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        onStoped()
    }
    ////////////////////////////////使用MediaRecord//////////////////////////////////////////////

    ////////////////////////////////使用AudioRecord//////////////////////////////////////////////
    private var audioRecord: AudioRecord? = null
    private var audioSize: Int? = null
    private var audioRecordHandler: Handler? = null
    private var fileOutputStream: OutputStream? = null
    private var fileSavePath: String = ""
    private val AUDIORECORD_START = 1
    private val AUDIORECORD_PAUSE = 2
    private val AUDIORECORD_RESUME = 3
    private val AUDIORECORD_STOP = 4

    /**
     * 初始化Handler
     */
    fun initHandler() {
        thread {
            Looper.prepare()
            audioRecordHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                @SuppressLint("HandlerLeak")
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    when (msg.what) {
                        AUDIORECORD_START -> {
                            audioRecord?.startRecording()
                            fileOutputStream = fileSavePath.fileOutputStream()
                            while (isRecording) {
                                var data = ByteArray(audioSize!!)
                                audioRecord?.read(data, 0, audioSize!!)
                                fileOutputStream?.write(data)
                            }
                        }
                        AUDIORECORD_PAUSE -> {

                        }
                        AUDIORECORD_RESUME -> {
                            while (isRecording) {
                                var data = ByteArray(audioSize!!)
                                audioRecord?.read(data, 0, audioSize!!)
                                fileOutputStream?.write(data)
                            }
                        }
                        AUDIORECORD_STOP -> {
                            fileOutputStream?.close()
                            audioRecord?.stop()
                            audioRecord?.release()
                            audioRecord = null
                            fileOutputStream = null
                        }
                    }
                }
            }
            Looper.loop()
        }

    }

    /**
     * 开始录音
     */
    @SuppressLint("MissingPermission")
    fun startRecordWithAudioRecord(onSuccess: (filePath: String) -> Unit = {}) {
        if (isRecording) {
            return
        }
        initHandler()
        var filePath = savePath
        if (!filePath?.endsWith(File.separator)!!) {
            filePath += File.separator
        }
        val fileName = "${TimeUtil.instance.nowString("yyyy-MM-dd-HH-mm-ss-SSS")}.pcm"
        fileSavePath = filePath + fileName
        audioSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, 44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioSize!!
        )
        if (!FileUtils.isFileExists(filePath)) {
            File(filePath).mkdirs()
        }
        if (!FileUtils.isFileExists(fileSavePath)) {
            FileUtils.createOrExistsFile(fileSavePath)
        }
        onSuccess(filePath + fileName)
        val message = Message()
        message.what = AUDIORECORD_START
        audioRecordHandler?.sendMessage(message)
        isRecording = true
    }

    /**
     * 暂停录音
     */
    fun pauseRecordWithAudioRecord(onPaused: () -> Unit = {}) {
        if (audioRecord == null) {
            return
        }
        isRecording = false
        val message = Message()
        message.what = AUDIORECORD_PAUSE
        audioRecordHandler?.sendMessage(message)
        onPaused()
    }

    /**
     * 恢复录音
     */
    fun resumeRecordWithAudioRecord(onResumed: () -> Unit = {}) {
        if (audioRecord == null) {
            return
        }
        isRecording = true
        val message = Message()
        message.what = AUDIORECORD_RESUME
        audioRecordHandler?.sendMessage(message)
        onResumed()
    }

    /**
     * 停止录音
     */
    fun stopRecordWithAudioRecord(onStoped: () -> Unit = {}) {
        if (audioRecord == null) {
            return
        }
        val message = Message()
        message.what = AUDIORECORD_STOP
        audioRecordHandler?.sendMessage(message)
        isRecording = false
        audioRecordHandler = null
        onStoped()
    }
    ////////////////////////////////使用AudioRecord//////////////////////////////////////////////
}
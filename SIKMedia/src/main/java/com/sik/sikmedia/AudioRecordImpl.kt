package com.sik.sikmedia

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import com.sik.sikcore.date.TimeUtils
import java.io.File
import java.io.FileOutputStream

/**
 * 使用 AudioRecord 实现的音频录制类
 */
class AudioRecordImpl : AudioRecorderInterface {
    private var audioRecord: AudioRecord? = null
    private var bufferSize: Int = 0
    private var fileOutputStream: FileOutputStream? = null
    private var savePath: String = ""
    override var isRecording: Boolean = false

    private val handlerThread = HandlerThread("AudioRecordThread")
    private lateinit var audioRecordHandler: Handler

    private var chunkDuration: Long = 0L
    private var chunkListener: ChunkListener? = null
    private var chunkStartTime: Long = 0L
    private var chunkIndex: Int = 0

    override fun setSavePath(path: String) {
        savePath = path
    }

    override fun setChunkDuration(durationMs: Long) {
        chunkDuration = durationMs
    }

    override fun setChunkListener(listener: ChunkListener?) {
        chunkListener = listener
    }

    /**
     * 初始化处理器
     */
    private fun initHandler() {
        handlerThread.start()
        audioRecordHandler = Handler(handlerThread.looper)
    }

    @SuppressLint("MissingPermission")
    override fun startRecord(onSuccess: (filePath: String) -> Unit) {
        if (isRecording) return

        bufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        initHandler()
        isRecording = true
        audioRecordHandler.post { recordAudio() }
        onSuccess(getCurrentFilePath())
    }

    /**
     * 录制音频
     */
    private fun recordAudio() {
        audioRecord?.startRecording()
        startNewChunk()
        val data = ByteArray(bufferSize)
        while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val read = audioRecord?.read(data, 0, bufferSize) ?: 0
            if (read > 0) {
                fileOutputStream?.write(data, 0, read)
                checkChunkDuration()
            }
        }
        closeCurrentChunk()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        handlerThread.quitSafely()
    }

    /**
     * 开始新的音频块
     */
    private fun startNewChunk() {
        closeCurrentChunk()

        val file = prepareFile()
        fileOutputStream = FileOutputStream(file)
        chunkStartTime = System.currentTimeMillis()
    }

    /**
     * 关闭当前音频块
     */
    private fun closeCurrentChunk() {
        fileOutputStream?.close()
        fileOutputStream = null
        if (chunkIndex > 0) {
            chunkListener?.onChunkSaved(getChunkFilePath(chunkIndex - 1))
        }
        chunkIndex++
    }

    /**
     * 检查是否需要开始新的音频块
     */
    private fun checkChunkDuration() {
        if (chunkDuration > 0L && System.currentTimeMillis() - chunkStartTime >= chunkDuration) {
            startNewChunk()
        }
    }

    /**
     * 获取当前音频文件的路径
     */
    private fun getCurrentFilePath(): String {
        return getChunkFilePath(chunkIndex)
    }

    /**
     * 获取指定索引的音频块文件路径
     */
    private fun getChunkFilePath(index: Int): String {
        val filePath = if (savePath.endsWith(File.separator)) savePath else "$savePath${File.separator}"
        val fileName = "${TimeUtils.instance.nowString("yyyy-MM-dd-HH-mm-ss-SSS")}_chunk_$index.pcm"
        return filePath + fileName
    }

    /**
     * 准备文件
     */
    private fun prepareFile(): File {
        val filePath = getCurrentFilePath()
        val file = File(filePath)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        return file
    }

    override fun pauseRecord(onPaused: () -> Unit) {
        // AudioRecord 不支持暂停，需自行实现逻辑
        onPaused()
    }

    override fun resumeRecord(onResumed: () -> Unit) {
        // AudioRecord 不支持恢复，需自行实现逻辑
        onResumed()
    }

    override fun stopRecord(onStopped: () -> Unit) {
        if (!isRecording) return
        isRecording = false
        onStopped()
    }
}

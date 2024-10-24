package com.sik.sikmedia

import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.sik.sikcore.date.TimeUtils
import java.io.File

/**
 * 使用 MediaRecorder 实现的音频录制类
 */
class MediaRecorderImpl : AudioRecorderInterface {
    private var mediaRecorder: MediaRecorder? = null
    override var isRecording: Boolean = false
    private var savePath: String = ""
    private var chunkDuration: Long = 0L
    private var chunkListener: ChunkListener? = null
    private var handler: Handler? = null
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

    override fun startRecord(onSuccess: (filePath: String) -> Unit) {
        if (isRecording) return

        if (chunkDuration > 0L) {
            // 如果设置了分块时长，启动分块录制
            handler = Handler(Looper.getMainLooper())
            chunkIndex = 0
            startNewChunk()
            scheduleNextChunk()
        } else {
            // 否则，开始普通录制
            startNewRecording()
        }

        isRecording = true
        onSuccess(getCurrentFilePath())
    }

    /**
     * 开始新的录音
     */
    private fun startNewRecording() {
        val file = prepareFile()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
    }

    /**
     * 开始新的音频块
     */
    private fun startNewChunk() {
        stopCurrentRecording()

        val file = prepareFile()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
    }

    /**
     * 停止当前录音
     */
    private fun stopCurrentRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                // 处理停止录音时的异常
            }
            release()
        }
        mediaRecorder = null

        // 通知音频块已保存
        if (chunkIndex > 0) {
            chunkListener?.onChunkSaved(getChunkFilePath(chunkIndex - 1))
        }
        chunkIndex++
    }

    /**
     * 安排下一个音频块的录制
     */
    private fun scheduleNextChunk() {
        handler?.postDelayed({
            if (isRecording) {
                startNewChunk()
                scheduleNextChunk()
            }
        }, chunkDuration)
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
        val fileName = "${TimeUtils.nowString("yyyy-MM-dd-HH-mm-ss-SSS")}_chunk_$index.mp3"
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
        if (mediaRecorder == null || !isRecording) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        }
        onPaused()
    }

    override fun resumeRecord(onResumed: () -> Unit) {
        if (mediaRecorder == null || !isRecording) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        }
        onResumed()
    }

    override fun stopRecord(onStopped: () -> Unit) {
        if (!isRecording) return
        isRecording = false
        handler?.removeCallbacksAndMessages(null)
        stopCurrentRecording()
        mediaRecorder = null
        onStopped()
    }
}

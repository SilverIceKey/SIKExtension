package com.sik.sikmedia

/**
 * 音频帮助类，提供录音功能的统一接口
 */
class AudioHelper private constructor() {
    companion object {
        @JvmStatic
        val instance: AudioHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioHelper()
        }
    }

    private var audioRecorder: AudioRecorderInterface? = null

    /**
     * 设置录音器类型，并初始化对应的实现
     * @param type 音频录制类型
     */
    fun setRecorderType(type: AudioRecorderType) {
        audioRecorder = when (type) {
            AudioRecorderType.MEDIA_RECORDER -> MediaRecorderImpl()
            AudioRecorderType.AUDIO_RECORD -> AudioRecordImpl()
        }
    }

    /**
     * 设置保存路径
     * @param path 音频文件保存路径
     */
    fun setSavePath(path: String) {
        audioRecorder?.setSavePath(path)
    }

    /**
     * 设置分块时长
     * @param durationMs 每个音频块的时长，单位毫秒
     */
    fun setChunkDuration(durationMs: Long) {
        audioRecorder?.setChunkDuration(durationMs)
    }

    /**
     * 设置分块监听器
     * @param listener 分块完成后的回调监听器
     */
    fun setChunkListener(listener: ChunkListener?) {
        audioRecorder?.setChunkListener(listener)
    }

    /**
     * 开始录音
     * @param onSuccess 录音开始成功后的回调，返回文件路径
     */
    fun startRecord(onSuccess: (filePath: String) -> Unit = {}) {
        audioRecorder?.startRecord(onSuccess)
    }

    /**
     * 暂停录音
     * @param onPaused 录音暂停后的回调
     */
    fun pauseRecord(onPaused: () -> Unit = {}) {
        audioRecorder?.pauseRecord(onPaused)
    }

    /**
     * 恢复录音
     * @param onResumed 录音恢复后的回调
     */
    fun resumeRecord(onResumed: () -> Unit = {}) {
        audioRecorder?.resumeRecord(onResumed)
    }

    /**
     * 停止录音
     * @param onStopped 录音停止后的回调
     */
    fun stopRecord(onStopped: () -> Unit = {}) {
        audioRecorder?.stopRecord(onStopped)
    }
}

package com.sik.sikmedia
/**
 * 音频录制接口，定义录音功能的方法
 */
interface AudioRecorderInterface {
    /**
     * 是否正在录音
     */
    var isRecording: Boolean

    /**
     * 设置保存路径
     * @param path 保存音频文件的路径
     */
    fun setSavePath(path: String)

    /**
     * 设置分块时长（毫秒）
     * @param durationMs 每个音频块的时长，单位毫秒
     */
    fun setChunkDuration(durationMs: Long)

    /**
     * 设置分块监听器
     * @param listener 分块完成后的回调监听器
     */
    fun setChunkListener(listener: ChunkListener?)

    /**
     * 开始录音
     * @param onSuccess 录音开始成功后的回调，返回文件路径
     */
    fun startRecord(onSuccess: (filePath: String) -> Unit = {})

    /**
     * 暂停录音
     * @param onPaused 录音暂停后的回调
     */
    fun pauseRecord(onPaused: () -> Unit = {})

    /**
     * 恢复录音
     * @param onResumed 录音恢复后的回调
     */
    fun resumeRecord(onResumed: () -> Unit = {})

    /**
     * 停止录音
     * @param onStopped 录音停止后的回调
     */
    fun stopRecord(onStopped: () -> Unit = {})
}

/**
 * 分块监听器接口
 */
interface ChunkListener {
    /**
     * 当音频块保存完成后回调
     * @param filePath 保存的音频块文件路径
     */
    fun onChunkSaved(filePath: String)
}

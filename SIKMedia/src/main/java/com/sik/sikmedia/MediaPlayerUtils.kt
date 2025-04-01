package com.sik.sikmedia

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.sik.sikcore.SIKCore
import com.sik.sikcore.thread.ThreadUtils
import java.io.IOException

/**
 * 媒体播放工具类，支持MediaPlayer和ExoPlayer，自动根据文件格式选择播放器
 */
object MediaPlayerUtils {
    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private var queue: MutableList<Any> = mutableListOf()
    private var currentIndex: Int = 0
    private var totalDuration: Long = 0
    private var durations: MutableList<Long> = mutableListOf()
    private var isPreparing: Boolean = false

    private var playbackListener: PlaybackListener? = null

    /**
     * 播放音频
     */
    fun playAudio(dataSource: Any) {
        when (dataSource) {
            is String, is Int, is Uri -> queue.add(dataSource)
            is List<*> -> queue.addAll(listOf(dataSource.filter { it is String || it is Int || it is Uri }))
            else -> return
        }

        if (mediaPlayer == null && exoPlayer == null) {
            currentIndex = 0
            totalDuration = 0
            durations.clear()
            prepareDurations {
                playNext()
            }
        }
    }

    /**
     * 准备每个音频的持续时间
     */
    private fun prepareDurations(onComplete: () -> Unit) {
        if (currentIndex >= queue.size) {
            currentIndex = 0
            onComplete()
            return
        }

        val dataSource = queue[currentIndex]
        val tempPlayer = MediaPlayer()

        try {
            when (dataSource) {
                is String -> tempPlayer.setDataSource(
                    SIKCore.getApplication(),
                    Uri.parse(dataSource)
                )

                is Uri -> tempPlayer.setDataSource(SIKCore.getApplication(), dataSource)
                is Int -> {
                    val afd = SIKCore.getApplication().resources.openRawResourceFd(dataSource)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tempPlayer.setDataSource(afd)
                    } else {
                        tempPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                }
            }

            tempPlayer.setOnPreparedListener {
                val duration = it.duration.toLong()
                durations.add(duration)
                totalDuration += duration
                it.release()
                currentIndex++
                prepareDurations(onComplete)
            }
            tempPlayer.setOnErrorListener { mp, what, extra ->
                mp.release()
                durations.add(0)
                currentIndex++
                prepareDurations(onComplete)
                true
            }
            tempPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
            tempPlayer.release()
            durations.add(0)
            currentIndex++
            prepareDurations(onComplete)
        }
    }

    /**
     * 播放下一个音频
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun playNext() {
        if (currentIndex >= queue.size) {
            releaseMediaPlayer()
            releaseExoPlayer()
            playbackListener?.onPlaybackComplete()
            return
        }

        val dataSource = queue[currentIndex]

        when (dataSource) {
            is String -> if (dataSource.endsWith(".m3u8")) {
                setupExoPlayer(dataSource)
            } else {
                setupMediaPlayer(dataSource)
            }

            is Int, is Uri -> setupMediaPlayer(dataSource)
            else -> return
        }
    }

    /**
     * 设置媒体播放器
     */
    private fun setupMediaPlayer(dataSource: Any) {
        isPreparing = true
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }

        try {
            when (dataSource) {
                is String -> mediaPlayer?.setDataSource(
                    SIKCore.getApplication(),
                    Uri.parse(dataSource)
                )

                is Uri -> mediaPlayer?.setDataSource(SIKCore.getApplication(), dataSource)
                is Int -> {
                    val afd = SIKCore.getApplication().resources.openRawResourceFd(dataSource)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mediaPlayer?.setDataSource(afd)
                    } else {
                        mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    }
                }
            }

            mediaPlayer?.apply {
                setOnPreparedListener {
                    isPreparing = false
                    it.start()
                    playbackListener?.onTrackChanged(currentIndex, durations[currentIndex])
                    updateProgress()
                }
                setOnCompletionListener {
                    currentIndex++
                    playNext()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.reset()
                    currentIndex++
                    playNext()
                    true
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer?.reset()
            currentIndex++
            playNext()
        }
    }

    /**
     * 设置媒体源
     */
    @UnstableApi
    private fun setupExoPlayer(dataSource: String) {
        isPreparing = true
        releaseExoPlayer()
        exoPlayer = ExoPlayer.Builder(SIKCore.getApplication()).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(dataSource))
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource: MediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        isPreparing = false
                        playbackListener?.onTrackChanged(currentIndex, durations[currentIndex])
                        updateProgress()
                    } else if (state == Player.STATE_ENDED) {
                        currentIndex++
                        playNext()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    currentIndex++
                    playNext()
                }
            })
        }
    }

    /**
     * 跳转到指定位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        var cumulativeDuration = 0L
        var targetIndex = -1
        var seekPositionInTrack = 0L

        for (i in durations.indices) {
            val duration = durations[i]
            if (positionMs < cumulativeDuration + duration) {
                targetIndex = i
                seekPositionInTrack = positionMs - cumulativeDuration
                break
            }
            cumulativeDuration += duration
        }

        if (targetIndex == -1) {
            // 超出总时长，停止播放
            stopAudio()
            return
        }

        if (targetIndex != currentIndex) {
            currentIndex = targetIndex
            if (mediaPlayer?.isPlaying == true || exoPlayer?.isPlaying == true || isPreparing) {
                mediaPlayer?.stop()
                exoPlayer?.stop()
            }
            playNext()
        }

        if (mediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo(seekPositionInTrack, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(seekPositionInTrack.toInt())
            }
        } else if (exoPlayer != null) {
            exoPlayer?.seekTo(seekPositionInTrack)
        }
    }

    /**
     * 获取当前播放位置（毫秒）
     */
    fun getCurrentPosition(): Long {
        var currentPosition = 0L
        for (i in 0 until currentIndex) {
            currentPosition += durations.getOrNull(i) ?: 0L
        }
        currentPosition += when {
            mediaPlayer != null && mediaPlayer?.isPlaying == true -> mediaPlayer?.currentPosition?.toLong()
                ?: 0L

            exoPlayer != null && exoPlayer?.isPlaying == true -> exoPlayer?.currentPosition ?: 0L
            else -> 0L
        }
        return currentPosition
    }

    /**
     * 获取总时长（毫秒）
     */
    fun getTotalDuration(): Long {
        return totalDuration
    }

    /**
     * 暂停播放
     */
    fun pauseAudio() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        exoPlayer?.playWhenReady = false
    }

    /**
     * 恢复播放
     */
    fun resumeAudio() {
        mediaPlayer?.start()
        exoPlayer?.playWhenReady = true
    }

    /**
     * 停止播放
     */
    fun stopAudio() {
        releaseMediaPlayer()
        releaseExoPlayer()
        queue.clear()
        durations.clear()
        currentIndex = 0
        totalDuration = 0
    }

    /**
     * 释放MediaPlayer
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * 释放ExoPlayer
     */
    private fun releaseExoPlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    /**
     * 设置播放监听器
     */
    fun setPlaybackListener(listener: PlaybackListener) {
        playbackListener = listener
    }

    /**
     * 更新播放进度
     */
    private fun updateProgress() {
        playbackListener?.onProgress(getCurrentPosition(), totalDuration)
        if ((mediaPlayer?.isPlaying == true || exoPlayer?.isPlaying == true) && !isPreparing) {
            ThreadUtils.runOnMainDelayed(1000) {
                updateProgress()
            }
        }
    }

    interface PlaybackListener {
        fun onProgress(currentPosition: Long, totalDuration: Long)
        fun onTrackChanged(index: Int, duration: Long)
        fun onPlaybackComplete()
    }
}

package com.sik.sikmedia

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.sik.sikcore.SIKCore
import java.io.IOException

/**
 * 媒体播放工具类，支持MediaPlayer和ExoPlayer，自动根据文件格式选择播放器
 */
object MediaPlayerUtils {
    private var mediaPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private var queue: MutableList<Any> = mutableListOf()

    /**
     * 播放音频
     */
    fun playAudio(dataSource: Any) {
        when (dataSource) {
            is String, is Int, is Uri -> queue.add(dataSource)
            is List<*> -> queue.addAll(listOf(dataSource.filter { it is String || it is Int || it is Uri }))
            else -> return
        }

        if (queue.isNotEmpty()) {
            playNext()
        }
    }

    private fun playNext() {
        val dataSource = queue.removeAt(0)
        when (dataSource) {
            is String -> if (dataSource.endsWith(".m3u8")) {
                setupExoPlayer(dataSource)
            } else {
                setupMediaPlayer(dataSource)
            }

            is Int -> setupMediaPlayerFromRaw(dataSource)
            is Uri -> setupMediaPlayer(dataSource)
        }
    }

    private fun setupMediaPlayer(dataSource: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            nextPlayer = MediaPlayer()
        }

        val player = nextPlayer ?: mediaPlayer!!
        player.apply {
            reset()
            setDataSource(SIKCore.getApplication(), Uri.parse(dataSource))
            setOnPreparedListener {
                it.start()
            }
            prepareAsync()
            setOnCompletionListener {
                player.reset()
                if (queue.isNotEmpty()) {
                    playNext()
                } else {
                    releaseMediaPlayer()
                }
            }
            setOnErrorListener { _, _, _ ->
                player.reset()
                true
            }
        }
        nextPlayer = mediaPlayer
        mediaPlayer = player
    }

    private fun setupMediaPlayer(dataSource: Uri) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            nextPlayer = MediaPlayer()
        }

        val player = nextPlayer ?: mediaPlayer!!
        player.apply {
            reset()
            setDataSource(SIKCore.getApplication(), dataSource)
            setOnPreparedListener {
                it.start()
            }
            prepareAsync()
            setOnCompletionListener {
                player.reset()
                if (queue.isNotEmpty()) {
                    playNext()
                } else {
                    releaseMediaPlayer()
                }
            }
            setOnErrorListener { _, _, _ ->
                player.reset()
                true
            }
        }
        nextPlayer = mediaPlayer
        mediaPlayer = player
    }

    private fun setupMediaPlayerFromRaw(rawResId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            nextPlayer = MediaPlayer()
        }

        val player = nextPlayer ?: mediaPlayer!!
        val afd = SIKCore.getApplication().resources.openRawResourceFd(rawResId)
        player.apply {
            reset()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setDataSource(afd)
            } else {
                try {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            setOnPreparedListener {
                it.start()
            }
            prepareAsync()
            setOnCompletionListener {
                player.reset()
                if (queue.isNotEmpty()) {
                    playNext()
                } else {
                    releaseMediaPlayer()
                }
            }
            setOnErrorListener { _, _, _ ->
                player.reset()
                true
            }
        }
        nextPlayer = mediaPlayer
        mediaPlayer = player
    }

    @OptIn(UnstableApi::class)
    private fun setupExoPlayer(dataSource: String) {
        releaseExoPlayer()  // 释放之前的ExoPlayer资源
        exoPlayer = ExoPlayer.Builder(SIKCore.getApplication()).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(dataSource))
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource: MediaSource =
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlayerError(e: PlaybackException) {
                    super.onPlayerError(e)
                    playNext() // 尝试播放下一个在队列中的文件
                }
            })
        }
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
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        nextPlayer?.release()
        mediaPlayer = null
        nextPlayer = null
    }

    private fun releaseExoPlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}

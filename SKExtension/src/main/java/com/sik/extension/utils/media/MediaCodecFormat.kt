package com.sk.skextension.utils.media

/**
 * 编码格式
 */
object MediaCodecFormat {
    /**
     * 音频
     */
    object Audio{
        /////////////////////////////音频////////////////////////////
        const val MIMETYPE_AUDIO_AMR_NB: String = "audio/3gpp"
        const val MIMETYPE_AUDIO_AMR_WB = "audio/amr-wb"
        const val MIMETYPE_AUDIO_MPEG = "audio/mpeg"
        const val MIMETYPE_AUDIO_AAC = "audio/mp4a-latm"
        const val MIMETYPE_AUDIO_QCELP = "audio/qcelp"
        const val MIMETYPE_AUDIO_VORBIS = "audio/vorbis"
        const val MIMETYPE_AUDIO_OPUS = "audio/opus"
        const val MIMETYPE_AUDIO_G711_ALAW = "audio/g711-alaw"
        const val MIMETYPE_AUDIO_G711_MLAW = "audio/g711-mlaw"
        const val MIMETYPE_AUDIO_RAW = "audio/raw"
        const val MIMETYPE_AUDIO_FLAC = "audio/flac"
        const val MIMETYPE_AUDIO_MSGSM = "audio/gsm"
        const val MIMETYPE_AUDIO_AC3 = "audio/ac3"
        const val MIMETYPE_AUDIO_EAC3 = "audio/eac3"
        const val MIMETYPE_AUDIO_EAC3_JOC = "audio/eac3-joc"
        const val MIMETYPE_AUDIO_AC4 = "audio/ac4"
        const val MIMETYPE_AUDIO_SCRAMBLED = "audio/scrambled"
    }

    /**
     * 视频
     */
    object Video{
        /////////////////////////////视频////////////////////////////
        const val MIMETYPE_VIDEO_VP8: String = "video/x-vnd.on2.vp8"
        const val MIMETYPE_VIDEO_VP9 = "video/x-vnd.on2.vp9"
        const val MIMETYPE_VIDEO_AV1 = "video/av01"
        const val MIMETYPE_VIDEO_AVC = "video/avc"
        const val MIMETYPE_VIDEO_HEVC = "video/hevc"
        const val MIMETYPE_VIDEO_MPEG4 = "video/mp4v-es"
        const val MIMETYPE_VIDEO_H263 = "video/3gpp"
        const val MIMETYPE_VIDEO_MPEG2 = "video/mpeg2"
        const val MIMETYPE_VIDEO_RAW = "video/raw"
        const val MIMETYPE_VIDEO_DOLBY_VISION = "video/dolby-vision"
        const val MIMETYPE_VIDEO_SCRAMBLED = "video/scrambled"
    }
}
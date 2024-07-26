package com.sik.sikmedia.audio_process

// 定义一个异常类，用于表示音频处理过程中发生的异常
class AudioProcessException(val error: AudioProcessError, message: String) : Exception(message)

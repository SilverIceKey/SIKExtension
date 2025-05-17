package com.sik.sikmedia.audio_process

// 定义一个枚举类，用于表示各种音频处理错误
enum class AudioProcessError {
    INVALID_FILE_FORMAT,  // 文件格式无效
    FILE_NOT_FOUND,       // 文件未找到
    CONVERSION_FAILED,    // 转换失败
    UNKNOWN_ERROR   ,      // 未知错误
    IO_ERROR,
    DECODE_ERROR,
    INVALID_FORMAT
}

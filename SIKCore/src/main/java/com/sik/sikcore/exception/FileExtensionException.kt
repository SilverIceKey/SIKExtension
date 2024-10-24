package com.sik.sikcore.exception

/**
 * File extension exception
 * 文件扩展异常类
 * @constructor Create empty File extension exception
 */
class FileExtensionException(message: String?) : Exception(message) {
    companion object {
        const val FILE_PATH_ERROR: String = "filepath is empty"
        const val FILE_NOT_EXIST_ERROR: String = "file not exists!"
    }
}
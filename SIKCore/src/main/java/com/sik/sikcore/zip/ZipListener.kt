package com.sik.sikcore.zip

import java.io.File

/**
 * 压缩和解压缩监听
 */
interface ZipListener {
    /**
     * 压缩结束
     */
    fun zipEnd(file: File)

    /**
     * 解压缩结束
     */
    fun unzipEnd(fileList: MutableList<File>)

    /**
     * 压缩和解压缩错误
     */
    fun error(errorMsg: String)

    /**
     * 压缩和解压缩进度
     */
    fun progress(progress: Float)
}
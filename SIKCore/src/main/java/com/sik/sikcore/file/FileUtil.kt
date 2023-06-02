package com.sik.sikcore.file

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

/**
 * 文件相关
 */
object FileUtil {
    /**
     * 读取文件转String
     */
    @Throws(IOException::class)
    fun loadFileAsString(filePath: String?): String {
        val fileData = StringBuffer(1000)
        val reader = BufferedReader(FileReader(filePath))
        val buf = CharArray(1024)
        var numRead:Int
        while (reader.read(buf).also { numRead = it } != -1) {
            val readData = String(buf, 0, numRead)
            fileData.append(readData)
        }
        reader.close()
        return fileData.toString()
    }
}
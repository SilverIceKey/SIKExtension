package com.sik.sikcore.file

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.sik.sikcore.SIKCore
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * 文件相关
 */
object FileUtils {
    /**
     * 读取文件转String
     */
    @Throws(IOException::class)
    fun loadFileAsString(filePath: String?): String {
        val fileData = StringBuffer(1000)
        val reader = BufferedReader(FileReader(filePath))
        val buf = CharArray(1024)
        var numRead: Int
        while (reader.read(buf).also { numRead = it } != -1) {
            val readData = String(buf, 0, numRead)
            fileData.append(readData)
        }
        reader.close()
        return fileData.toString()
    }

    /**
     * 文件是否存在
     */
    fun isFileExists(filePath: String): Boolean {
        if (filePath.isEmpty()) {
            return false
        }
        return File(filePath).exists()
    }

    /**
     * 创建的文件如果存在则不创建
     */
    fun createOrExistsFile(file: String) {
        if (file.isEmpty()) {
            return
        }
        val tempFile = File(file)
        if (!tempFile.exists()) {
            tempFile.mkdirs()
            tempFile.createNewFile()
        }
    }
}
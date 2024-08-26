package com.sik.sikcore.file

import android.net.Uri
import android.provider.OpenableColumns
import com.sik.sikcore.SIKCore
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream

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
     * @see com.sik.sikcore.extension.exists
     */
    @Deprecated("重复代码")
    fun isFileExists(filePath: String): Boolean {
        if (filePath.isEmpty()) {
            return false
        }
        return File(filePath).exists()
    }

    /**
     * 创建的文件如果存在则不创建
     * @see com.sik.sikcore.extension.createNewFile
     */
    @Deprecated("重复代码")
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

    /**
     * 从uri中获取文件
     * @param uri
     */
    fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream: InputStream? =
                SIKCore.getApplication().contentResolver.openInputStream(uri)
            inputStream?.let {
                val fileName = getFileName(uri) ?: "temp_audio_file"
                val file = File(SIKCore.getApplication().cacheDir, fileName.replace(" ",""))
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从uri获取文件名
     */
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = SIKCore.getApplication().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME).coerceAtLeast(0))
            }
        }
        return fileName
    }

    /**
     * Format bytes
     * 文件大小单位转换
     * @param bytes
     * @return
     */
    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = ("KMGTPE")[exp - 1] + "B"
        return String.format("%.3f %s", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
package com.sik.sikcore.io

import java.io.*

/**
 * 流处理工具类
 */
object IOUtils {

    /**
     * 将 InputStream 转换为 ByteArray
     */
    @Throws(IOException::class)
    fun toByteArray(inputStream: InputStream): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            copy(inputStream, outputStream)
            return outputStream.toByteArray()
        }
    }

    /**
     * 将 InputStream 转换为字符串
     */
    @Throws(IOException::class)
    fun toString(inputStream: InputStream, charset: String = "UTF-8"): String {
        ByteArrayOutputStream().use { outputStream ->
            copy(inputStream, outputStream)
            return outputStream.toString(charset)
        }
    }

    /**
     * 将字符串写入到 OutputStream
     */
    @Throws(IOException::class)
    fun writeString(outputStream: OutputStream, data: String, charset: String = "UTF-8") {
        outputStream.write(data.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    /**
     * 将 byte 数组写入到 OutputStream
     */
    @Throws(IOException::class)
    fun writeBytes(outputStream: OutputStream, data: ByteArray) {
        outputStream.write(data)
        outputStream.flush()
    }

    /**
     * 复制 InputStream 到 OutputStream
     */
    @Throws(IOException::class)
    fun copy(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
    }

    /**
     * 关闭多个 Closeable 对象（如流、Reader、Writer等）
     */
    fun close(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            try {
                closeable?.close()
            } catch (e: IOException) {
                e.printStackTrace() // 这里可以根据需要记录日志或处理异常
            }
        }
    }

    /**
     * 读取文件内容为字符串
     */
    @Throws(IOException::class)
    fun readFileAsString(file: File, charset: String = "UTF-8"): String {
        FileInputStream(file).use { inputStream ->
            return toString(inputStream, charset)
        }
    }

    /**
     * 写入字符串到文件
     */
    @Throws(IOException::class)
    fun writeStringToFile(file: File, data: String, charset: String = "UTF-8") {
        FileOutputStream(file).use { outputStream ->
            writeString(outputStream, data, charset)
        }
    }

    /**
     * 将 byte 数组写入文件
     */
    @Throws(IOException::class)
    fun writeBytesToFile(file: File, data: ByteArray) {
        FileOutputStream(file).use { outputStream ->
            writeBytes(outputStream, data)
        }
    }

    /**
     * 将文件复制到另一个文件
     */
    @Throws(IOException::class)
    fun copyFile(srcFile: File, destFile: File) {
        FileInputStream(srcFile).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                copy(inputStream, outputStream)
            }
        }
    }
}

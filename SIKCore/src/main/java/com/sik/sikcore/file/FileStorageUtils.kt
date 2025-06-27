package com.sik.sikcore.file

import com.sik.sikcore.SIKCore
import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException

/**
 * 工具类：支持在应用私有目录（internal）和外部 SDCard 目录下切换文件读写、删除及判断功能。
 * internal->[Context.filesDir]/folder
 * external->[Context.getExternalFilesDir(null)]/folder（若外部不存在，则降级为 internal）
 * 均不需额外存储权限。
 */
object FileStorageUtils {

    /** 存储类型枚举 */
    enum class StorageType {
        INTERNAL,  // 应用私有 internal storage
        EXTERNAL   // 外部 SDCard 应用专属目录
    }

    private fun getBaseDir(type: StorageType): File {
        val ctx = SIKCore.getApplication()
        return if (type == StorageType.EXTERNAL) {
            ctx.getExternalFilesDir(null)?.apply { if (!exists()) mkdirs() } ?: ctx.filesDir
        } else {
            ctx.filesDir
        }
    }

    /**
     * 写文本到指定存储类型的 dataDir/[folder]/[filename]
     * @param type 存储类型，默认 INTERNAL
     * @return 成功 true，否则 false
     */
    fun writeText(
        folder: String,
        filename: String,
        content: String,
        append: Boolean = false,
        type: StorageType = StorageType.INTERNAL
    ): Boolean {
        return try {
            val dir = File(getBaseDir(type), folder).apply { if (!exists()) mkdirs() }
            val file = File(dir, filename)
            if (append) file.appendText(content) else file.writeText(content)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读文本
     */
    fun readText(
        folder: String,
        filename: String,
        type: StorageType = StorageType.INTERNAL
    ): String? {
        return try {
            val file = File(getBaseDir(type), "$folder/$filename")
            if (!file.exists()) return null
            file.readText()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 写字节
     */
    fun writeBytes(
        folder: String,
        filename: String,
        bytes: ByteArray,
        append: Boolean = false,
        type: StorageType = StorageType.INTERNAL
    ): Boolean {
        return try {
            val dir = File(getBaseDir(type), folder).apply { if (!exists()) mkdirs() }
            val file = File(dir, filename)
            if (append) file.appendBytes(bytes) else file.writeBytes(bytes)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读字节
     */
    fun readBytes(
        folder: String,
        filename: String,
        type: StorageType = StorageType.INTERNAL
    ): ByteArray? {
        return try {
            val file = File(getBaseDir(type), "$folder/$filename")
            if (!file.exists()) return null
            file.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 判断文件是否存在
     */
    fun exists(
        folder: String,
        filename: String,
        type: StorageType = StorageType.INTERNAL
    ): Boolean = File(getBaseDir(type), "$folder/$filename").exists()

    /**
     * 删除单文件
     */
    fun deleteFile(
        folder: String,
        filename: String,
        type: StorageType = StorageType.INTERNAL
    ): Boolean {
        return try {
            val file = File(getBaseDir(type), "$folder/$filename")
            !file.exists() || file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除整个目录及子文件
     */
    fun deleteFolder(
        folder: String,
        type: StorageType = StorageType.INTERNAL
    ): Boolean {
        return try {
            val dir = File(getBaseDir(type), folder)
            if (!dir.exists()) return true
            dir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

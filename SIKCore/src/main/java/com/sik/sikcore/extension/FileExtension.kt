package com.sik.sikcore.extension

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 获取文件的输出流
 */
fun File.outputStream(): FileOutputStream {
    return FileOutputStream(this, false)
}

/**
 * 文件路径直接返回输出流
 */
fun String.fileOutputStream(): FileOutputStream {
    if (this.isEmpty()) throw FileExtensionException(FileExtensionException.FILE_PATH_ERROR)
    val file = File(this)
    if (!file.exists()) {
        file.parentFile?.mkdirs()
        file.createNewFile()
    }
    return FileOutputStream(file)
}

/**
 * 文件路径直接返回文件
 */
fun String.file(): File {
    if (this.isEmpty()) {
        throw FileExtensionException(FileExtensionException.FILE_PATH_ERROR)
    }
    if (!File(this).exists()) {
        throw FileExtensionException(FileExtensionException.FILE_NOT_EXIST_ERROR)
    }
    return File(this)
}

/**
 * Folder
 * 获取文件夹
 * @return
 */
fun String.folder(): File {
    if (this.isEmpty()) {
        throw FileExtensionException(FileExtensionException.FILE_PATH_ERROR)
    }
    val folder = File(this)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return folder
}

/**
 * 文件路径直接返回输入流
 */
fun String.fileInputStream(): FileInputStream {
    if (this.isEmpty()) {
        throw FileExtensionException(FileExtensionException.FILE_PATH_ERROR)
    }
    if (!File(this).exists()) {
        throw FileExtensionException(FileExtensionException.FILE_NOT_EXIST_ERROR)
    }
    return File(this).inputStream()
}

/**
 * 判断文件是否存在
 */
fun String.exists(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    return File(this).exists()
}

/**
 * 删除文件
 */
fun String.deleteIfExists(): Boolean {
    if (this.isEmpty() || !File(this).exists()) {
        return false
    }
    return File(this).delete()
}

/**
 * 判断文件夹是否存在，不存在则创建
 */
fun String.existsAndCreateFolder() {
    if (this.isNotEmpty()) {
        File(this).mkdirs()
    }
}

/**
 * 文件不存的情况下创建文件,存在的情况下直接返回true
 */
fun String.createNewFile(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    if (this.exists()) {
        return true
    }
    return File(this).createNewFile()
}

/**
 * 文件路径写入数据
 */
fun String.write(data: ByteArray) {
    if (this.createNewFile()) {
        FileOutputStream(File(this)).use { outputStream ->
            outputStream.write(data)
        }
    }
}

/**
 * 文件路径获取文本数据
 * 不支持大文件读取
 */
fun String.getData(): String {
    val fis = this.fileInputStream()
    val data = StringBuilder()
    fis.let { data.append(String(it.readBytes())) }
    fis.close()
    return data.toString()
}
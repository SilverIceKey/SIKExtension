package com.sik.extension

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
fun String.fileOutputStream(): FileOutputStream? {
    if (this.isEmpty()) {
        return null
    }
    if (!File(this).exists()) {
        File(this.substring(0, this.lastIndexOf(File.separator))).mkdirs()
        File(this).createNewFile()
    }
    return File(this).outputStream()
}

/**
 * 文件路径直接返回文件
 */
fun String.file(): File? {
    if (this.isEmpty()) {
        return null
    }
    if (!File(this).exists()) {
        File(this.substring(0, this.lastIndexOf(File.separator))).mkdirs()
        File(this).createNewFile()
    }
    return File(this)
}

/**
 * 文件路径直接返回输入流
 */
fun String.fileInputStream(): FileInputStream? {
    if (this.isEmpty()) {
        return null
    }
    if (!File(this).exists()) {
        File(this.substring(0, this.lastIndexOf(File.separator))).mkdirs()
        File(this).createNewFile()
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
fun String.existsDelete(): Boolean {
    if (this.isEmpty() || !File(this).exists()) {
        return false
    }
    return File(this).delete()
}

/**
 * 判断文件夹是否存在，不存在则创建
 */
fun String.existsAndCreateFolder() {
    if (this.isEmpty()) {
        return
    }
    if (!File(this).exists()) {
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
        this.fileOutputStream()?.let {
            it.write(data)
            it.close()
        }
    }
}

/**
 * 文件路径获取文本数据
 */
fun String.getData(): String {
    val fis = this.fileInputStream()
    var data = ""
    fis?.let { data = String(it.readBytes()) }
    fis?.close()
    return data
}
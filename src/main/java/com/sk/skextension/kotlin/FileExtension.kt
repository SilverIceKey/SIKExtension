package com.sk.skextension.kotlin

import java.io.File
import java.io.FileOutputStream


/**
 * Constructs a new FileOutputStream of this file and returns it as a result.
 */
fun File.emptyOutputStream(): FileOutputStream {
    return FileOutputStream(this,false)
}

/**
 * 文件路径直接返回输出流
 */
fun String.fileOutputStream():FileOutputStream?{
    if (this.isEmpty()){
        return null
    }
    if (File(this).exists()){
        File(this.substring(0,this.lastIndexOf(File.separator))).mkdirs()
        File(this).createNewFile()
    }
    return File(this).outputStream()
}

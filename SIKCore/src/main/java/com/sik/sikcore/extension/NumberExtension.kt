package com.sik.sikcore.extension

/**
 * Format bytes
 * 文件大小单位转换
 * @param bytes
 * @return
 */
fun Long.formatBytes(): String {
    val unit = 1024
    if (this < unit) return "$this B"
    val exp = (Math.log(this.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = ("KMGTPE")[exp - 1] + "B"
    return String.format("%.3f %s", this / Math.pow(unit.toDouble(), exp.toDouble()), pre)
}
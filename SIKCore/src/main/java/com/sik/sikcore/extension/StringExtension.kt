package com.sik.sikcore.extension

/**
 * 替换最后一个oldValue
 *
 * @param oldValue
 * @param newValue
 * @return
 */
fun String.replaceLast(oldValue: String, newValue: String): String {
    val index = this.lastIndexOf(oldValue)
    return if (index == -1) {
        this  // 如果没找到，返回原字符串
    } else {
        this.substring(0, index) + newValue + this.substring(index + oldValue.length)
    }
}

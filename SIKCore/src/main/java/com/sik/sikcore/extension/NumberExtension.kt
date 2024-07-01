package com.sik.sikcore.extension

import java.text.DecimalFormat
import java.util.Locale

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

/**
 * 获取百分比
 */
fun Float.percent(retainedNumber:Int = 2): String {
    val percentFormatString = "%.${retainedNumber}f%%"
    return String.format(percentFormatString, this * 100)
}

/**
 * 获取百分比
 */
fun Double.percent(retainedNumber:Int = 2): String {
    val percentFormatString = "%.${retainedNumber}f%%"
    return String.format(percentFormatString, this * 100)
}

/**
 * 格式化大数字为千分位
 */
fun Long.toThousandSeparator(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this)
}

/**
 * 将数字转换为货币格式
 */
fun Double.toCurrency(locale: Locale = Locale.getDefault()): String {
    val formatter = DecimalFormat.getCurrencyInstance(locale)
    return formatter.format(this)
}

/**
 * 检查数字是否为质数
 */
fun Int.isPrime(): Boolean {
    if (this <= 1) return false
    for (i in 2..Math.sqrt(this.toDouble()).toInt()) {
        if (this % i == 0) return false
    }
    return true
}

/**
 * 计算数字的阶乘
 */
fun Int.factorial(): Long {
    if (this < 0) throw IllegalArgumentException("Number must be non-negative")
    return if (this == 0) 1 else this * (this - 1).factorial()
}

/**
 * 转换数字为科学计数法表示
 */
fun Double.toScientificNotation(): String {
    return DecimalFormat("0.###E0").format(this)
}
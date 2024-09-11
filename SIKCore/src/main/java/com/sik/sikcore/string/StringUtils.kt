package com.sik.sikcore.string

import android.util.Base64

/**
 * 字符串工具类
 */
object StringUtils {
    /**
     * 提取中文返回 使用正则
     * @param input
     * @return
     */
    fun getAllZH(input: String?): String {
        //中文正则
        val regexZH = "([\\u4e00-\\u9fa5]+)"
        val strings = RegexUtils.getMatches(regexZH, input)
        val builder = StringBuilder()
        for (i in strings.indices) {
            builder.append(strings[i])
        }
        return builder.toString()
    }

    /**
     * 检查字符串是否是 Base64 编码
     * Base64 字符包含 A-Z, a-z, 0-9, +, / 和可选的 '=' 作为填充字符
     * @param input 要检查的字符串
     * @return 如果是有效的 Base64 字符串则返回 true，否则返回 false
     */
    fun isBase64(input: String): Boolean {
        // 正则表达式匹配 Base64 允许的字符集
        val base64Pattern = "^[A-Za-z0-9+/=]+$".toRegex()

        // 先检查字符串是否符合 Base64 字符规则，再尝试解码验证是否为有效 Base64
        return base64Pattern.matches(input) && try {
            Base64.decode(input, Base64.DEFAULT)
            true
        } catch (e: IllegalArgumentException) {
            // 如果解码失败，说明不是有效的 Base64 字符串
            false
        }
    }

    /**
     * 检查字符串是否是十六进制 (Hex) 编码
     * Hex 字符串包含 0-9 和 A-F (或 a-f)，且长度必须为偶数（每两个字符表示一个字节）
     * @param input 要检查的字符串
     * @return 如果是有效的 Hex 字符串则返回 true，否则返回 false
     */
    fun isHex(input: String): Boolean {
        // 正则表达式匹配十六进制字符集
        val hexPattern = "^[0-9A-Fa-f]+$".toRegex()

        // Hex 字符串的长度必须是偶数，因为每两个字符代表一个字节
        return input.length % 2 == 0 && hexPattern.matches(input)
    }

    /**
     * 检查字符串的类型是 Base64 还是 Hex
     * @param input 要检查的字符串
     * @return 返回 "Base64"、"Hex" 或 "Unknown" 表示未知类型
     */
    fun checkStringType(input: String): String {
        return when {
            isBase64(input) -> "Base64"
            isHex(input) -> "Hex"
            else -> "Unknown"
        }
    }
}
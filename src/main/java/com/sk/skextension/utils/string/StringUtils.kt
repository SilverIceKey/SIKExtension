package com.sk.skextension.utils.string

import com.blankj.utilcode.util.RegexUtils

/**
 * 字符串操作
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
}
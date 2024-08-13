package com.sik.sikcore.string

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * 正则工具类
 */
object RegexUtils {
    /**
     * 返回正则匹配字符串。
     *
     * @param regex 正则表达式.
     * @param input 输入字符串.
     * @return 匹配字符串
     */
    fun getMatches(regex: String, input: CharSequence?): List<String> {
        if (input == null) return emptyList()
        val matches: MutableList<String> = ArrayList()
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(input)
        while (matcher.find()) {
            matches.add(matcher.group())
        }
        return matches
    }

    /**
     * 使用正则表达式替换匹配的字符串。
     *
     * @param regex 正则表达式.
     * @param input 输入字符串.
     * @param replacement 替换内容.
     * @return 替换后的字符串
     */
    fun replaceMatches(regex: String, input: CharSequence?, replacement: String): String {
        if (input == null) return ""
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(input)
        return matcher.replaceAll(replacement)
    }
}
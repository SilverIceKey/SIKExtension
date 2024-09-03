package com.sik.sikcore.string

import java.util.regex.Pattern

/**
 * ValidationType 是一个密封类，用于定义不同的验证类型。
 * 包含内置的验证类型，并且支持通过正则表达式或函数定义自定义验证类型。
 */
sealed class ValidationType {
    abstract fun isValid(input: String): Boolean

    /**
     * 内置的电话号码验证类型
     */
    object Phone : ValidationType() {
        private val phonePattern = "^\\d{3}-?\\d{4}-?\\d{4}\$" // 示例: "123-4567-8901"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(phonePattern, input)
        }
    }

    /**
     * 内置的电子邮件验证类型
     */
    object Email : ValidationType() {
        private val emailPattern = "^[\\w\\.-]+@[\\w\\.-]+\\.\\w+\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(emailPattern, input)
        }
    }

    /**
     * 内置的银行卡号验证类型
     */
    object BankCard : ValidationType() {
        private val bankCardPattern = "^\\d{16}\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(bankCardPattern, input)
        }
    }

    /**
     * 内置的身份证号验证类型
     */
    object IdCard : ValidationType() {
        private val idCardPattern = "^\\d{15}|\\d{18}\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(idCardPattern, input)
        }
    }

    /**
     * 内置的 HTTP URL 验证类型
     */
    object HttpUrl : ValidationType() {
        private val httpUrlPattern = "^http://[\\w\\.-]+(:\\d+)?(/.*)?\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(httpUrlPattern, input)
        }
    }

    /**
     * 内置的 HTTPS URL 验证类型
     */
    object HttpsUrl : ValidationType() {
        private val httpsUrlPattern = "^https://[\\w\\.-]+(:\\d+)?(/.*)?\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(httpsUrlPattern, input)
        }
    }

    /**
     * 内置的 WebSocket URL 验证类型 (ws:// 或 wss://)
     */
    object WebSocketUrl : ValidationType() {
        private val wsUrlPattern = "^ws(s)?://[\\w\\.-]+(:\\d+)?(/.*)?\$"
        override fun isValid(input: String): Boolean {
            return Pattern.matches(wsUrlPattern, input)
        }
    }

    /**
     * 内置的 IPv4 验证类型
     */
    object IPv4 : ValidationType() {
        private val ipv4Pattern =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$"

        override fun isValid(input: String): Boolean {
            return Pattern.matches(ipv4Pattern, input)
        }
    }

    /**
     * 内置的 IPv6 验证类型
     */
    object IPv6 : ValidationType() {
        private val ipv6Pattern =
            "([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)|(([0-9a-fA-F]{1,4}:){1,7}|:):(([0-9a-fA-F]{1,4}:){1,7}|:)"

        override fun isValid(input: String): Boolean {
            return Pattern.matches(ipv6Pattern, input)
        }
    }

    /**
     * 自定义的正则表达式验证类型
     */
    class CustomRegex(private val regex: String) : ValidationType() {
        override fun isValid(input: String): Boolean {
            return Pattern.matches(regex, input)
        }
    }

    /**
     * 自定义的函数验证类型
     */
    class CustomFunction(private val validationFunction: (String) -> Boolean) : ValidationType() {
        override fun isValid(input: String): Boolean {
            return validationFunction(input)
        }
    }
}

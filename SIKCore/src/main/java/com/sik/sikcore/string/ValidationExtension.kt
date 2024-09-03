package com.sik.sikcore.string

/**
 * 扩展函数 isValid，用于根据指定的验证类型检查字符串是否有效
 * @param types 传入多个 ValidationType 实例，表示需要验证的类型
 * @return 如果字符串符合其中任意一个验证类型，则返回 true，否则返回 false
 */
fun String.isValid(vararg types: ValidationType): Boolean {
    return types.any { type -> type.isValid(this) }
}


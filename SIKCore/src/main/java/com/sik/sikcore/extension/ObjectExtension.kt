package com.sik.sikcore.extension

// 扩展Any?（任何可为空的类型）
fun Any?.isNullOrEmpty(): Boolean = when (this) {
    null -> true
    is String -> this.isEmpty() || "null" == this
    is Collection<*> -> this.isEmpty()
    // 可以添加更多类型的检查
    else -> false
}
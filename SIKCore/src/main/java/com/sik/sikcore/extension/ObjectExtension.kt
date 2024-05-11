package com.sik.sikcore.extension

import com.google.gson.Gson

// 扩展Any?（任何可为空的类型）
fun Any?.isNullOrEmpty(): Boolean = when (this) {
    null -> true
    is String -> this.isEmpty() || "null" == this
    is Collection<*> -> this.isEmpty()
    // 可以添加更多类型的检查
    else -> false
}

//任何类型转为json，如果为空默认为对象,可以指定数组
fun Any?.toJson(isJsonObject: Boolean = true): String = if (this == null) {
    if (isJsonObject) {
        "{}"
    } else {
        "[]"
    }
} else {
    Gson().toJson(this)
}
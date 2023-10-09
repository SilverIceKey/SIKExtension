package com.sik.sikcore.extension

import com.tencent.mmkv.MMKV

/**
 * Save m m k v data
 * 指定mmkv保存数据
 * @param T
 * @param key
 * @param value
 */
inline fun <reified T> MMKV.saveMMKVData(key: String, value: T) {
    when (T::class) {
        Boolean::class -> this.encode(key, value as Boolean)
        Float::class -> this.encode(key, value as Float)
        Int::class -> this.encode(key, value as Int)
        Long::class -> this.encode(key, value as Long)
        String::class -> this.encode(key, value as String)
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

/**
 * Get m m k v data
 * 指定mmkv获取数据
 * @param T
 * @param key
 * @param defaultValue
 * @return
 */
inline fun <reified T> MMKV.getMMKVData(key: String, defaultValue: T): T {
    return when (T::class) {
        Boolean::class -> this.decodeBool(key, defaultValue as Boolean) as T
        Float::class -> this.decodeFloat(key, defaultValue as Float) as T
        Int::class -> this.decodeInt(key, defaultValue as Int) as T
        Long::class -> this.decodeLong(key, defaultValue as Long) as T
        String::class -> this.decodeString(key, defaultValue as String) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

/**
 * 保存数据
 */
inline fun <reified T> String.saveMMKVData(value: T) {
    val mmkv = MMKV.defaultMMKV()
    mmkv.saveMMKVData(this, value)
}

/**
 * 获取数据
 */
inline fun <reified T> String.getMMKVData(defaultValue: T): T {
    val mmkv = MMKV.defaultMMKV()
    return mmkv.getMMKVData(this, defaultValue)
}
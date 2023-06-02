package com.sk.skextension.utils.net.retrofit

/**
 * 创建Api接口
 */
fun <T> Class<T>.create(): T {
    return RetrofitClient.instance.createService(this)
}
package com.sik.siknet.retrofit

/**
 * 创建Api接口
 */
fun <T> Class<T>.create(): T {
    return RetrofitClient.instance.createService(this)
}
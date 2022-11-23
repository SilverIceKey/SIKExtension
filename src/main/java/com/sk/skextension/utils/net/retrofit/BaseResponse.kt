package com.sk.skextension.utils.net.retrofit

/**
 * 基础返回数据类型
 */
open class BaseResponse<T> {
    var code: Int = -1
    var msg: String = ""
    var data: T? = null
}
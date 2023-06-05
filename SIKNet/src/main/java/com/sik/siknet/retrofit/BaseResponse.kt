package com.sik.siknet.retrofit

/**
 * 基础返回数据类型
 */
open class BaseResponse<T> {
    var returnNo: Int = -1
    var returnMsg: String = ""
    var result: T? = null
}
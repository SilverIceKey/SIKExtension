package com.sik.siknet.http.ssl

import java.io.InputStream

/**
 * 证书来源
 */
interface CertSource {
    /**
     * 获取别名
     */
    fun getAlias(): String

    /**
     * 获取证书来源流
     */
    fun getCertSourceInputStream(): InputStream
}
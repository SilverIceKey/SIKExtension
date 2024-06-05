package com.sik.siknet.http.ssl

import java.io.FileInputStream
import java.io.InputStream

/**
 * 默认从文件路径加载证书
 */
class DefaultFileCertSource(private val filePath: String) : CertSource {
    private val alias: String = "file-$filePath"
    override fun getAlias(): String {
        return alias
    }

    override fun getCertSourceInputStream(): InputStream {
        return FileInputStream(filePath)
    }
}
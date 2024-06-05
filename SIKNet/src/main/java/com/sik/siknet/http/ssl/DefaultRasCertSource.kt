package com.sik.siknet.http.ssl

import android.content.Context
import com.sik.sikcore.SIKCore
import java.io.InputStream

/**
 * 默认从res文件夹加载
 */
class DefaultRasCertSource(
    private val context: Context = SIKCore.getApplication(),
    private val resId: Int
) : CertSource {
    private val alias: String = "raw-$resId"
    override fun getAlias(): String {
        return alias
    }

    override fun getCertSourceInputStream(): InputStream {
        return context.resources.openRawResource(resId)
    }
}
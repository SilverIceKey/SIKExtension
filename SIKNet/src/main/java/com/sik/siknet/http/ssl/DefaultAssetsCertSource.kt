package com.sik.siknet.http.ssl

import android.content.Context
import com.sik.sikcore.SIKCore
import java.io.InputStream

/**
 * 默认从assets中加载证书
 */
class DefaultAssetsCertSource(private val context: Context = SIKCore.getApplication(), private val assetPath: String) : CertSource {
    private val alias: String = "assets-$assetPath"
    override fun getAlias(): String {
        return alias
    }

    override fun getCertSourceInputStream(): InputStream {
        return context.assets.open(assetPath)
    }
}
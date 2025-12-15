package com.sik.siknet.http

import android.util.Log
import com.sik.siknet.http.dns.DefaultDns
import com.sik.siknet.http.interceptor.AutoSaveCookieJar
import com.sik.siknet.http.interceptor.DefaultHeaderInterceptor
import com.sik.siknet.http.interceptor.DefaultParameterInterceptor
import com.sik.siknet.http.ssl.CertSource
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object HttpUtils {

    /**
     * 请求头类型
     */
    val CLIENT_MEDIA_TYPE: MediaType? =
        "application/json; charset=utf-8".toMediaTypeOrNull()

    /**
     * 应用层拦截器
     */
    val interceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * 网络拦截器
     */
    val networkInterceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * 是否在请求时打印日志
     */
    var isLoggerInRequest: Boolean = true

    /**
     * 证书源
     */
    val certSources: MutableList<CertSource> = mutableListOf()

    /**
     * 证书加载策略（可被替换）
     */
    var loadCertificates: (List<CertSource>) -> SSLSocketFactory = {
        createSSLSocketFactory(it)
    }

    // =====================================================
    //  非 2xx / 网络异常兜底机制（重点）
    // =====================================================

    /**
     * 默认兜底行为：
     * - 仅记录日志
     * - 不做 UI
     * - 不抛异常
     *
     * 上层可直接替换：
     * HttpUtils.defaultErrorFallback = { toast / dialog / 埋点 }
     */
    var defaultErrorFallback: (Request, NetException) -> Unit = { req, ex ->
        Log.e(
            "HttpUtils",
            "HTTP fallback\n" +
                    "url=${req.url}\n" +
                    "method=${req.method}\n" +
                    "msg=${ex.message}",
            ex
        )
    }

    /**
     * 全局异常处理器
     *
     * @return true  -> 已处理（解析层可以吞异常）
     * @return false -> 未处理（解析层应抛 NetException）
     */
    var globalNetExceptionHandler: (Request, NetException) -> Boolean = { req, ex ->
        defaultErrorFallback(req, ex)
        true // 默认：兜底后吞掉
    }

    /**
     * 工具方法：用于解析层在发现非 2xx 时调用
     */
    fun handleHttpError(
        request: Request,
        code: Int,
        rawBody: String? = null
    ): Boolean {
        val msg = buildString {
            append("HTTP $code")
            if (!rawBody.isNullOrBlank()) append(" body=$rawBody")
        }
        return globalNetExceptionHandler(
            request,
            NetException(request, msg, null)
        )
    }

    // =====================================================
    //  OkHttp Client 构建
    // =====================================================

    fun createOkHttpClient(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient {
        return createOkHttpClientBuilder(timeoutTime, timeoutUnit).build()
    }

    fun createOkHttpClientBuilder(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .cookieJar(AutoSaveCookieJar())
            .addInterceptor(DefaultHeaderInterceptor())
            .addInterceptor(DefaultParameterInterceptor())
            .apply {
                interceptor.forEach { addInterceptor(it) }
                networkInterceptor.forEach { addNetworkInterceptor(it) }
            }
            .dns(DefaultDns())
            .connectTimeout(timeoutTime, timeoutUnit)
            .readTimeout(timeoutTime, timeoutUnit)
            .writeTimeout(timeoutTime, timeoutUnit)
            .followRedirects(true)

        if (certSources.isNotEmpty()) {
            val sslSocketFactory = loadCertificates(certSources)
            val trustManager = createTrustManager(certSources)
            builder.sslSocketFactory(sslSocketFactory, trustManager)
        }

        return builder
    }

    // =====================================================
    //  SSL
    // =====================================================

    private fun createSSLSocketFactory(certSources: List<CertSource>): SSLSocketFactory {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        certSources.forEach { source ->
            source.getCertSourceInputStream().use {
                val cert = certificateFactory.generateCertificate(it) as X509Certificate
                keyStore.setCertificateEntry(source.getAlias(), cert)
            }
        }

        val tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        tmf.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext.socketFactory
    }

    private fun createTrustManager(certSources: List<CertSource>): X509TrustManager {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        certSources.forEach { source ->
            source.getCertSourceInputStream().use {
                val cert = certificateFactory.generateCertificate(it) as X509Certificate
                keyStore.setCertificateEntry(source.getAlias(), cert)
            }
        }

        val tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        tmf.init(keyStore)

        return tmf.trustManagers[0] as X509TrustManager
    }
}

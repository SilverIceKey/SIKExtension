package com.sik.siknet.http

import android.util.Log
import com.sik.siknet.http.dns.DefaultDns
import com.sik.siknet.http.interceptor.AutoSaveCookieJar
import com.sik.siknet.http.interceptor.DefaultHeaderInterceptor
import com.sik.siknet.http.interceptor.DefaultParameterInterceptor
import com.sik.siknet.http.ssl.CertSource
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
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

    /**
     * 全局共享连接池
     */
    private val sharedConnectionPool: ConnectionPool by lazy {
        ConnectionPool(
            8,
            5,
            TimeUnit.MINUTES
        )
    }

    /**
     * 全局共享调度器
     */
    private val sharedDispatcher: Dispatcher by lazy {
        Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 16
        }
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
        true
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
    //  Client 构建
    // =====================================================

    /**
     * 全局 API Client
     * 普通接口统一走这里
     */
    val apiClient: OkHttpClient by lazy {
        createBaseBuilder(
            timeoutTime = 60,
            timeoutUnit = TimeUnit.SECONDS
        ).build()
    }

    /**
     * 全局下载 Client
     * 下载/大文件统一走这里
     */
    val downloadClient: OkHttpClient by lazy {
        createBaseBuilder(
            timeoutTime = 5,
            timeoutUnit = TimeUnit.MINUTES
        ).build()
    }

    /**
     * 为兼容旧调用保留
     * 以后普通请求都走 apiClient
     */
    fun createOkHttpClient(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient {
        return if (timeoutTime == 60L && timeoutUnit == TimeUnit.SECONDS) {
            apiClient
        } else if (timeoutTime == 5L && timeoutUnit == TimeUnit.MINUTES) {
            downloadClient
        } else {
            createBaseBuilder(timeoutTime, timeoutUnit).build()
        }
    }

    /**
     * 为兼容旧调用保留
     * 注意：推荐优先从 apiClient/downloadClient 派生 newBuilder()
     */
    fun createOkHttpClientBuilder(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient.Builder {
        return if (timeoutTime == 60L && timeoutUnit == TimeUnit.SECONDS) {
            apiClient.newBuilder()
        } else if (timeoutTime == 5L && timeoutUnit == TimeUnit.MINUTES) {
            downloadClient.newBuilder()
        } else {
            createBaseBuilder(timeoutTime, timeoutUnit)
        }
    }

    /**
     * 真正的基础 Builder
     */
    private fun createBaseBuilder(
        timeoutTime: Long,
        timeoutUnit: TimeUnit
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
            .dispatcher(sharedDispatcher)
            .connectionPool(sharedConnectionPool)
            .connectTimeout(timeoutTime, timeoutUnit)
            .readTimeout(timeoutTime, timeoutUnit)
            .writeTimeout(timeoutTime, timeoutUnit)
            .followRedirects(true)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))

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
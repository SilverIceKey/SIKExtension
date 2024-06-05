package com.sik.siknet.http

import com.sik.sikcore.log.LogUtils
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
     * 日志
     */
    val logger = LogUtils.getLogger(HttpUtils::class)

    /**
     * 请求头类型
     */
    val CLIENT_MEDIA_TYPE: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

    /**
     * 拦截器
     */
    val interceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * 网络拦截器
     */
    val networkInterceptor: MutableList<Interceptor> = mutableListOf()

    /**
     * 全局网络异常处理
     */
    var globalNetExceptionHandler: (Request, NetException) -> Boolean = { _, _ -> false }

    /**
     * 是否在请求时写日志
     */
    var isLoggerInRequest: Boolean = true

    /**
     * 证书源数组
     */
    val certSources: MutableList<CertSource> = mutableListOf()

    /**
     * 默认的证书加载方法
     */
    var loadCertificates: (List<CertSource>) -> SSLSocketFactory = { certSources ->
        createSSLSocketFactory(certSources)
    }

    /**
     * Create ok http client
     * 创建okhttpClient
     * @param timeoutTime
     * @param timeoutUnit
     * @return
     */
    fun createOkHttpClient(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient {
        return createOkHttpClientBuilder(timeoutTime, timeoutUnit)
            .build()
    }

    /**
     * Create ok http client builder
     * 创建okhttp client建造器
     * @param timeoutTime
     * @param timeoutUnit
     * @return
     */
    fun createOkHttpClientBuilder(
        timeoutTime: Long = 60,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .cookieJar(AutoSaveCookieJar())
            .addInterceptor(DefaultHeaderInterceptor())
            .addInterceptor(DefaultParameterInterceptor())
            .apply {
                if (interceptor.isNotEmpty()) {
                    interceptor.forEach {
                        addInterceptor(it)
                    }
                }
                if (networkInterceptor.isNotEmpty()) {
                    networkInterceptor.forEach {
                        addNetworkInterceptor(it)
                    }
                }
            }
            .connectTimeout(timeoutTime, timeoutUnit)
            .readTimeout(timeoutTime, timeoutUnit)
            .writeTimeout(timeoutTime, timeoutUnit)
            .followRedirects(true)

        // 如果有证书源，加载证书
        if (certSources.isNotEmpty()) {
            val sslSocketFactory = loadCertificates(certSources)
            val trustManager = createTrustManager(certSources)
            builder.sslSocketFactory(sslSocketFactory, trustManager)
        }

        return builder
    }

    /**
     * 创建 SSL Socket Factory
     */
    private fun createSSLSocketFactory(certSources: List<CertSource>): SSLSocketFactory {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        certSources.forEach { certSource ->
            val inputStream = certSource.getCertSourceInputStream()
            inputStream.use {
                val cert = certificateFactory.generateCertificate(it) as X509Certificate
                keyStore.setCertificateEntry(certSource.getAlias(), cert)
            }
        }

        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)
        return sslContext.socketFactory
    }

    /**
     * 创建 Trust Manager
     */
    private fun createTrustManager(certSources: List<CertSource>): X509TrustManager {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        certSources.forEach { certSource ->
            val inputStream = certSource.getCertSourceInputStream()
            inputStream.use {
                val cert = certificateFactory.generateCertificate(it) as X509Certificate
                keyStore.setCertificateEntry(certSource.getAlias(), cert)
            }
        }

        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        return trustManagerFactory.trustManagers[0] as X509TrustManager
    }
}
package com.sk.skextension.utils.net.retrofit

import android.content.Context
import okhttp3.*
import okhttp3.Credentials.basic
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * retrofit客户端
 */
class RetrofitClient private constructor() {
    /**
     * 全局Context
     */
    private var context: Context? = null

    /**
     * 根据不同配置创建okhttpclient
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val okHttpClients: ConcurrentHashMap<Class<RetrofitConfig>, OkHttpClient> =
        ConcurrentHashMap()

    /**
     * 根据不同配置创建Retrofit
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val retrofits: ConcurrentHashMap<Class<RetrofitConfig>, Retrofit> = ConcurrentHashMap()

    /**
     * retrofit默认配置
     */
    private var defaultConfigClazz: Class<RetrofitConfig>? = null

    /**
     * 日志拦截器
     */
    private var httpLoggingInterceptor: HttpLoggingInterceptor

    /**
     * 默认配置
     */
    private var defaultConfig: RetrofitConfig? = null

    /**
     * 临时配置
     */
    private var tempConfig: RetrofitConfig? = null

    /**
     * 根据不同配置创建HeaderParamsPreloadInterceptor
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val headerParamsPreloadInterceptors: ConcurrentHashMap<Class<RetrofitConfig>, HeaderParamsPreloadInterceptor> =
        ConcurrentHashMap()

    /**
     * 更新token操作
     */
    var updateToken: () -> Unit = {}

    companion object {
        val instance: RetrofitClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitClient()
        }
    }

    /**
     * RetrofitClient初始化
     */
    init {
        //初始化日志拦截器
        httpLoggingInterceptor = HttpLoggingInterceptor(HttpLogger())
        //设置拦截等级
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    /**
     * 设置全局Context
     *
     * @param context
     */
    fun setApplicationContext(context: Context?) {
        this.context = context
    }

    /**
     * 注册Retrofit
     */
    private fun registerRetrofitConfig(retrofitConfig: RetrofitConfig) {
        var okHttpClient: OkHttpClient? = null
        val className = retrofitConfig.javaClass
        if (okHttpClients[className] == null) {
            okHttpClient = initOkhttp(retrofitConfig)
            okHttpClients.put(className, okHttpClient)
        }
        if (retrofits[retrofitConfig.javaClass] == null) {
            retrofits.put(className, initAndGet(retrofitConfig, okHttpClient!!))
        }
    }

    /**
     * 设置默认配置
     *
     * @param retrofitConfig
     * @return
     */
    fun defaultConfig(retrofitConfig: RetrofitConfig): RetrofitClient {
        defaultConfig = retrofitConfig
        defaultConfigClazz = retrofitConfig.javaClass
        registerRetrofitConfig(retrofitConfig)
        return this
    }

    /**
     * 设置临时配置
     *
     * @param retrofitConfig
     * @return
     */
    fun config(retrofitConfig: RetrofitConfig): RetrofitClient {
        if (okHttpClients[retrofitConfig.javaClass] != null && retrofits[retrofitConfig.javaClass] != null) {
            return this
        }
        tempConfig = retrofitConfig
        registerRetrofitConfig(retrofitConfig)
        return this
    }

    /**
     * 初始化retrofit和okhttpclient
     */
    private fun initAndGet(retrofitConfig: RetrofitConfig, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(retrofitConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    /**
     * 初始化okhttpclient
     *
     * @return
     */
    private fun initOkhttp(retrofitConfig: RetrofitConfig): OkHttpClient {
        val cacheSize = (10 * 1024 * 1024).toLong()
        val cacheFile = File(context!!.externalCacheDir, "retrofit")
        if (!cacheFile.exists()) {
            cacheFile.mkdirs()
        }
        val cache = Cache(cacheFile, cacheSize)
        val builder = OkHttpClient.Builder()
        if (retrofitConfig.proxyType() != Proxy.Type.DIRECT) {
            val proxy = Proxy(
                retrofitConfig.proxyType(), InetSocketAddress(
                    retrofitConfig.proxyIPAddr(), retrofitConfig.proxyPort()
                )
            )
            builder.proxy(proxy)
        }
        val headerParamsPreloadInterceptor = HeaderParamsPreloadInterceptor()
        headerParamsPreloadInterceptor.addHeader(retrofitConfig.defaultHeaders())
        if (!retrofitConfig.defaultParams().isEmpty()) {
            headerParamsPreloadInterceptor.addParams(retrofitConfig.defaultParams())
        }
        headerParamsPreloadInterceptors.put(
            retrofitConfig.javaClass,
            headerParamsPreloadInterceptor
        )
        builder
            .proxyAuthenticator { _: Route?, response: Response ->
                val credential = basic(
                    retrofitConfig.proxyUserName(), retrofitConfig.proxyPassword()
                )
                response.request.newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build()
            }
            .addInterceptor(headerParamsPreloadInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .cache(cache)
            .connectTimeout(retrofitConfig.connectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(retrofitConfig.readTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(retrofitConfig.writeTimeout(), TimeUnit.MILLISECONDS)
        return builder.build()
    }

    /**
     * 添加更新token方法
     */
    fun addUpdateToken(updateToken: () -> Unit) {
        this.updateToken = updateToken
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     * @return
     */
    fun addInterceptor(
        interceptor: Interceptor?,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        val okHttpClient =
            okHttpClients[clazz]?.newBuilder()?.addInterceptor(interceptor!!)?.build()
        retrofits[clazz]?.newBuilder()?.client(okHttpClient!!)?.build()
        return this
    }

    /**
     * 添加网络拦截器
     *
     * @param interceptor
     * @return
     */
    fun addNetworkInterceptor(
        interceptor: Interceptor?,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        val okHttpClient =
            okHttpClients[clazz]?.newBuilder()?.addNetworkInterceptor(interceptor!!)?.build()
        retrofits[clazz]?.newBuilder()?.client(okHttpClient!!)?.build()
        return this
    }

    /**
     * 返回请求接口实例
     *
     * @param service
     * @param <T>
     * @return
    </T> */
    fun <T> createService(
        service: Class<T>,
        clazz: Class<RetrofitConfig>? = defaultConfigClazz
    ): T {
        if (clazz == null) {
            throw NullPointerException("请先设置默认配置或临时配置")
        }
        if (defaultConfig?.isTokenShouldUpdate()!!) {
            updateToken()
        }
        if (clazz == defaultConfigClazz) {
            addDefaultHeader(defaultConfig?.defaultHeaders(), defaultConfigClazz!!)
            addDefaultParams(defaultConfig?.defaultParams(),defaultConfigClazz!!)
        } else {
            addDefaultHeader(tempConfig?.defaultHeaders(), clazz)
            addDefaultParams(tempConfig?.defaultParams(),clazz)
        }
        return retrofits[clazz]!!.create(service)
    }

    /**
     * 添加默认请求头
     *
     * @param key
     * @param value
     * @return
     */
    fun addDefaultHeader(
        key: String?, value: String?,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[clazz]?.addHeader(key!!, value!!)
        return this
    }

    /**
     * 批量添加默认请求头
     *
     * @param headers
     * @return
     */
    fun addDefaultHeader(
        headers: Map<String, String>?,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[clazz]?.addHeader(headers)
        return this
    }

    /**
     * 添加默认参数
     *
     * @param key
     * @param value
     * @return
     */
    fun addDefaultParams(
        key: String, value: String,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[clazz]?.addHeader(key, value)
        return this
    }

    /**
     * 批量添加默认参数
     *
     * @param params
     * @return
     */
    fun addDefaultParams(
        params: Map<String, String>?,
        clazz: Class<RetrofitConfig> = defaultConfigClazz!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[clazz]?.addParams(params)
        return this
    }
}
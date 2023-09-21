package com.sik.siknet.net

import com.sik.sikcore.SIKCore
import okhttp3.Cache
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
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
     * 根据不同配置创建okhttpclient
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val okHttpClients: ConcurrentHashMap<String, OkHttpClient> =
        ConcurrentHashMap()

    /**
     * 根据不同配置创建Retrofit
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val retrofits: ConcurrentHashMap<String, Retrofit> = ConcurrentHashMap()

    /**
     * 已经添加的配置文件
     */
    private val retrofitConfigs: ConcurrentHashMap<String, RetrofitConfig> = ConcurrentHashMap()

    /**
     * retrofit默认配置
     */
    private var defaultConfigTAG: String? = null

    /**
     * 日志拦截器
     */
    private var httpLoggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor(HttpLogger())

    /**
     * 根据不同配置创建HeaderParamsPreloadInterceptor
     * 优点：加快服务器切换使用时间
     * 缺点：增加内存占用
     */
    private val headerParamsPreloadInterceptors: ConcurrentHashMap<String, HeaderParamsPreloadInterceptor> =
        ConcurrentHashMap()

    /**
     * 更新token操作
     */
    private var updateToken: HashMap<String, () -> Unit> = hashMapOf()

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
        //设置拦截等级
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    /**
     * 注册Retrofit
     */
    private fun registerRetrofitConfig(retrofitConfig: RetrofitConfig) {
        var okHttpClient: OkHttpClient? = null
        val tag = retrofitConfig.TAG
        if (okHttpClients[tag] == null) {
            okHttpClient = initOkhttp(retrofitConfig)
            okHttpClients[tag] = okHttpClient
        }
        if (retrofits[tag] == null) {
            retrofits[tag] = initAndGet(retrofitConfig, okHttpClient!!)
        }
    }

    /**
     * 设置默认配置
     *
     * @param retrofitConfig
     * @return
     */
    fun defaultConfig(retrofitConfig: RetrofitConfig): RetrofitClient {
        retrofitConfigs[retrofitConfig.TAG] = retrofitConfig
        defaultConfigTAG = retrofitConfig.TAG
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
        if (okHttpClients[retrofitConfig.TAG] != null && retrofits[retrofitConfig.TAG] != null) {
            return this
        }
        retrofitConfigs[retrofitConfig.TAG] = retrofitConfig
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
        val cacheFile: File = File(SIKCore.getApplication().externalCacheDir, "retrofit")
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
        if (retrofitConfig.defaultParams().isNotEmpty()) {
            headerParamsPreloadInterceptor.addParams(retrofitConfig.defaultParams())
        }
        headerParamsPreloadInterceptors[retrofitConfig.TAG] = headerParamsPreloadInterceptor
        builder
            .proxyAuthenticator { _: Route?, response: Response ->
                val credential = basic(
                    retrofitConfig.proxyUserName(), retrofitConfig.proxyPassword()
                )
                response.request.newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build()
            }
            .cookieJar(AutoSaveCookieJar())
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
    fun addUpdateToken(updateToken: () -> Unit, tag: String? = defaultConfigTAG) {
        this.updateToken[defaultConfigTAG!!] = updateToken
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     * @return
     */
    fun addInterceptor(
        interceptor: Interceptor?,
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        val okHttpClient =
            okHttpClients[tag]?.newBuilder()?.addInterceptor(interceptor!!)?.build()!!
        okHttpClients[tag] = okHttpClient
        retrofits[tag] = retrofits[tag]?.newBuilder()?.client(okHttpClients[tag]!!)?.build()!!
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
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        val okHttpClient =
            okHttpClients[tag]?.newBuilder()?.addNetworkInterceptor(interceptor!!)?.build()!!
        okHttpClients[tag] = okHttpClient
        retrofits[tag] = retrofits[tag]?.newBuilder()?.client(okHttpClients[tag]!!)?.build()!!
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
        tag: String? = defaultConfigTAG
    ): T {
        if (tag == null) {
            throw NullPointerException("请先设置默认配置或临时配置")
        }
        if (retrofitConfigs[tag]?.isTokenShouldUpdate()!!) {
            updateToken[tag]?.let { it() }
        }
        addDefaultHeader(retrofitConfigs[tag]?.defaultHeaders(), tag)
        addDefaultParams(retrofitConfigs[tag]?.defaultParams(), tag)
        return retrofits[tag]!!.create(service)
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
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[tag]?.addHeader(key!!, value!!)
        return this
    }

    /**
     * 批量添加默认请求头
     *
     * @param headers
     * @return
     */
    private fun addDefaultHeader(
        headers: Map<String, String>?,
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[tag]?.addHeader(headers)
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
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[tag]?.addHeader(key, value)
        return this
    }

    /**
     * 批量添加默认参数
     *
     * @param params
     * @return
     */
    private fun addDefaultParams(
        params: Map<String, String>?,
        tag: String = defaultConfigTAG!!
    ): RetrofitClient {
        headerParamsPreloadInterceptors[tag]?.addParams(params)
        return this
    }
}
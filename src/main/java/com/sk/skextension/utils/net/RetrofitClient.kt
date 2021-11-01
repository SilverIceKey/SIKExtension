package com.sk.skextension.utils.net

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
     * okhttp客户端
     */
    private var okHttpClient: OkHttpClient? = null

    /**
     * retrofit实例
     */
    private var retrofit: Retrofit? = null

    /**
     * retrofit临时配置
     */
    private var retrofitConfig: RetrofitConfig? = null

    /**
     * retrofit默认配置
     */
    private var defaultConfig: RetrofitConfig? = null

    /**
     * 目前配置是否为默认配置
     */
    private var isConfigDefault = false

    /**
     * 是否使用默认配置
     */
    private var useDefaultConfig = false

    /**
     * 日志拦截器
     */
    private var httpLoggingInterceptor: HttpLoggingInterceptor

    /**
     * 头部和参数默认添加拦截器
     */
    private var headerParamsPreloadInterceptor: HeaderParamsPreloadInterceptor

    companion object{
        val instance:RetrofitClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
            RetrofitClient()
        }
    }

    /**
     * RetrofitClient初始化
     */
    init {
        //判断日志拦截器是否为空
        //初始化日志拦截器
        httpLoggingInterceptor = HttpLoggingInterceptor(HttpLogger())
        //设置拦截等级
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        //初始化默认头部和参数拦截器
        headerParamsPreloadInterceptor = HeaderParamsPreloadInterceptor()
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
     * 设置默认配置
     *
     * @param retrofitConfig
     * @return
     */
    fun defaultConfig(retrofitConfig: RetrofitConfig): RetrofitClient {
        defaultConfig = retrofitConfig
        this.retrofitConfig = retrofitConfig
        useDefaultConfig = true
        headerParamsPreloadInterceptor.addHeader(retrofitConfig.defaultHeaders())
        headerParamsPreloadInterceptor.addParams(retrofitConfig.defaultParams())
        return this
    }

    /**
     * 设置临时配置
     *
     * @param retrofitConfig
     * @return
     */
    fun config(retrofitConfig: RetrofitConfig): RetrofitClient {
        this.retrofitConfig = retrofitConfig
        useDefaultConfig = false
        headerParamsPreloadInterceptor.addHeader(retrofitConfig.defaultHeaders())
        headerParamsPreloadInterceptor.addParams(retrofitConfig.defaultParams())
        return this
    }

    /**
     * 初始化retrofit和okhttpclient
     */
    private fun init() {
        retrofit = Retrofit.Builder()
            .baseUrl(retrofitConfig!!.baseUrl)
            .client(initOkhttp())
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
    private fun initOkhttp(): OkHttpClient {
        val cacheSize = (10 * 1024 * 1024).toLong()
        val cacheFile = File(context!!.externalCacheDir, "retrofit")
        if (!cacheFile.exists()) {
            cacheFile.mkdirs()
        }
        val cache = Cache(cacheFile, cacheSize)
        val builder = OkHttpClient.Builder()
        if (retrofitConfig!!.proxyType() != Proxy.Type.DIRECT) {
            val proxy = Proxy(
                retrofitConfig!!.proxyType(), InetSocketAddress(
                    retrofitConfig!!.proxyIPAddr(), retrofitConfig!!.proxyPort()
                )
            )
            builder.proxy(proxy)
        }
        builder
            .proxyAuthenticator(Authenticator { route: Route?, response: Response ->
                val credential = basic(
                    retrofitConfig!!.proxyUserName(), retrofitConfig!!.proxyPassword()
                )
                response.request.newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build()
            })
            .addInterceptor(headerParamsPreloadInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .cache(cache)
            .connectTimeout(retrofitConfig!!.connectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(retrofitConfig!!.ReadTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(retrofitConfig!!.WriteTimeout(), TimeUnit.MILLISECONDS)
        okHttpClient = builder.build()
        return okHttpClient!!
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     * @return
     */
    fun addInterceptor(interceptor: Interceptor?): RetrofitClient {
        okHttpClient = okHttpClient!!.newBuilder().addInterceptor(interceptor!!).build()
        retrofit = retrofit!!.newBuilder().client(okHttpClient!!).build()
        return this
    }

    /**
     * 添加网络拦截器
     *
     * @param interceptor
     * @return
     */
    fun addNetworkInterceptor(interceptor: Interceptor?): RetrofitClient {
        okHttpClient = okHttpClient!!.newBuilder().addNetworkInterceptor(interceptor!!).build()
        retrofit = retrofit!!.newBuilder().client(okHttpClient!!).build()
        return this
    }

    /**
     * 返回请求接口实例
     *
     * @param service
     * @param <T>
     * @return
    </T> */
    fun <T> createService(service: Class<T>): T {
        if (defaultConfig == null || retrofitConfig == null) {
            throw NullPointerException("请先设置默认配置或临时配置")
        }
        return if (useDefaultConfig) {
            if (isConfigDefault) {
                retrofit!!.create(service)
            } else {
                retrofitConfig = defaultConfig
                init()
                isConfigDefault = true
                retrofit!!.create(service)
            }
        } else {
            init()
            isConfigDefault = false
            useDefaultConfig = true
            retrofit!!.create(service)
        }
    }

    /**
     * 添加默认请求头
     *
     * @param key
     * @param value
     * @return
     */
    fun addDefaultHeader(key: String?, value: String?): RetrofitClient {
        headerParamsPreloadInterceptor.addHeader(key!!, value!!)
        return this
    }

    /**
     * 批量添加默认请求头
     *
     * @param headers
     * @return
     */
    fun addDefaultHeader(headers: Map<String, String>?): RetrofitClient {
        headerParamsPreloadInterceptor.addHeader(headers)
        return this
    }

    /**
     * 添加默认参数
     *
     * @param key
     * @param value
     * @return
     */
    fun addDefaultParams(key: String, value: String): RetrofitClient {
        headerParamsPreloadInterceptor.addHeader(key, value)
        return this
    }

    /**
     * 批量添加默认参数
     *
     * @param params
     * @return
     */
    fun addDefaultParams(params: Map<String, String>?): RetrofitClient {
        headerParamsPreloadInterceptor.addParams(params)
        return this
    }
}
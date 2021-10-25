package com.sk.skextension.utils.net;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 头部和参数默认添加拦截器
 */
public class HeaderParamsPreloadInterceptor implements Interceptor {
    /**
     * 默认头部
     */
    private final HashMap<String, String> mHeader = new HashMap<>();
    /**
     * 默认参数
     */
    private final HashMap<String, String> mParams = new HashMap<>();

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        RequestBody requestBody = chain.request().body();
        for (String key:mHeader.keySet()){
            requestBuilder.header(key,mHeader.get(key));
        }
        if (requestBody==null||requestBody instanceof FormBody){
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            if (requestBody!=null){
                for (int i = 0; i < ((FormBody) requestBody).size(); i++) {
                    formBodyBuilder.addEncoded(((FormBody) requestBody).encodedName(i),
                            ((FormBody) requestBody).encodedValue(i));
                }
            }
            for (String key:mParams.keySet()){
                formBodyBuilder.addEncoded(key,mParams.get(key));
            }
            requestBody = formBodyBuilder.build();
        }
        if (requestBody!=null){
            requestBuilder.post(requestBody);
        }
        return chain.proceed(requestBuilder.build());
    }

    /**
     * 添加默认头部
     * @param key
     * @param value
     * @return
     */
    public HeaderParamsPreloadInterceptor addHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    /**
     * 批量添加默认头部
     * @param headers
     * @return
     */
    public HeaderParamsPreloadInterceptor addHeader(Map<String, String> headers) {
        mHeader.putAll(headers);
        return this;
    }

    /**
     * 添加默认参数
     * @param key
     * @param value
     * @return
     */
    public HeaderParamsPreloadInterceptor addParams(String key, String value) {
        mParams.put(key, value);
        return this;
    }

    /**
     * 批量添加默认参数
     * @param params
     * @return
     */
    public HeaderParamsPreloadInterceptor addParams(Map<String, String> params) {
        mParams.putAll(params);
        return this;
    }
}

package com.sk.skextension.utils.net;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 日志拦截器
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger {
    /**
     * 请求信息全部日志
     */
    private final StringBuffer mMessage = new StringBuffer();

    @Override
    public void log(@NotNull String message) {
        // 请求或者响应开始
        if (message.startsWith("--> POST")) {
            mMessage.setLength(0);
        }
        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if ((message.startsWith("{") && message.endsWith("}"))
                || (message.startsWith("[") && message.endsWith("]"))) {
            message = JsonUtil.formatJson(JsonUtil.decodeUnicode(message));
        }
        mMessage.append(message.concat("\n"));
        // 响应结束，打印整条日志
        if (message.startsWith("<-- END HTTP")) {
            LoggerFactory.getLogger(HttpLogger.class.getSimpleName()).info(mMessage.toString());
            mMessage.delete(0, mMessage.length());
        }
    }
}

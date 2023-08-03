package com.sik.siknet.net

import com.sik.sikcore.log.LogUtils
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 日志拦截器
 */
open class HttpLogger : HttpLoggingInterceptor.Logger {
    /**
     * 请求信息全部日志
     */
    private val mMessage = StringBuffer()

    /**
     * 记录日志
     */
    override fun log(message: String) {
        // 请求或者响应开始
        try {
            var mresultMessage = message
            if (mresultMessage.startsWith("--> POST") || mresultMessage.startsWith("--> GET") || mresultMessage.startsWith(
                    "--> PUT"
                )
            ) {
                mMessage.setLength(0)
            }
            /**
             * 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
             * 如果需要json格式输出，取消注释，将下一行注释
             *
             */
//        if (mresultMessage.startsWith("{") && mresultMessage.endsWith("}")
//            || mresultMessage.startsWith("[") && mresultMessage.endsWith("]")
//        ) {
//            mresultMessage = formatJson(decodeUnicode(mresultMessage))
//        }
//            mresultMessage = decodeUnicode(mresultMessage)
            mMessage.appendLine(mresultMessage)
            // 响应结束，打印整条日志
            if (mresultMessage.startsWith("<-- END HTTP")) {
                endLog(mMessage.toString())
                mMessage.delete(0, mMessage.length)
            }
        }catch (e:Exception){
            LogUtils.logger.e(e.message)
        }
    }

    /**
     * 输出结束
     */
    protected open fun endLog(message: String) {

    }
}
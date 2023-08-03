package com.sik.sikcore.log

import android.os.Environment
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.sik.sikcore.extension.existsAndCreateFolder
import org.slf4j.impl.LoggerFactory

/**
 * 日志工具类
 */
class LogUtils {
    companion object {
        private const val LOG_FILE_NAME_PATTERN = "app_log.%d{yyyy-MM-dd}.%i.log"

        /**
         * Debug
         * 是否为调试模式
         */
        var DEBUG: Boolean = true

        /**
         * 日志保存日期
         */
        @JvmStatic
        var LOG_BACKUP_INDEX = 7

        /**
         * 日志保存大小
         */
        @JvmStatic
        var MAX_LOG_FILE_SIZE = "10MB"

        /**
         * Logger
         * 日志信息
         */
        val logger: LogUtils by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            LogUtils()
        }
    }

    private val rootLogger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    }
    private val loggerContext = rootLogger.loggerContext

    init {
        if (DEBUG) {
            BasicLogcatConfigurator.configureDefaultContext()

            // Pattern
            val encoder: PatternLayoutEncoder = PatternLayoutEncoder()
            encoder.context = loggerContext
            encoder.pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
            encoder.start()

            // Appender
            val appender: RollingFileAppender<ILoggingEvent> = RollingFileAppender<ILoggingEvent>()
            appender.context = loggerContext
            appender.name = "FILE"
            appender.encoder = encoder

            // Rolling Policy
            val rollingPolicy: SizeAndTimeBasedRollingPolicy<ILoggingEvent> =
                SizeAndTimeBasedRollingPolicy()
            rollingPolicy.context = loggerContext
            getDocsDirPath().existsAndCreateFolder()
            rollingPolicy.fileNamePattern = "${getDocsDirPath()}/$LOG_FILE_NAME_PATTERN"
            rollingPolicy.maxHistory = LOG_BACKUP_INDEX
            rollingPolicy.isCleanHistoryOnStart = true
            rollingPolicy.setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE))
            rollingPolicy.setParent(appender)  // parent and context required!
            rollingPolicy.start()

            // Assign the rolling policy to the appender
            appender.rollingPolicy = rollingPolicy
            appender.start()

            val logcatAppender: LogcatAppender = LogcatAppender()
            logcatAppender.context = loggerContext
            logcatAppender.encoder = encoder
            logcatAppender.start()

            // Configure Logback
            rootLogger.level = Level.ALL
            rootLogger.addAppender(appender)
            rootLogger.addAppender(logcatAppender)
        }
    }

    fun d(msg: String?) {
        if (!DEBUG) {
            return
        }
        rootLogger.debug(msg)
    }

    fun i(msg: String?) {
        if (!DEBUG) {
            return
        }
        rootLogger.info(msg)
    }

    fun w(msg: String?) {
        if (!DEBUG) {
            return
        }
        rootLogger.warn(msg)
    }

    fun e(msg: String?) {
        if (!DEBUG) {
            return
        }
        rootLogger.error(msg)
    }

    private fun getDocsDirPath(): String {
        if (!DEBUG) {
            return ""
        }
        return "${Environment.getExternalStorageDirectory()}/Documents/logs"
    }
}

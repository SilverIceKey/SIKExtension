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
object LogUtils {

    private const val LOG_FILE_NAME_PATTERN = "app_log.%d{yyyy-MM-dd}.%i.log"

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

    private val rootLogger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    }
    private val loggerContext = rootLogger.loggerContext
    private val encoder: PatternLayoutEncoder
    private val rollingPolicy: SizeAndTimeBasedRollingPolicy<ILoggingEvent>
    private val appender: RollingFileAppender<ILoggingEvent>
    private val logcatAppender:LogcatAppender

    init {
        BasicLogcatConfigurator.configureDefaultContext()

        // Pattern
        encoder = PatternLayoutEncoder()
        encoder.context = loggerContext
        encoder.pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
        encoder.start()

        // Appender
        appender = RollingFileAppender<ILoggingEvent>()
        appender.context = loggerContext
        appender.name = "FILE"
        appender.encoder = encoder

        // Rolling Policy
        rollingPolicy = SizeAndTimeBasedRollingPolicy()
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

        logcatAppender = LogcatAppender()
        logcatAppender.context = loggerContext
        logcatAppender.encoder = encoder
        logcatAppender.start()

        // Configure Logback
        rootLogger.level = Level.ALL
        rootLogger.addAppender(appender)
        rootLogger.addAppender(logcatAppender)
    }

    fun d(msg: String?) {
        rootLogger.debug(msg)
    }

    fun i(msg: String?) {
        rootLogger.info(msg)
    }

    fun w(msg: String?) {
        rootLogger.warn(msg)
    }

    fun e(msg: String?) {
        rootLogger.error(msg)
    }

    private fun getDocsDirPath(): String {
        return "${Environment.getExternalStorageDirectory()}/Documents/logs"
    }
}

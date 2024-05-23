package com.sik.sikcore.log

import android.os.Environment
import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.sik.sikcore.SIKCore
import com.sik.sikcore.extension.existsAndCreateFolder
import com.sik.sikcore.extension.folder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

/**
 * 日志工具类，支持通过实例化和注解的方式获取。
 */
class LogUtils(private val clazz: KClass<*>) {
    companion object {
        private const val LOG_FILE_NAME_PATTERN = "app_log.%d{yyyy-MM-dd}.%i.log"
        var DEBUG: Boolean = true

        @JvmStatic
        var LOG_BACKUP_INDEX = 7

        @JvmStatic
        var MAX_LOG_FILE_SIZE = "10MB"

        fun getLogger(clazz: KClass<*>): LogUtils = LogUtils(clazz)
    }

    private val logger: Logger by lazy {
        val logger = LoggerFactory.getLogger(clazz.java.simpleName) as Logger
        configureLogger(logger)
        logger
    }

    init {
        if (DEBUG) {
            BasicLogcatConfigurator.configureDefaultContext()
        }
    }

    private fun configureLogger(logger: Logger) {
        val lc = logger.loggerContext
//        lc.stop()

        val encoder = PatternLayoutEncoder().apply {
            context = lc
            pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
            start()
        }

        val fileAppender = RollingFileAppender<ILoggingEvent>().apply {
            isAppend = true
            context = lc
            start()
        }

        val rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>().apply {
            context = lc
            fileNamePattern = "${getLogsDirPath()}/$LOG_FILE_NAME_PATTERN"
            maxHistory = LOG_BACKUP_INDEX
            setParent(fileAppender)
            isCleanHistoryOnStart = true
            setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE))
            start()
        }

        fileAppender.apply {
            this.rollingPolicy = rollingPolicy
            this.encoder = encoder
            start()
        }

        val logcatAppender = LogcatAppender().apply {
            context = lc
            this.encoder = encoder
            start()
        }

        logger.apply {
            level = Level.ALL
            addAppender(fileAppender)
            addAppender(logcatAppender)
        }
    }

    fun d(msg: String?) = msg?.let { logger.debug(it) }
    fun i(msg: String?) = msg?.let { logger.info(it) }
    fun w(msg: String?) = msg?.let { logger.warn(it) }
    fun e(msg: String?) = msg?.let { logger.error(it) }

    fun copyLogFileToPublicStorage() {
        try {
            val sourceFile = File(getLogsDirPath(), "app_log_file.log") // 替换为实际日志文件名
            val publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!publicDirectory.exists()) publicDirectory.mkdirs()
            val destinationFile = File(publicDirectory, sourceFile.name)

            sourceFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.i("LogUtils", "Log file copied to ${destinationFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("LogUtils", "Failed to copy log file: ${e.message}")
        }
    }

    private fun getLogsDirPath(): String {
        val logsDirPath = "${SIKCore.getApplication().filesDir}/logs"
        return logsDirPath.folder().absolutePath
    }
}

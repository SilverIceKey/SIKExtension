package com.sik.sikcore.log

import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.sik.sikcore.SIKCore
import com.sik.sikcore.zip.ZipListener
import com.sik.sikcore.zip.ZipUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * 日志配置工具类，用于在运行时更新 logback 的配置，例如调整日志级别、修改输出模式等。
 */
object LogUtils {

    private const val TAG = "LogUtils"

    /**
     * 设置指定 logger 的日志级别
     * @param loggerName 日志器名称，使用全限定类名，例如 "com.example.MainActivity"
     * @param level 日志级别，例如 Level.DEBUG
     */
    fun setLogLevel(loggerName: String, level: Level) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(loggerName)
        logger.level = level
    }

    /**
     * 设置全局日志级别
     * @param level 日志级别，例如 Level.DEBUG
     */
    fun setGlobalLogLevel(level: Level) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        logger.level = level
    }

    /**
     * 获取指定 logger 的当前日志级别
     * @param loggerName 日志器名称
     * @return 日志级别
     */
    fun getLogLevel(loggerName: String): Level? {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(loggerName)
        return logger.level
    }

    /**
     * 动态更新日志输出模式（Pattern）
     * @param newPattern 新的日志输出模式，例如 "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
     */
    fun setPattern(newPattern: String) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        val appenders = logger.iteratorForAppenders()
        while (appenders.hasNext()) {
            val appender = appenders.next()
            val encoder = when (appender) {
                is FileAppender<*> -> appender.encoder
                is ch.qos.logback.classic.android.LogcatAppender -> appender.encoder
                else -> null
            }
            if (encoder is PatternLayoutEncoder) {
                encoder.stop() // 先停止当前的 encoder
                encoder.pattern = newPattern
                encoder.context = context
                encoder.start() // 重新启动 encoder 以应用新的模式
            }
        }
    }

    /**
     * 设置日志输出目录 主要是注册logback.xml的LOG_DIR
     * @param newLogDir 新的日志输出目录
     */
    fun setLogDir(newLogDir: String) {
        val logDir = File(newLogDir)
        if (!logDir.exists()) logDir.mkdirs()

        // 把 LOG_DIR 注入给 logback.xml
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        lc.putProperty("LOG_DIR", logDir.absolutePath)
    }

    /**
     * 动态更新日志输出路径
     * @param newFilePath 新的日志文件路径
     */
    fun setLogFilePath(newFilePath: String) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        val appenders = logger.iteratorForAppenders()
        while (appenders.hasNext()) {
            val appender = appenders.next()
            if (appender is FileAppender<*>) {
                appender.stop()
                appender.file = newFilePath
                appender.start()
            }
        }
    }

    /**
     * 获取当前日志输出路径
     * @return 日志文件路径，如果没有配置文件输出，返回 null
     */
    fun getLogFilePath(): String? {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        val appenders = logger.iteratorForAppenders()
        while (appenders.hasNext()) {
            val appender = appenders.next()
            if (appender is FileAppender<*>) {
                return appender.file
            }
        }
        return null
    }

    /**
     * 重新加载 logback.xml 配置文件
     */
    fun reloadConfiguration() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        try {
            val assetStream = SIKCore.getApplication().assets.open("logback.xml")
            configurator.doConfigure(assetStream)
            Log.i(TAG, "logback.xml 配置重新加载成功")
        } catch (je: JoranException) {
            Log.e(TAG, "加载 logback.xml 失败: ${je.message}")
        } catch (e: IOException) {
            Log.e(TAG, "读取 logback.xml 失败: ${e.message}")
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context)
    }

    /**
     * 获取指定日期的日志文件，并压缩成 zip 包
     * @param date 日期字符串，格式需与日志文件名中的日期部分一致
     * @param zipListener 压缩结果回调
     */
    fun getLogFileByDate(date: String, zipListener: ZipListener) {
        val logFilePath = getLogFilePath()
        if (logFilePath == null) {
            Log.e(TAG, "无法获取日志文件路径")
            zipListener.error("无法获取日志文件路径")
            return
        }

        val logDir = File(logFilePath).parentFile
        if (logDir == null || !logDir.exists() || !logDir.isDirectory) {
            Log.e(TAG, "日志目录不存在")
            zipListener.error("日志目录不存在")
            return
        }

        // 根据日志文件的命名格式匹配日期，例如 "app_log_2023-09-01.log"
        val matchingFiles = logDir.listFiles { _, name ->
            name.contains(date) && name.endsWith(".log")
        }

        if (matchingFiles.isNullOrEmpty()) {
            Log.e(TAG, "未找到指定日期的日志文件")
            zipListener.error("未找到指定日期的日志文件")
            return
        }

        val zipFileName = "logs_$date.zip"
        createZipFromFiles(matchingFiles, zipFileName, zipListener)
    }

    /**
     * 压缩日志文件
     */
    private fun createZipFromFiles(
        files: Array<File>,
        zipFileName: String,
        zipListener: ZipListener
    ) {
        val zipFile = File(files[0].parentFile, zipFileName)
        try {
            ZipUtils.zip(*files, destFile = zipFile, zipListener = zipListener)
            Log.i(TAG, "日志文件成功压缩为: ${zipFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "压缩日志文件失败: ${e.message}")
            zipListener.error("压缩日志文件失败: ${e.message}")
        }
    }
}

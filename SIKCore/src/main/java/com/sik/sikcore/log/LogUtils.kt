package com.sik.sikcore.log

import android.os.Environment
import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.sik.sikcore.SIKCore
import com.sik.sikcore.explain.AnnotationScanner
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.extension.folder
import com.sik.sikcore.zip.ZipListener
import com.sik.sikcore.zip.ZipUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

/**
 * 日志工具类，支持通过实例化和注解的方式获取。
 */
class LogUtils(private val clazz: KClass<*>) {
    companion object {
        private const val LOG_FILE_NAME_PATTERN = "log.%d{yyyy-MM-dd}.%i.log"

        @JvmStatic
        var DEBUG: Boolean = true

        @JvmStatic
        var LOG_BACKUP_INDEX = 7

        @JvmStatic
        var MAX_LOG_FILE_SIZE = "10MB"

        @JvmStatic
        var LOG_LEVEL = Level.ALL

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
            level = LOG_LEVEL
            addAppender(fileAppender)
            addAppender(logcatAppender)
            isAdditive = false // 确保不继承父类的 Appender
        }
    }

    fun d(msg: String?) = msg?.let {
        if (DEBUG) {
            logger.debug(it)
        }
    }

    fun i(msg: String?) = msg?.let {
        AnnotationScanner.getDescription(it)?.let { description ->
            logger.info(description)
        }
        logger.info(it)
    }

    fun i(clazz: KClass<*>) {
        clazz.annotations.find { it is LogInfo }?.let {
            logger.info((it as LogInfo).description)
        }
    }

    fun w(msg: String?) = msg?.let { logger.warn(it) }
    fun e(msg: String?) = msg?.let { logger.error(it) }

    fun copyLogFileToPublicStorage() {
        try {
            val sourceFolder = File(getLogsDirPath()) // 替换为实际日志文件名
            val publicDirectory =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + File.separator + "log")
            if (!publicDirectory.exists()) publicDirectory.mkdirs()
            sourceFolder.listFiles()?.forEach {
                val destinationFile = File(publicDirectory, it.name)
                if (!destinationFile.exists()) {
                    destinationFile.createNewFile()
                }
                it.inputStream().use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Log.i("LogUtils", "日志文件夹下的日志已经复制到${sourceFolder.absolutePath}")
        } catch (e: IOException) {
            Log.e("LogUtils", "复制日志文件失败: ${e.message}")
        }
    }

    private fun getLogsDirPath(): String {
        val logsDirPath = "${SIKCore.getApplication().filesDir}/logs"
        return logsDirPath.folder().absolutePath
    }

    /**
     * 根据日期获取日志压缩包
     */
    fun getLogFileByDate(date: String, zipListener: ZipListener) {
        val logDir = File(getLogsDirPath())
        if (!logDir.exists() || !logDir.isDirectory) {
            logger.error("日志目录不存在")
            zipListener.error("日志目录不存在")
            return
        }

        // 日期格式：yyyy-MM-dd（需与日志文件命名格式一致）
        val targetDate = "log.$date"
        val matchingFiles = logDir.listFiles { _, name ->
            name.contains(targetDate) && name.endsWith(".log")
        }

        if (matchingFiles.isNullOrEmpty()) {
            logger.error("未找到指定日期的日志文件")
            zipListener.error("未找到指定日期的日志文件")
            return
        }

        createZipFromFiles(matchingFiles, "logs_$date.zip", zipListener)
    }

    /**
     * 压缩日志文件
     */
    private fun createZipFromFiles(
        files: Array<File>,
        zipFileName: String,
        zipListener: ZipListener
    ) {
        val zipFile = File(getLogsDirPath(), zipFileName)
        try {
            ZipUtils.zip(*files, destFile = zipFile, zipListener = zipListener)
            logger.info("日志文件成功压缩为: ${zipFile.absolutePath}")
        } catch (e: IOException) {
            logger.error("压缩日志文件失败: ${e.message}")
        }
    }
}

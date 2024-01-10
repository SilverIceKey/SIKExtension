package com.sik.sikcore.zip

import android.util.Log
import com.sik.sikcore.extension.file
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 压缩包工具类
 */
class ZipUtils private constructor(
    /**
     * 压缩源文件
     */
    private var zipSrcFiles: MutableList<File> = mutableListOf(),
    /**
     * 压缩目标文件
     */
    private var zipDestFile: File? = null,
    /**
     * 解压缩源文件
     */
    private var unzipSrcFile: File? = null,
    /**
     * 解压缩目标目录
     */
    private var unzipDestFolder: File? = null,
    /**
     * 压缩和解压缩监听
     */
    private var zipListener: ZipListener
) {
    companion object {
        /**
         * 压缩文件
         *
         * @param srcFiles 源文件列表
         * @param destFile 目标文件
         * @param zipListener 压缩监听
         */
        @JvmStatic
        fun zip(vararg srcFiles: File, destFile: File, zipListener: ZipListener? = null) {
            ZipUtils(
                zipSrcFiles = srcFiles.toMutableList(),
                zipDestFile = destFile,
                zipListener = zipListener ?: object : ZipListener {
                    override fun zipEnd(file: File) {
                        Log.i("ZipUtils", "压缩完成")
                    }

                    override fun unzipEnd(fileList: MutableList<File>) {
                        Log.i("ZipUtils", "解压完成")
                    }

                    override fun error(errorMsg: String) {
                        Log.i("ZipUtils", "错误:$errorMsg")
                    }

                    override fun progress(progress: Float) {
                        Log.i("ZipUtils", "$progress")
                    }

                }).zip()
        }

        /**
         * 压缩文件
         *
         * @param srcFiles 源文件列表
         * @param destFile 目标文件
         * @param zipListener 压缩监听
         */
        @JvmStatic
        fun zip(vararg srcFiles: String, destFile: String, zipListener: ZipListener? = null) {
            if (srcFiles.isEmpty()) {
                zipListener?.error("源文件为空")
                return
            }
            ZipUtils(
                zipSrcFiles = mutableListOf<File>().apply {
                    for (srcFile in srcFiles) {
                        val tempFile = srcFile.file()
                        if (tempFile != null && tempFile.exists()) {
                            add(tempFile)
                        }
                    }
                },
                zipDestFile = destFile.file(),
                zipListener = zipListener ?: object : ZipListener {
                    override fun zipEnd(file: File) {
                        Log.i("ZipUtils", "压缩完成")
                    }

                    override fun unzipEnd(fileList: MutableList<File>) {
                        Log.i("ZipUtils", "解压完成")
                    }

                    override fun error(errorMsg: String) {
                        Log.i("ZipUtils", "错误:$errorMsg")
                    }

                    override fun progress(progress: Float) {
                        Log.i("ZipUtils", "$progress")
                    }

                }).zip()
        }

        /**
         * 解压文件
         *
         * @param srcFile 源文件
         * @param destFolder 目标目录
         * @param zipListener 解压监听
         */
        @JvmStatic
        fun unzip(srcFile: File, destFolder: File, zipListener: ZipListener? = null) {
            ZipUtils(
                unzipSrcFile = srcFile,
                unzipDestFolder = destFolder,
                zipListener = zipListener ?: object : ZipListener {
                    override fun zipEnd(file: File) {
                        Log.i("ZipUtils", "压缩完成")
                    }

                    override fun unzipEnd(fileList: MutableList<File>) {
                        Log.i("ZipUtils", "解压完成")
                    }

                    override fun error(errorMsg: String) {
                        Log.i("ZipUtils", "错误:$errorMsg")
                    }

                    override fun progress(progress: Float) {
                        Log.i("ZipUtils", "$progress")
                    }

                }).unzip()
        }

        /**
         * 解压文件
         *
         * @param srcFile 源文件
         * @param destFolder 目标目录
         * @param zipListener 解压监听
         */
        @JvmStatic
        fun unzip(srcFile: String, destFolder: String, zipListener: ZipListener? = null) {
            val tempFile = srcFile.file()
            if (tempFile == null) {
                zipListener?.error("源文件为空")
                return
            }
            val tempDestFolder =
                if (!destFolder.endsWith(File.separator)) {
                    destFolder + File.separator
                } else {
                    destFolder
                }
            ZipUtils(
                unzipSrcFile = tempFile,
                unzipDestFolder = tempDestFolder.file(),
                zipListener = zipListener ?: object : ZipListener {
                    override fun zipEnd(file: File) {
                        Log.i("ZipUtils", "压缩完成")
                    }

                    override fun unzipEnd(fileList: MutableList<File>) {
                        Log.i("ZipUtils", "解压完成")
                    }

                    override fun error(errorMsg: String) {
                        Log.i("ZipUtils", "错误:$errorMsg")
                    }

                    override fun progress(progress: Float) {
                        Log.i("ZipUtils", "$progress")
                    }

                }).unzip()
        }
    }

    /**
     * 压缩文件
     *
     */
    fun zip() {
        val totalSize = zipSrcFiles.sumOf { it.length().toDouble() }
        var processedSize = 0.0
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipDestFile))).use { out ->
                for (srcFile in zipSrcFiles) {
                    FileInputStream(srcFile).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(srcFile.name)
                            out.putNextEntry(entry)
                            origin.copyTo(out)

                            processedSize += srcFile.length().toDouble()
                            val progress = (processedSize / totalSize * 100).toFloat()
                            zipListener.progress(progress)
                        }
                    }
                }
            }
            zipListener.zipEnd(zipDestFile!!)
        } catch (e: Exception) {
            zipListener.error(e.stackTraceToString())
        }
    }

    /**
     * 解压文件
     *
     */
    fun unzip() {
        val totalSize = unzipSrcFile!!.length().toDouble()
        var processedSize = 0.0
        val unzippedFiles = mutableListOf<File>()
        if (unzipDestFolder?.exists() == false) {
            unzipDestFolder?.mkdirs()
        }
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(unzipSrcFile))).use { zis ->
                var entry: ZipEntry?
                while (zis.nextEntry.also { entry = it } != null) {
                    val outputFile = File(unzipDestFolder, entry!!.name)
                    unzippedFiles.add(outputFile)
                    BufferedOutputStream(FileOutputStream(outputFile)).use { dest ->
                        zis.copyTo(dest)

                        processedSize += entry!!.compressedSize.toDouble()
                        val progress = (processedSize / totalSize * 100).toFloat()
                        zipListener.progress(progress)
                    }
                }
            }
            zipListener.unzipEnd(unzippedFiles)
        } catch (e: Exception) {
            zipListener.error(e.stackTraceToString())
        }
    }
}
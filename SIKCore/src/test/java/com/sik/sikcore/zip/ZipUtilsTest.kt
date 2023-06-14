package com.sik.sikcore.zip

import org.junit.Test
import java.io.File

internal class ZipUtilsTest {

    @Test
    fun zip() {
        val srcFiles = mutableListOf<String>()
        for (i in 0..90) {
            srcFiles.add("D:\\Project\\Java\\ImageCreate\\design\\circle${i}.png")
        }
        ZipUtils.zip(srcFiles = srcFiles.toTypedArray(),
            destFile = "D:\\Project\\Java\\ImageCreate\\design\\circle.zip",
            zipListener = object : ZipListener {
                override fun zipEnd(file: File) {
                    println("压缩完成:$file")
                }

                override fun unzipEnd(fileList: MutableList<File>) {

                }

                override fun error(errorMsg: String) {
                    println("错误:$errorMsg")
                }

                override fun progress(progress: Float) {
                    println("$progress")
                }

            })
    }

    @Test
    fun unzip() {
        ZipUtils.unzip(srcFile = "D:\\Project\\Java\\ImageCreate\\design\\circle.zip",
            destFolder = "D:\\Project\\Java\\ImageCreate\\design\\circle\\",
            zipListener = object : ZipListener {
                override fun zipEnd(file: File) {

                }

                override fun unzipEnd(fileList: MutableList<File>) {
                    println("解压完成:${fileList.map { it.name }}")
                }

                override fun error(errorMsg: String) {
                    println("错误:$errorMsg")
                }

                override fun progress(progress: Float) {
                    println("$progress")
                }

            })
    }

}
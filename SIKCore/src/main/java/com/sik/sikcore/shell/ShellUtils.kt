package com.sik.sikcore.shell

import android.text.TextUtils
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader


/**
 * 命令行工具类
 */
object ShellUtils {
    private val LINE_SEP = System.getProperty("line.separator")

    /**
     * 执行命令
     */
    @JvmOverloads
    fun execCmd(
        command: String,
        isRoot: Boolean = false,
        env: Array<String> = arrayOf()
    ): ShellResult {
        val successMsg: StringBuilder = StringBuilder()
        val errorMsg: StringBuilder = StringBuilder()
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        var os: DataOutputStream? = null
        var process: Process? = null
        var resultCode = -1
        try {
            process = Runtime.getRuntime().exec(if (isRoot) "su" else "sh", env, null)
            os = DataOutputStream(process.outputStream)
            if (TextUtils.isEmpty(command)) {
                return ShellResult(resultCode, "", "命令为空")
            }
            os.write(command.toByteArray())
            os.writeBytes(LINE_SEP)
            os.flush()
            os.writeBytes("exit$LINE_SEP")
            os.flush()
            resultCode = process.waitFor()
            successResult = BufferedReader(
                InputStreamReader(process.inputStream, "UTF-8")
            )
            errorResult = BufferedReader(
                InputStreamReader(process.errorStream, "UTF-8")
            )
            var line: String? = ""
            line = successResult.readLine()
            if (line != null) {
                successMsg.append(line)
                line = successResult.readLine()
                while (line != null) {
                    successMsg.append("\n").append(line)
                    line = successResult.readLine()
                }
            }
            line = errorResult.readLine()
            if (line != null) {
                errorMsg.append(line)
                line = errorResult.readLine()
                while (line != null) {
                    errorMsg.append("\n").append(line)
                    line = errorResult.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                successResult?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                errorResult?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            process?.destroy()
        }
        return ShellResult(resultCode, successMsg.toString(), errorMsg.toString())
    }
}
package com.sik.sikcore.shell

/**
 * 命令结果
 */
data class ShellResult(val code: Int = 0, val successMsg: String = "", val errorMsg: String = "")

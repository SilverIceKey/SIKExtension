package com.sik.skextensionsample

import com.sik.sikcore.explain.LogInfo

object ObjectDemo {
    @field:LogInfo("调用了一个测试的字段")
    val demoField = "这是一个测试的字段"
}
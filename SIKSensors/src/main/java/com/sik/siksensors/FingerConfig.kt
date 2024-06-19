package com.sik.siksensors

import android.content.DialogInterface

/**
 * 指纹识别配置
 *
 */
abstract class FingerConfig {
    /**
     * 指纹识别标题
     */
    var title: String = "指纹识别"

    /**
     * 指纹识别描述
     */
    var description: String = "进行指纹识别"

    /**
     * 取消按钮文本
     */
    var negativeButtonTxt: String = "取消"

    /**
     * 取消按钮事件
     */
    var listener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }

    companion object {
        /**
         * 默认配置
         */
        @JvmStatic
        val defaultConfig
            get() = object : FingerConfig() {}
    }
}
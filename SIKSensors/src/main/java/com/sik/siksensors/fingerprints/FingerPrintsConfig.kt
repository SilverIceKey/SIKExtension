package com.sik.siksensors.fingerprints

import android.content.DialogInterface

/**
 * 指纹识别配置
 *
 */
abstract class FingerPrintsConfig() {
    /**
     * 指纹识别标题
     */
    var title: String = "指纹识别"

    /**
     * 指纹识别描述
     */
    var description: String = "请将您的手指放在指纹传感器上。"

    /**
     * 取消按钮文本
     */
    var negativeButtonTxt: String = "取消"

    /**
     * 取消按钮事件
     */
    var listener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }

    /**
     * 使用系统弹窗
     */
    var useSystemDialog: Boolean = true

    companion object {
        /**
         * 默认配置
         */
        @JvmStatic
        val defaultConfig: DefaultFingerPrintsConfig
            get() = DefaultFingerPrintsConfig()

    }

    class DefaultFingerPrintsConfig : FingerPrintsConfig() {

    }
}
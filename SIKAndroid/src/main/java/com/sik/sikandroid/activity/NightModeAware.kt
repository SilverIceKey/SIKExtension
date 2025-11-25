package com.sik.sikandroid.activity

/**
 * 黑暗模式提醒监听
 */
interface NightModeAware {
    /**
     * 当夜间模式状态发生变化时调用
     *
     * @param mode 当前夜间模式状态
     */
    fun onNightModeChanged(mode: Int)
}

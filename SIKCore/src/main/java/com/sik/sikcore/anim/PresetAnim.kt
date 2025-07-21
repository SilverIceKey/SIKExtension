package com.sik.sikcore.anim

import android.view.Gravity

/**
 * 动画预设与扩展工具类
 */
object PresetAnim {

    /**
     * 渐入动画
     */
    fun fadeIn(duration: Long = 700L, maxAlpha: Float = 1f): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            this.duration = duration
            this.maxAlpha = maxAlpha
        }
    }

    /**
     * 渐出动画
     */
    fun fadeOut(duration: Long = 700L, maxAlpha: Float = 1f): AnimConfig {
        return AnimConfig().apply {
            isIn = false
            this.duration = duration
            this.maxAlpha = maxAlpha
        }
    }

    /**
     * 从底部滑入
     */
    fun slideInFromBottom(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            gravity = Gravity.BOTTOM
            this.duration = duration
        }
    }

    /**
     * 从底部滑出
     */
    fun slideOutToBottom(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = false
            gravity = Gravity.BOTTOM
            this.duration = duration
        }
    }

    /**
     * 从左侧滑入
     */
    fun slideInFromLeft(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            gravity = Gravity.LEFT
            this.duration = duration
        }
    }

    /**
     * 从左侧滑出
     */
    fun slideOutToLeft(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = false
            gravity = Gravity.LEFT
            this.duration = duration
        }
    }

    /**
     * 从右侧滑入
     */
    fun slideInFromRight(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            gravity = Gravity.RIGHT
            this.duration = duration
        }
    }

    /**
     * 从左侧滑出
     */
    fun slideOutToRight(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = false
            gravity = Gravity.RIGHT
            this.duration = duration
        }
    }

    /**
     * 从上边滑入
     */
    fun slideInFromTop(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            gravity = Gravity.TOP
            this.duration = duration
        }
    }

    /**
     * 从上边滑出
     */
    fun slideOutToTop(duration: Long = 700L): AnimConfig {
        return AnimConfig().apply {
            isIn = false
            gravity = Gravity.TOP
            this.duration = duration
        }
    }

    /**
     * 弹性放大动画（类似弹窗）
     */
    fun popIn(duration: Long = 800L, rotation: Float = 0f): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            enableAlpha = true
            enableSlide = false
            enableRotation = rotation != 0f
            this.rotation = rotation
            setInterpolator(EffectInterpolatorType.Overshoot)
            this.duration = duration
        }
    }

    /**
     * 弹性滑入 + 淡入（从右侧）
     */
    fun bounceInRight(duration: Long = 800L): AnimConfig {
        return AnimConfig().apply {
            isIn = true
            gravity = Gravity.RIGHT
            maxAlpha = 1f
            setInterpolator(EffectInterpolatorType.Bounce)
            this.duration = duration
        }
    }
}
package com.sik.sikcore.anim

import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Anim config
 * 动画配置
 * @constructor Create empty Anim config
 */
class AnimConfig {
    /**
     * Interpolator
     * 插值器
     */
    private var interpolator: Interpolator = LinearInterpolator()

    /**
     * Enable slide
     * 启用移动包括上下左右
     */
    var enableSlide: Boolean = false

    /**
     * Gravity
     * 从哪边进入或者从那边出去，默认不设置<P>
     * 只支持上下左右以及上下和左右拼接
     */
    var gravity: Int = Gravity.NO_GRAVITY
        set(value) {
            field = value
            enableSlide = true
        }

    /**
     * Start point
     * 宽高相对于控件的偏移量，默认为1个控件宽高
     */
    var offset: Pair<Float, Float> = Pair(-1f, -1f)

    /**
     * Enable alpha
     * 启用渐入渐出动画
     */
    var enableAlpha: Boolean = false

    /**
     * Max alpha
     * 最大透明度，默认完全显示
     */
    var maxAlpha: Float = 1f
        set(value) {
            field = value
            enableAlpha = true
        }

    /**
     * Enable rotation
     * 启用旋转动画
     */
    var enableRotation: Boolean = false

    /**
     * Rotation
     * 旋转角度
     */
    var rotation: Float = 0f
        set(value) {
            field = value
            enableRotation = true
        }

    /**
     * Duration
     * 动画时长
     */
    var duration: Long = 1000L

    /**
     * Is in
     * 是否为进入动画
     */
    var isIn: Boolean = true

    /**
     * Set interpolator
     * 设置动画插值器
     */
    fun setInterpolator(effectInterpolatorType: EffectInterpolatorType) {
        interpolator = when (effectInterpolatorType) {
            EffectInterpolatorType.Linear -> LinearInterpolator()
            EffectInterpolatorType.Accelerate -> AccelerateInterpolator()
            EffectInterpolatorType.Decelerate -> DecelerateInterpolator()
            EffectInterpolatorType.AccelerateDecelerate -> AccelerateDecelerateInterpolator()
            EffectInterpolatorType.Anticipate -> AnticipateInterpolator()
            EffectInterpolatorType.Overshoot -> OvershootInterpolator()
            EffectInterpolatorType.AnticipateOvershoot -> AnticipateOvershootInterpolator()
            EffectInterpolatorType.Bounce -> BounceInterpolator()
        }
    }

    /**
     * Set interpolator
     * 设置动画插值器
     */
    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    /**
     * Get interpolator
     * 获取插值器
     * @return
     */
    fun getInterpolator(): Interpolator {
        return interpolator
    }
}
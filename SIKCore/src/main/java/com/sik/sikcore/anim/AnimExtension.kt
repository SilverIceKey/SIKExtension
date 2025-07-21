package com.sik.sikcore.anim

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.View

/**
 * Anim
 * 获取动画
 * @param animConfig 动画配置
 * @param createStart 创建完成之后马上执行
 * @return
 */
fun View.anim(animConfig: AnimConfig, createStart: Boolean = true): ValueAnimator {
    val isIn = animConfig.isIn
    val startValue = if (isIn) 0f else 1f
    val endValue = if (isIn) 1f else 0f

    val offsetX = if (animConfig.offset.first == -1f) width.toFloat() else animConfig.offset.first
    val offsetY = if (animConfig.offset.second == -1f) height.toFloat() else animConfig.offset.second

    if (animConfig.enableSlide) {
        if (animConfig.gravity and Gravity.TOP == Gravity.TOP) {
            translationY = if (isIn) -offsetY else 0f
        }
        if (animConfig.gravity and Gravity.BOTTOM == Gravity.BOTTOM) {
            translationY = if (isIn) offsetY else 0f
        }
        if (animConfig.gravity and Gravity.LEFT == Gravity.LEFT) {
            translationX = if (isIn) -offsetX else 0f
        }
        if (animConfig.gravity and Gravity.RIGHT == Gravity.RIGHT) {
            translationX = if (isIn) offsetX else 0f
        }
    }

    if (animConfig.enableAlpha) {
        alpha = if (isIn) 0f else animConfig.maxAlpha
    }

    if (animConfig.enableRotation) {
        rotation = if (isIn) animConfig.rotation else 0f
    }

    val valueAnimator = ValueAnimator.ofFloat(startValue, endValue).apply {
        duration = animConfig.duration
        interpolator = animConfig.getInterpolator()

        addUpdateListener { animation ->
            val value = animation.animatedValue as Float

            // 滑动动画
            if (animConfig.enableSlide) {
                if (animConfig.gravity and Gravity.TOP == Gravity.TOP) {
                    translationY = if (isIn) -offsetY * (1 - value) else -offsetY * value
                }
                if (animConfig.gravity and Gravity.BOTTOM == Gravity.BOTTOM) {
                    translationY = if (isIn) offsetY * (1 - value) else offsetY * value
                }
                if (animConfig.gravity and Gravity.LEFT == Gravity.LEFT) {
                    translationX = if (isIn) -offsetX * (1 - value) else -offsetX * value
                }
                if (animConfig.gravity and Gravity.RIGHT == Gravity.RIGHT) {
                    translationX = if (isIn) offsetX * (1 - value) else offsetX * value
                }
            }

            // 透明度动画
            if (animConfig.enableAlpha) {
                alpha = if (isIn) value * animConfig.maxAlpha else (1f - value) * animConfig.maxAlpha
            }

            // 旋转动画
            if (animConfig.enableRotation) {
                rotation = if (isIn) animConfig.rotation * value else animConfig.rotation * (1 - value)
            }
        }

        if (createStart) {
            start()
        }
    }

    return valueAnimator
}

/**
 * 支持 DSL 的调用方式：
 * ```kotlin
 * view.anim { fadeIn() }
 * ```
 */
fun View.anim(builder: PresetAnim.() -> AnimConfig): ValueAnimator {
    return this.anim(PresetAnim.builder())
}

/**
 * 安全执行动画
 */
fun View.animSafely(animConfig: AnimConfig, createStart: Boolean = true) {
    if (width == 0 || height == 0) {
        // 延迟执行，确保宽高可用
        post { anim(animConfig, createStart) }
    } else {
        anim(animConfig, createStart)
    }
}

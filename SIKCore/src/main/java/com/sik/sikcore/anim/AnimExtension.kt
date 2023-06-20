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
    val valueAnimator = ValueAnimator.ofFloat(
        if (animConfig.isIn) 1.0f else 0.0f,
        if (animConfig.isIn) 0.0f else 1.0f
    ).apply {
        //设置动画时间
        duration = animConfig.duration
        addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            // 是否滑入滑出
            if (animConfig.enableSlide) {
                if (animConfig.gravity.and(Gravity.TOP) == Gravity.TOP) {
                    translationY =
                        (if (animConfig.offset.second == -1f) height.toFloat() else animConfig.offset.second) *
                                value * -1
                }
                if (animConfig.gravity.and(Gravity.BOTTOM) == Gravity.BOTTOM) {
                    translationY =
                        (if (animConfig.offset.second == -1f) height.toFloat() else animConfig.offset.second) * value
                }
                if (animConfig.gravity.and(Gravity.LEFT) == Gravity.LEFT) {
                    translationX =
                        (if (animConfig.offset.second == -1f) width.toFloat() else animConfig.offset.first) * value * -1
                }
                if (animConfig.gravity.and(Gravity.RIGHT) == Gravity.RIGHT) {
                    translationX =
                        (if (animConfig.offset.second == -1f) width.toFloat() else animConfig.offset.first) * value
                }
            }
            //是否渐入渐出
            if (animConfig.enableAlpha) {
                alpha = animConfig.maxAlpha - value
            }
            // 是否旋转
            if (animConfig.enableRotation) {
                rotation = animConfig.rotation * value
            }
            // 根据不同的回弹效果设置不同的插值器
            interpolator = animConfig.getInterpolator()
        }
        if (createStart) {
            start()
        }
    }
    return valueAnimator
}

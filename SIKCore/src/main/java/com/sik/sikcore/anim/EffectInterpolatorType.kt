package com.sik.sikcore.anim

/**
 * Bounce effect
 * 回弹动画
 * @constructor Create empty Bounce effect
 */
enum class EffectInterpolatorType {
    /**
     * Linear
     * 线性插值器
     * 速率恒定
     * @constructor Create empty Linear
     */
    Linear,

    /**
     * Accelerate
     * 在动画开始的地方速率改变比较慢，然后开始加速
     * @constructor Create empty Accelerate
     */
    Accelerate,

    /**
     * Decelerate
     * 在动画开始的地方速率改变比较快，然后速度慢慢减速
     * @constructor Create empty Decelerate
     */
    Decelerate,

    /**
     * AccelerateDecelerate
     *
     * @constructor Create empty Gravity
     */
    AccelerateDecelerate,

    /**
     * Anticipate
     * 开始的时候向后然后向前甩
     * @constructor Create empty Anticipate
     */
    Anticipate,

    /**
     * Overshoot
     * 开始的时候向后然后向前甩一定值后返回最后的值
     * @constructor Create empty Overshoot
     */
    Overshoot,

    /**
     * Anticipate overshoot
     * 开始的时候向后然后向前甩一定值后返回最后的值
     * @constructor Create empty Anticipate overshoot
     */
    AnticipateOvershoot,
    /**
     * Bounce
     * 模拟弹球效果，速率随时间先快后慢
     * @constructor Create empty Jelly
     */
    Bounce,
}
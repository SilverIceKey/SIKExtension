package com.sik.sikandroid.activity

@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME) // 确保注解在运行时可用
@MustBeDocumented
annotation class NightModeChangeListener()

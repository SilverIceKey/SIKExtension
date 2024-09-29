package com.sik.sikcore.activity

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME) // 确保注解在运行时可用
@MustBeDocumented
annotation class SecureActivity()

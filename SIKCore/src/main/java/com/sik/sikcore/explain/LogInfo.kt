package com.sik.sikcore.explain

/**
 * 日志输出注解
 */
@Target(
    AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME) // 确保注解在运行时可用
@MustBeDocumented
annotation class LogInfo(val description: String)


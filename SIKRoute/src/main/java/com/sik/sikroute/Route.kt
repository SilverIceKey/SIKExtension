package com.sik.sikroute

/**
 * 导航注解
 * @param isStart 是否为起始导航
 * @param name 导航名称
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Route(val name: String, val params: Array<String> = [], val isStart: Boolean = false)

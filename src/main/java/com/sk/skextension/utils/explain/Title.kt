package com.sk.skextension.utils.explain

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
/**
 * 类标题
 */
annotation class Title(val titleValue: String = "标题")

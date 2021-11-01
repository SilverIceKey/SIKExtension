package com.sk.skextension.utils.explain

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
/**
 * 属性介绍说明
 */
annotation class Explain(val explainValue:String = "未知")

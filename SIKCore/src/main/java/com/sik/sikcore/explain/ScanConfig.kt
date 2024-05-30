package com.sik.sikcore.explain

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ScanConfig(vararg val classesToScan: KClass<*>)


package com.sik.sikcore.explain.gerneration

/**
 * 注解配置类
 */
abstract class EntitiesGenerationConfig {
    init {
        GenerationHelper.generateDescriptionWithConfig(this.javaClass)
    }
}
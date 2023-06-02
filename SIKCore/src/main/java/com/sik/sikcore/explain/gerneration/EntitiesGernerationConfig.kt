package com.sik.sikcore.explain.gerneration

/**
 * 注解配置类
 */
abstract class EntitiesGernerationConfig {
    init {
        GernerationHelper.gerneratieDescriptionWithConfig(this.javaClass)
    }
}
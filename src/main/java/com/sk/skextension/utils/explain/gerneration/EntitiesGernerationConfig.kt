package com.sik.edg.utils.gerneration

/**
 * 注解配置类
 */
abstract class EntitiesGernerationConfig {
    init {
        GernerationHelper.gerneratieDescriptionWithConfig(this.javaClass)
    }
}
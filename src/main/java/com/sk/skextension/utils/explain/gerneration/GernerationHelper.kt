package com.sik.edg.utils.gerneration

import com.alibaba.fastjson.JSONObject
import com.sik.edg.entities.MaterialFavoritesDo
import com.sik.edg.utils.explain.Entities
import com.sik.edg.utils.explain.ExplainUtils
import java.io.File

/**
 * 生成工具类
 */
object GernerationHelper {
    val JsonFile = ".\\entities\\"

    /**
     * 根据配置类生成实体描述
     */
    @JvmStatic
    fun <T : EntitiesGernerationConfig> gerneratieDescriptionWithConfig(clazz: Class<T>) {
        val entities = clazz.getAnnotation(Entities::class.java)
        for (entity in entities.entities) {
            gernerateDescription(entity.java)
        }
    }

    /**
     * 生成实体描述
     */
    @JvmStatic
    private fun gernerateDescription(clazz: Class<*>) {
        if (!File(JsonFile).exists()){
            File(JsonFile).mkdirs()
        }
        val os = File(JsonFile + clazz.simpleName + ".txt").outputStream()
        val descriptionJson = JSONObject()
        descriptionJson["title"] = ExplainUtils.getClassTitle(clazz)
        descriptionJson["description"] = ExplainUtils.getClassDescription(clazz)
        descriptionJson["type"] = "object"
        descriptionJson["properties"] = ExplainUtils.getExplainValuesToJson(clazz)
        descriptionJson["required"] = ExplainUtils.getRequiredField(clazz)
        os.write(descriptionJson.toJSONString().toByteArray())
        os.flush()
        os.close()
    }
}
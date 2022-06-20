package com.sk.skextension.utils.explain.gerneration

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sik.edg.utils.explain.ExplainUtils
import com.sk.skextension.utils.explain.Entities
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
        if (!File(JsonFile).exists()) {
            File(JsonFile).mkdirs()
        }
        val os = File(JsonFile + clazz.simpleName + ".txt").outputStream()
        val descriptionJson = JsonObject()
        descriptionJson.addProperty("title", ExplainUtils.getClassTitle(clazz))
        descriptionJson.addProperty("description", ExplainUtils.getClassDescription(clazz))
        descriptionJson.addProperty("type", "object")
        descriptionJson.add("properties", ExplainUtils.getExplainValuesToJson(clazz))
        descriptionJson.addProperty("required", ExplainUtils.getRequiredField(clazz))
        os.write(Gson().toJson(descriptionJson).toByteArray())
        os.flush()
        os.close()
    }
}
package io.projectenv.intellijplugin

import com.google.gson.reflect.TypeToken
import io.projectenv.commons.gson.GsonFactory

object ToolInfoParser {
    private val TOOL_INFOS_TYPE = object : TypeToken<Map<String, List<ToolInfo>>>() {}.type
    fun fromJson(rawToolInfos: String): Map<String, List<ToolInfo>> {
        return GsonFactory.createGson().fromJson(rawToolInfos, TOOL_INFOS_TYPE)
    }
}

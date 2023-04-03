package io.projectenv.intellijplugin.toolinfo

import com.google.gson.reflect.TypeToken
import io.projectenv.commons.gson.GsonFactory
import io.projectenv.core.toolsupport.spi.ToolInfo

object ToolInfoParser {
    private val TOOL_INFOS_TYPE = object : TypeToken<Map<String, List<ToolInfo>>>() {}.type
    fun fromJson(rawToolInfos: String): ToolInfos {
        return ToolInfos(GsonFactory.createGson().fromJson(rawToolInfos, TOOL_INFOS_TYPE))
    }
}

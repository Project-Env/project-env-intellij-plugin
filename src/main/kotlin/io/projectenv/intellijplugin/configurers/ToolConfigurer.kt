package io.projectenv.intellijplugin.configurers

import com.intellij.openapi.Disposable
import io.projectenv.core.tools.info.ToolInfo

interface ToolConfigurer<ToolInfoType : ToolInfo> : Disposable {

    fun supportsType(toolInfo: ToolInfo): Boolean

    fun configureTool(toolInfo: ToolInfoType)

    override fun dispose() {
        // noop
    }
}

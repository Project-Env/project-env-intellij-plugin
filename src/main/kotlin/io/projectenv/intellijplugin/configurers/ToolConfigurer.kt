package io.projectenv.intellijplugin.configurers

import com.intellij.openapi.Disposable
import io.projectenv.intellijplugin.ToolInfo

interface ToolConfigurer : Disposable {

    fun getToolIdentifier(): String

    fun configureTool(toolInfo: ToolInfo)

    override fun dispose() {
        // noop
    }
}

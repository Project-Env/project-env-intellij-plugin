package io.projectenv.intellijplugin.configurers

import com.intellij.openapi.Disposable
import io.projectenv.core.cli.api.ToolInfo

interface ToolConfigurer : Disposable {

    fun getToolIdentifier(): String

    fun configureTool(toolInfo: ToolInfo)

    override fun dispose() {
        // noop
    }
}

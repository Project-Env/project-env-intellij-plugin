package ch.repolevedavaj.projectenv.intellijplugin.configurers

import ch.projectenv.core.toolinfo.ToolInfo
import com.intellij.openapi.Disposable

interface ToolConfigurer<ToolInfoType : ToolInfo> : Disposable {

    fun supportsType(toolInfo: ToolInfo): Boolean

    fun configureTool(toolInfo: ToolInfoType)

    override fun dispose() {
        // noop
    }
}

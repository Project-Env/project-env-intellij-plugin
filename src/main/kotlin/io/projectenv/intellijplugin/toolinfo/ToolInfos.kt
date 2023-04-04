package io.projectenv.intellijplugin.toolinfo

import io.projectenv.core.commons.process.ProcessEnvironmentHelper
import io.projectenv.core.toolsupport.spi.ToolInfo
import java.io.File
import java.util.Collections

class ToolInfos(val allToolInfos: Map<String, List<ToolInfo>>) {

    val toolEnvironment: Map<String, String> = createToolEnvironment()

    fun getToolInfo(toolIdentifier: String): ToolInfo? {
        return allToolInfos.getOrDefault(toolIdentifier, Collections.emptyList()).getOrNull(0)
    }

    fun hasToolInfo(toolIdentifier: String): Boolean {
        return allToolInfos.getOrDefault(toolIdentifier, Collections.emptyList()).isNotEmpty()
    }

    private fun createToolEnvironment(): Map<String, String> {
        val exports = HashMap<String, File>()
        val pathElements = ArrayList<File>()

        for (toolInfoEntry in allToolInfos.entries) {
            for (toolInfo in toolInfoEntry.value) {
                for (export in toolInfo.environmentVariables) {
                    exports.put(export.key, export.value)
                }
                for (pathElement in toolInfo.pathElements) {
                    pathElements.add(pathElement)
                }
            }
        }

        return ProcessEnvironmentHelper.createProcessEnvironment(exports, pathElements)
    }
}

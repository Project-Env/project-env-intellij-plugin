package io.projectenv.intellijplugin

import java.io.File

data class ToolInfo(
    val toolBinariesRoot: File?,
    val primaryExecutable: File?,
    val environmentVariables: Map<String, File>,
    val pathElements: List<File>,
    val handledProjectResources: List<File>,
    val unhandledProjectResources: Map<String, File>
)

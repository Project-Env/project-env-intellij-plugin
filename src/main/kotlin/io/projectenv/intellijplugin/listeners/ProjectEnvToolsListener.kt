package io.projectenv.intellijplugin.listeners

import io.projectenv.intellijplugin.toolinfo.ToolInfos

fun interface ProjectEnvToolsListener {

    fun toolsUpdated(toolInfos: ToolInfos)
}

package io.projectenv.intellijplugin.listeners

import io.projectenv.intellijplugin.toolinfo.ToolInfos

interface ProjectEnvToolsListener {

    fun toolsUpdated(toolInfos: ToolInfos)
}

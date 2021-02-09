package io.projectenv.intellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import io.projectenv.intellijplugin.services.ProjectEnvService

class CleanProjectEnvAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<ProjectEnvService>()?.cleanProjectEnv()
    }
}

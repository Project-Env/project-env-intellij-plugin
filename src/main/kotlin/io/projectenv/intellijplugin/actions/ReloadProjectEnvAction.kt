package io.projectenv.intellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import io.projectenv.intellijplugin.services.ProjectEnvService

class ReloadProjectEnvAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<ProjectEnvService>()?.refreshProjectEnv()
    }
}

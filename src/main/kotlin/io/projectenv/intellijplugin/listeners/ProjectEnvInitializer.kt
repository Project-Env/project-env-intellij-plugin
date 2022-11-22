package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import io.projectenv.intellijplugin.services.ProjectEnvService

class ProjectEnvInitializer : StartupActivity {

    override fun runActivity(project: Project) {
        project.service<ProjectEnvService>().refreshProjectEnv()
    }
}

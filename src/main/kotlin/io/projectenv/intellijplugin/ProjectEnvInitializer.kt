package io.projectenv.intellijplugin

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.projectenv.intellijplugin.services.ProjectEnvService

class ProjectEnvInitializer : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<ProjectEnvService>().refreshProjectEnv()
    }
}

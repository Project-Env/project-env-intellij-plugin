package ch.repolevedavaj.projectenv.intellijplugin

import ch.repolevedavaj.projectenv.intellijplugin.services.ProjectEnvService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class ProjectEnvInitializer : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<ProjectEnvService>().refreshProjectEnv()
    }

}

package io.projectenv.intellijplugin.services.impl

import com.intellij.openapi.project.Project
import io.projectenv.core.configuration.ProjectEnvConfiguration
import io.projectenv.core.configuration.ProjectEnvConfigurationFactory
import io.projectenv.intellijplugin.services.ProjectEnvConfigurationService
import java.io.File

class ProjectEnvConfigurationServiceImpl(val project: Project) : ProjectEnvConfigurationService {

    private var projectEnvConfiguration: ProjectEnvConfiguration? = null

    override fun getConfiguration(): ProjectEnvConfiguration? {
        return projectEnvConfiguration
    }

    override fun refresh() {
        val configurationFile = File(project.basePath, "project-env.yml")
        if (configurationFile.exists()) {
            projectEnvConfiguration = ProjectEnvConfigurationFactory.createFromFile(configurationFile)
        } else {
            projectEnvConfiguration = null
        }
    }
}

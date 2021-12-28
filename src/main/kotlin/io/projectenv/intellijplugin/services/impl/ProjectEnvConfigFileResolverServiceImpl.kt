package io.projectenv.intellijplugin.services.impl

import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.services.ProjectEnvConfigFileResolverService
import java.io.File

class ProjectEnvConfigFileResolverServiceImpl(val project: Project) : ProjectEnvConfigFileResolverService {

    override fun resolveConfig(): File? {
        val configFile = File(project.basePath, "project-env.toml")
        if (!configFile.exists()) {
            return null
        }

        return configFile
    }

    override fun dispose() {
        // noop
    }
}

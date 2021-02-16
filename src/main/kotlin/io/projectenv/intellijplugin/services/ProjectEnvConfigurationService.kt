package io.projectenv.intellijplugin.services

import io.projectenv.core.configuration.ProjectEnvConfiguration

interface ProjectEnvConfigurationService {

    fun getConfiguration(): ProjectEnvConfiguration?

    fun refresh()
}

package ch.repolevedavaj.projectenv.intellijplugin.services.impl


import ch.projectenv.core.configuration.ProjectEnvConfiguration
import ch.projectenv.core.configuration.ProjectEnvConfigurationFactory
import ch.projectenv.core.installer.ToolInstallers
import ch.projectenv.core.toolinfo.ToolInfo
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import ch.repolevedavaj.projectenv.intellijplugin.services.ExecutionEnvironmentService
import ch.repolevedavaj.projectenv.intellijplugin.services.ProjectEnvService
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.project.Project
import java.io.File

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val TOOL_CONFIGURER_EXTENSION_POINT_NAME: ProjectExtensionPointName<ToolConfigurer<ToolInfo>> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    override fun refreshProjectEnv() {
        val configurationFile = File(project.basePath, "project-env.yml")
        if (configurationFile.exists()) {
            val projectEnvConfiguration: ProjectEnvConfiguration =
                ProjectEnvConfigurationFactory.createFromFile(configurationFile)

            val toolDetailsList = installTools(projectEnvConfiguration)
            configureExecutionEnvironment(toolDetailsList)
            configureTools(toolDetailsList)
        }
    }

    private fun installTools(projectEnvConfiguration: ProjectEnvConfiguration): List<ToolInfo> {
        return ToolInstallers.installAllTools(projectEnvConfiguration, File(project.basePath))
    }

    private fun configureExecutionEnvironment(toolInfos: List<ToolInfo>) {
        val executionEnvironmentService = project.service<ExecutionEnvironmentService>()

        for (toolInfo in toolInfos) {
            for (export in toolInfo.environmentVariables) {
                executionEnvironmentService.registerExport(export.key, export.value)
            }
        }

        for (toolDetails in toolInfos) {
            for (pathElement in toolDetails.pathElements) {
                executionEnvironmentService.registerPathElement(pathElement)
            }
        }
    }

    private fun configureTools(toolInfos: List<ToolInfo>) {
        for (toolInfo in toolInfos) {
            for (toolConfigurer in TOOL_CONFIGURER_EXTENSION_POINT_NAME.extensions(project)) {
                if (toolConfigurer.supportsType(toolInfo)) {
                    toolConfigurer.configureTool(toolInfo)
                }
            }
        }
    }

}
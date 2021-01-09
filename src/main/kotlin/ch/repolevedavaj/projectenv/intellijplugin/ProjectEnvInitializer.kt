package ch.repolevedavaj.projectenv.intellijplugin

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.configuration.ConfigurationFactory
import ch.repolevedavaj.projectenv.core.configuration.ProjectEnv
import ch.repolevedavaj.projectenv.core.installer.ToolInstallerCollection
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import ch.repolevedavaj.projectenv.intellijplugin.services.ExecutionEnvironmentService
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import java.io.File

class ProjectEnvInitializer : ProjectManagerListener {

    private val TOOL_CONFIGURER_EXTENSION_POINT_NAME: ProjectExtensionPointName<ToolConfigurer> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    override fun projectOpened(project: Project) {
        val configurationFile = File(project.basePath, "project-env.xml")
        if (configurationFile.exists()) {
            val projectEnvConfiguration: ProjectEnv = ConfigurationFactory.createFromFile(configurationFile)

            val toolsDirectory = getToolsDirectory(project, projectEnvConfiguration)
            val toolDetailsList = installTools(projectEnvConfiguration, toolsDirectory)
            configureExecutionEnvironment(project, toolDetailsList)
            configureTools(project, toolDetailsList)
        }
    }

    private fun getToolsDirectory(project: Project, projectEnvConfiguration: ProjectEnv): File {
        return File(project.basePath, projectEnvConfiguration.tools.directory)
    }

    private fun installTools(projectEnvConfiguration: ProjectEnv, toolsDirectory: File): List<ProjectToolDetails> {
        return ToolInstallerCollection.installAllTools(projectEnvConfiguration, toolsDirectory)
    }

    private fun configureExecutionEnvironment(project: Project, toolDetailsList: List<ProjectToolDetails>) {
        val executionEnvironmentService = project.service<ExecutionEnvironmentService>()

        for (toolDetails in toolDetailsList) {
            for (export in toolDetails.exports) {
                executionEnvironmentService.registerExport(export.key, export.value)
            }
        }

        for (toolDetails in toolDetailsList) {
            for (pathElement in toolDetails.pathElements) {
                executionEnvironmentService.registerPathElement(pathElement)
            }
        }
    }

    private fun configureTools(project: Project, toolDetailsList: List<ProjectToolDetails>) {
        for (toolDetails in toolDetailsList) {
            for (toolConfigurer in TOOL_CONFIGURER_EXTENSION_POINT_NAME.extensions(project)) {
                if (toolConfigurer.supportsType(toolDetails.type)) {
                    toolConfigurer.configureTool(toolDetails)
                }
            }
        }
    }
}

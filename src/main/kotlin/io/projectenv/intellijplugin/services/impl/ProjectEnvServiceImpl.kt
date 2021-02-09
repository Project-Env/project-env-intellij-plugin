package io.projectenv.intellijplugin.services.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import io.projectenv.core.configuration.ProjectEnvConfiguration
import io.projectenv.core.configuration.ProjectEnvConfigurationFactory
import io.projectenv.core.tools.info.ToolInfo
import io.projectenv.core.tools.repository.ToolsRepositoryFactory
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.slf4j.LoggerFactory
import java.io.File

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val toolConfigurerExtensionPoint: ProjectExtensionPointName<ToolConfigurer<ToolInfo>> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val projectRoot = File(project.basePath)

    override fun refreshProjectEnv() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                try {
                    val configurationFile = getProjectEnvConfiguration()
                    if (configurationFile.exists()) {
                        val projectEnvConfiguration: ProjectEnvConfiguration =
                            ProjectEnvConfigurationFactory.createFromFile(configurationFile)

                        val toolDetailsList = installTools(projectEnvConfiguration)
                        configureExecutionEnvironment(toolDetailsList)

                        ApplicationManager.getApplication().invokeLater {
                            configureTools(toolDetailsList)
                        }
                    }
                } catch (exception: Exception) {
                    logger.error("failed to refresh project environment", exception)
                }
            },
            "Installing Project-Env",
            false,
            project
        )
    }

    override fun cleanProjectEnv() {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                try {
                    val configurationFile = getProjectEnvConfiguration()
                    if (configurationFile.exists()) {
                        val projectEnvConfiguration: ProjectEnvConfiguration =
                            ProjectEnvConfigurationFactory.createFromFile(configurationFile)

                        cleanTools(projectEnvConfiguration)
                    }
                } catch (exception: Exception) {
                    logger.error("failed to clean Project-Env", exception)
                }
            },
            "Cleaning Project-Env",
            false,
            project
        )
    }

    private fun getProjectEnvConfiguration(): File {
        return File(project.basePath, "project-env.yml")
    }

    private fun installTools(projectEnvConfiguration: ProjectEnvConfiguration): List<ToolInfo> {
        val toolsConfiguration = projectEnvConfiguration.toolsConfiguration

        return ToolsRepositoryFactory.createToolRepository(File(projectRoot, toolsConfiguration.toolsDirectory))
            .requestTools(toolsConfiguration.allToolConfigurations, projectRoot)
    }

    private fun cleanTools(projectEnvConfiguration: ProjectEnvConfiguration) {
        val toolsConfiguration = projectEnvConfiguration.toolsConfiguration

        ToolsRepositoryFactory.createToolRepository(File(projectRoot, toolsConfiguration.toolsDirectory))
            .cleanAllToolsOfCurrentOSExcluding(toolsConfiguration.allToolConfigurations)
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
            for (toolConfigurer in toolConfigurerExtensionPoint.extensions(project)) {
                if (toolConfigurer.supportsType(toolInfo)) {
                    toolConfigurer.configureTool(toolInfo)
                }
            }
        }
    }
}

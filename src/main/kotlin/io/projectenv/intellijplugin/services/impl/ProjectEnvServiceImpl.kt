package io.projectenv.intellijplugin.services.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import io.projectenv.core.configuration.ProjectEnvConfiguration
import io.projectenv.core.tools.info.ToolInfo
import io.projectenv.core.tools.repository.ToolsRepositoryFactory
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import io.projectenv.intellijplugin.services.ProjectEnvConfigurationService
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.slf4j.LoggerFactory
import java.io.File

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val toolConfigurerExtensionPoint: ProjectExtensionPointName<ToolConfigurer<ToolInfo>> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val projectRoot = File(project.basePath)

    override fun refreshProjectEnv() {
        runProcess("Installing Project-Env") {
            val configurationService = project.service<ProjectEnvConfigurationService>()
            configurationService.refresh()

            val configuration = configurationService.getConfiguration()
            if (configuration != null) {
                val toolDetailsList = installTools(configuration)
                configureExecutionEnvironment(toolDetailsList)

                ApplicationManager.getApplication().invokeLater {
                    configureTools(toolDetailsList)
                }
            }
        }
    }

    override fun cleanProjectEnv() {
        runProcess("Cleaning Project-Env") {
            val configuration = project.service<ProjectEnvConfigurationService>().getConfiguration()
            if (configuration != null) {
                cleanTools(configuration)
            }
        }
    }

    private fun runProcess(title: String, runnable: Runnable) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                try {
                    runnable.run()
                } catch (exception: Exception) {
                    logger.error("error running '$title'", exception)
                }
            },
            title,
            false,
            project
        )
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
        executionEnvironmentService.clear()

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

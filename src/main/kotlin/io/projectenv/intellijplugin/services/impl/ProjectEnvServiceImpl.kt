package io.projectenv.intellijplugin.services.impl

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.core.cli.api.ToolInfoParser
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName
import io.projectenv.core.commons.process.ProcessHelper
import io.projectenv.core.commons.process.ProcessResult
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import io.projectenv.intellijplugin.services.ProjectEnvCliResolverService
import io.projectenv.intellijplugin.services.ProjectEnvService
import java.io.File
import java.util.Collections

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val toolConfigurerExtensionPoint: ProjectExtensionPointName<ToolConfigurer> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    private val projectRoot = File(project.basePath!!)

    override fun refreshProjectEnv() {
        val configurationFile = File(project.basePath, "project-env.toml")
        if (!configurationFile.exists()) {
            return
        }

        val projectEnvCliExecutable = project.service<ProjectEnvCliResolverService>().resolveCli()
        if (projectEnvCliExecutable == null) {
            handleMissingCli()
            return
        }

        runProcess("Installing Project-Env") {
            val result = executeProjectEnvCli(projectEnvCliExecutable, configurationFile)
            if (isSuccessfulCliExecution(result)) {
                handleSuccessfulCliExecution(result)
            } else {
                handleCliExecutionFailure(result)
            }
        }
    }

    private fun handleMissingCli() {
        getProjectEnvNotificationGroup()
            .createNotification(
                "Could not resolve Project-Env CLI. Please make sure that the CLI is installed and on ${getPathVariableName()}.",
                NotificationType.WARNING
            )
            .notify(project)
    }

    private fun runProcess(title: String, runnable: Runnable) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                runnable.run()
            },
            title,
            false,
            project
        )
    }

    private fun executeProjectEnvCli(projectEnvCliExecutable: File, configurationFile: File): ProcessResult {
        val processBuilder = ProcessBuilder()
            .command(
                projectEnvCliExecutable.canonicalPath,
                "--project-root",
                projectRoot.canonicalPath,
                "--config-file",
                configurationFile.canonicalPath
            )
            .directory(projectRoot)

        return ProcessHelper.executeProcess(processBuilder, true, true)
    }

    private fun isSuccessfulCliExecution(result: ProcessResult): Boolean {
        return result.exitCode == 0
    }

    private fun handleSuccessfulCliExecution(result: ProcessResult) {
        val toolDetails = parseRawToolDetails(result)

        configureExecutionEnvironment(toolDetails)

        ApplicationManager.getApplication().invokeLater {
            configureTools(toolDetails)
        }
    }

    private fun parseRawToolDetails(result: ProcessResult): Map<String, List<ToolInfo>> {
        return ToolInfoParser.fromJson(result.stdOutput.get())
    }

    private fun configureExecutionEnvironment(toolInfos: Map<String, List<ToolInfo>>) {
        val executionEnvironmentService = project.service<ExecutionEnvironmentService>()
        executionEnvironmentService.clear()

        for (toolInfoEntry in toolInfos.entries) {
            for (toolInfo in toolInfoEntry.value) {
                for (export in toolInfo.environmentVariables) {
                    executionEnvironmentService.registerExport(export.key, export.value)
                }
                for (pathElement in toolInfo.pathElements) {
                    executionEnvironmentService.registerPathElement(pathElement)
                }
            }
        }
    }

    private fun configureTools(toolInfos: Map<String, List<ToolInfo>>) {
        for (toolConfigurer in toolConfigurerExtensionPoint.extensions(project)) {
            for (toolInfo in toolInfos.getOrDefault(toolConfigurer.getToolIdentifier(), Collections.emptyList())) {
                toolConfigurer.configureTool(toolInfo)
            }
        }
    }

    private fun handleCliExecutionFailure(result: ProcessResult) {
        getProjectEnvNotificationGroup()
            .createNotification(
                "Failed to execute Project-Env CLI. See process output for more details:\n${result.errOutput.get()}",
                NotificationType.WARNING
            )
            .notify(project)
    }

    override fun dispose() {
        // noop
    }
}

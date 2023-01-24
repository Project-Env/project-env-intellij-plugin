package io.projectenv.intellijplugin.services.impl

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName
import io.projectenv.core.commons.process.ProcessHelper
import io.projectenv.core.commons.process.ProcessResult
import io.projectenv.intellijplugin.listeners.ProjectEnvTopics
import io.projectenv.intellijplugin.notifications.ProjectEnvNotificationGroup
import io.projectenv.intellijplugin.services.ProjectEnvCliResolverService
import io.projectenv.intellijplugin.services.ProjectEnvConfigFileResolverService
import io.projectenv.intellijplugin.services.ProjectEnvService
import io.projectenv.intellijplugin.toolinfo.ToolInfoParser
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import java.io.File

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val projectRoot = File(project.basePath!!)

    override fun refreshProjectEnv(sync: Boolean) {
        val configurationFile = project.service<ProjectEnvConfigFileResolverService>().resolveConfig() ?: return

        val projectEnvCliExecutable = project.service<ProjectEnvCliResolverService>().resolveCli()
        if (projectEnvCliExecutable == null) {
            handleMissingCli()
            return
        }

        runProcess("Installing Project-Env", sync) {
            val result = executeProjectEnvCli(projectEnvCliExecutable, configurationFile)
            if (isSuccessfulCliExecution(result)) {
                handleSuccessfulCliExecution(result)
            } else {
                handleCliExecutionFailure(result)
            }
        }
    }

    private fun handleMissingCli() {
        ProjectEnvNotificationGroup.createNotification(
            "Could not resolve Project-Env CLI. Please make sure that the CLI is installed and on ${getPathVariableName()}.",
            NotificationType.WARNING
        ).notify(project)
    }

    private fun runProcess(title: String, sync: Boolean, runnable: Runnable) {
        if (sync) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, project)
        } else {
            val task = object : Task.Backgroundable(project, title, false) {
                override fun run(indicator: ProgressIndicator) {
                    runnable.run()
                }
            }

            ProgressManager.getInstance()
                .runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
        }
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

        project.messageBus.syncPublisher(ProjectEnvTopics.TOOLS_TOPIC).toolsUpdated(toolDetails)
    }

    private fun parseRawToolDetails(result: ProcessResult): ToolInfos {
        return ToolInfoParser.fromJson(result.stdOutput.get())
    }

    private fun handleCliExecutionFailure(result: ProcessResult) {
        ProjectEnvNotificationGroup
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

package io.projectenv.intellijplugin.services.impl

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.ToolSupportHelper
import io.projectenv.core.cli.configuration.ProjectEnvConfiguration
import io.projectenv.core.cli.configuration.toml.TomlConfigurationFactory
import io.projectenv.core.cli.index.DefaultToolsIndexManager
import io.projectenv.core.cli.installer.DefaultLocalToolInstallationManager
import io.projectenv.core.commons.process.ProcessHelper
import io.projectenv.core.commons.process.ProcessResult
import io.projectenv.core.toolsupport.spi.ImmutableToolSupportContext
import io.projectenv.core.toolsupport.spi.ToolInfo
import io.projectenv.core.toolsupport.spi.ToolSupport
import io.projectenv.core.toolsupport.spi.ToolSupportContext
import io.projectenv.intellijplugin.listeners.ProjectEnvTopics
import io.projectenv.intellijplugin.notifications.ProjectEnvNotificationGroup
import io.projectenv.intellijplugin.services.ProjectEnvCliResolverService
import io.projectenv.intellijplugin.services.ProjectEnvConfigFileResolverService
import io.projectenv.intellijplugin.services.ProjectEnvService
import io.projectenv.intellijplugin.toolinfo.ToolInfoParser
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import java.io.File
import java.util.ServiceLoader

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val projectRoot = File(project.basePath!!)

    override fun refreshProjectEnv(sync: Boolean) {
        val configurationFile = project.service<ProjectEnvConfigFileResolverService>().resolveConfig() ?: return

        runProcess("Installing Project-Env", sync) {
            try {
                val projectEnvCliExecutable = project.service<ProjectEnvCliResolverService>().resolveCli()

                val toolInfos = if (projectEnvCliExecutable != null) {
                    executeProjectEnvCliExecutable(projectEnvCliExecutable, configurationFile)
                } else {
                    executeEmbeddedProjectEnvCli(configurationFile)
                }

                project.messageBus.syncPublisher(ProjectEnvTopics.TOOLS_TOPIC).toolsUpdated(toolInfos)
            } catch (e: Exception) {
                ProjectEnvNotificationGroup.createNotification(e.message.orEmpty(), NotificationType.WARNING)
                    .notify(project)
            }
        }
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

    private fun executeProjectEnvCliExecutable(projectEnvCliExecutable: File, configurationFile: File): ToolInfos {
        val processBuilder = ProcessBuilder()
            .command(
                projectEnvCliExecutable.canonicalPath,
                "--project-root",
                projectRoot.canonicalPath,
                "--config-file",
                configurationFile.canonicalPath
            )
            .directory(projectRoot)

        val result = ProcessHelper.executeProcess(processBuilder, true, true)
        if (isSuccessfulCliExecution(result)) {
            return ToolInfoParser.fromJson(result.stdOutput.get())
        } else {
            throw Exception("Failed to execute Project-Env CLI. See process output for more details:\n${result.errOutput.get()}")
        }
    }

    private fun isSuccessfulCliExecution(result: ProcessResult): Boolean {
        return result.exitCode == 0
    }

    private fun executeEmbeddedProjectEnvCli(configurationFile: File): ToolInfos {
        val configuration: ProjectEnvConfiguration = TomlConfigurationFactory.fromFile(configurationFile)
        val toolSupportContext = createToolSupportContext(configuration)

        val toolInstallationInfos = LinkedHashMap<String, List<ToolInfo>>()
        for (toolSupport in ServiceLoader.load(ToolSupport::class.java, ToolSupport::class.java.classLoader)) {
            val toolInfos: List<ToolInfo> = installOrUpdateTool(toolSupport, configuration, toolSupportContext)
            if (toolInfos.isNotEmpty()) {
                toolInstallationInfos[toolSupport.toolIdentifier] = toolInfos
            }
        }

        return ToolInfos(toolInstallationInfos)
    }

    private fun <T> installOrUpdateTool(
        toolSupport: ToolSupport<T>,
        configuration: ProjectEnvConfiguration,
        toolSupportContext: ToolSupportContext
    ): List<ToolInfo> {
        val toolSupportConfigurationClass = ToolSupportHelper.getToolSupportConfigurationClass(toolSupport)
        val toolConfigurations =
            configuration.getToolConfigurations(toolSupport.toolIdentifier, toolSupportConfigurationClass)
        if (toolConfigurations.isEmpty()) {
            return emptyList()
        }

        val toolInfos = ArrayList<ToolInfo>()
        for (toolConfiguration in toolConfigurations) {
            toolInfos.add(toolSupport.prepareTool(toolConfiguration, toolSupportContext))
        }
        return toolInfos
    }

    private fun createToolSupportContext(configuration: ProjectEnvConfiguration): ToolSupportContext {
        val toolsDirectory = File(projectRoot, configuration.toolsDirectory)
        val localToolInstallationManager = DefaultLocalToolInstallationManager(toolsDirectory)
        val toolsIndexManager = DefaultToolsIndexManager(toolsDirectory)

        return ImmutableToolSupportContext.builder()
            .projectRoot(projectRoot)
            .localToolInstallationManager(localToolInstallationManager)
            .toolsIndexManager(toolsIndexManager)
            .build()
    }

    override fun dispose() {
        // noop
    }
}

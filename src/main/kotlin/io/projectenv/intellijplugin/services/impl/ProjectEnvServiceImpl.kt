package io.projectenv.intellijplugin.services.impl

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ProjectExtensionPointName
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.core.cli.api.ToolInfoParser
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import io.projectenv.intellijplugin.services.ProjectEnvException
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.TimeUnit

class ProjectEnvServiceImpl(val project: Project) : ProjectEnvService {

    private val toolConfigurerExtensionPoint: ProjectExtensionPointName<ToolConfigurer> =
        ProjectExtensionPointName("ch.repolevedavaj.projectenv.intellijplugin.toolConfigurer")

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val projectRoot = File(project.basePath!!)

    override fun refreshProjectEnv() {
        val configurationFile = File(project.basePath, "project-env.toml")
        if (configurationFile.exists()) {

            val projectEnvCliExecutable = getProjectEnvCliExecutable()
            if (projectEnvCliExecutable == null || !projectEnvCliExecutable.exists()) {
                NotificationGroupManager.getInstance().getNotificationGroup("Project-Env")
                    .createNotification(
                        "Could not resolve Project-Env CLI. Please make sure that the CLI is installed and on PATH.",
                        NotificationType.WARNING
                    )
                    .notify(project)
                return
            }

            runProcess("Installing Project-Env") {
                val rawToolDetails = executeProjectEnvCli(projectEnvCliExecutable, configurationFile)
                val toolDetails = parseRawToolDetails(rawToolDetails)

                configureExecutionEnvironment(toolDetails)

                ApplicationManager.getApplication().invokeLater {
                    configureTools(toolDetails)
                }
            }
        }
    }

    private fun getProjectEnvCliExecutable(): File? {
        val projectEnvCliExecutableName = getProjectEnvCliExecutableName()
        for (candidate in getProjectEnvCliExecutableLocationCandidates()) {
            val projectEnvCliExecutable = File(candidate, projectEnvCliExecutableName)
            if (projectEnvCliExecutable.exists()) {
                return projectEnvCliExecutable
            }
        }

        return null
    }

    private fun getProjectEnvCliExecutableLocationCandidates(): ArrayList<String> {
        val candidates = ArrayList<String>()
        candidates.addAll(getPathElements())
        candidates.addAll(getKnownExecutableLocations())

        return candidates
    }

    private fun getPathElements(): List<String> {
        return System.getenv()["PATH"]?.split(File.pathSeparator).orEmpty()
    }

    private fun getKnownExecutableLocations(): List<String> {
        return if (!SystemUtils.IS_OS_WINDOWS) {
            Collections.singletonList("/usr/local/bin")
        } else {
            Collections.emptyList()
        }
    }

    private fun getProjectEnvCliExecutableName(): String {
        return if (SystemUtils.IS_OS_WINDOWS) {
            "project-env-cli.exe"
        } else {
            "project-env-cli"
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

    private fun executeProjectEnvCli(projectEnvCliExecutable: File, configurationFile: File): String {
        val process = ProcessBuilder()
            .command(
                projectEnvCliExecutable.canonicalPath,
                "--project-root",
                projectRoot.canonicalPath,
                "--config-file",
                configurationFile.canonicalPath
            )
            .directory(projectRoot)
            .start()

        if (!process.waitFor(1, TimeUnit.HOURS)) {
            process.destroy()
        }

        if (process.exitValue() != 0) {
            throw ProjectEnvException("failed to execute Project-Env CLI")
        }

        return StringUtils.toEncodedString(process.inputStream.readAllBytes(), StandardCharsets.UTF_8)
    }

    private fun parseRawToolDetails(output: String): Map<String, List<ToolInfo>> {
        return ToolInfoParser.fromJson(output)
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

    override fun dispose() {
        // noop
    }
}

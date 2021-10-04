package io.projectenv.intellijplugin.configurers.maven

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import org.jetbrains.idea.maven.execution.MavenRunner
import org.jetbrains.idea.maven.project.MavenProjectsManager

class MavenConfigurer(val project: Project) : ToolConfigurer {

    override fun getToolIdentifier(): String {
        return "maven"
    }

    override fun configureTool(toolInfo: ToolInfo) {
        ApplicationManager.getApplication().runWriteAction {
            val settings = MavenProjectsManager.getInstance(project)
            settings.generalSettings.mavenHome = toolInfo.toolBinariesRoot.get().canonicalPath

            val userSettingsFile = toolInfo.unhandledProjectResources.get("userSettingsFile")
            if (userSettingsFile != null) {
                settings.generalSettings.setUserSettingsFile(userSettingsFile.canonicalPath)
            }

            val environment = project.service<ExecutionEnvironmentService>().createEnvironment()
            MavenRunner.getInstance(project).settings.environmentProperties.putAll(environment)
        }
    }
}

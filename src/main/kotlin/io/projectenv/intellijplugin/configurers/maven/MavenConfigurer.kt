package io.projectenv.intellijplugin.configurers.maven

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.projectenv.core.tools.info.MavenInfo
import io.projectenv.core.tools.info.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import org.jetbrains.idea.maven.execution.MavenRunner
import org.jetbrains.idea.maven.project.MavenProjectsManager

class MavenConfigurer(val project: Project) : ToolConfigurer<MavenInfo> {

    override fun supportsType(toolInfo: ToolInfo): Boolean {
        return toolInfo is MavenInfo
    }

    override fun configureTool(toolInfo: MavenInfo) {
        ApplicationManager.getApplication().runWriteAction {
            val settings = MavenProjectsManager.getInstance(project)
            settings.generalSettings.mavenHome = toolInfo.location.canonicalPath

            if (toolInfo.userSettingsFile.isPresent && toolInfo.userSettingsFile.get().exists()) {
                settings.generalSettings.setUserSettingsFile(toolInfo.userSettingsFile.get().canonicalPath)
            }

            val environment = project.service<ExecutionEnvironmentService>().createEnvironment()
            MavenRunner.getInstance(project).settings.environmentProperties.putAll(environment)
        }
    }
}

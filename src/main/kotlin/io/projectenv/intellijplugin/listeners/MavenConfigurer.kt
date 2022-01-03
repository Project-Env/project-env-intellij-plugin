package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import org.jetbrains.idea.maven.execution.MavenRunner
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager

class MavenConfigurer(val project: Project) : ProjectEnvToolsListener {

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val mavenInfo = toolInfos.getToolInfo("maven") ?: return

        WriteAction.runAndWait<Throwable> {
            val settings = MavenProjectsManager.getInstance(project)
            settings.generalSettings.mavenHome = mavenInfo.toolBinariesRoot!!.canonicalPath

            val userSettingsFile = mavenInfo.unhandledProjectResources["userSettingsFile"]
            if (userSettingsFile != null) {
                settings.generalSettings.setUserSettingsFile(userSettingsFile.canonicalPath)
            }

            MavenRunner.getInstance(project).settings.environmentProperties.putAll(toolInfos.toolEnvironment)

            if (toolInfos.hasToolInfo("jdk")) {
                MavenProjectsManager.getInstance(project).importingSettings.jdkForImporter =
                    MavenRunnerSettings.USE_PROJECT_JDK
            }
        }
    }
}

package io.projectenv.intellijplugin.configurers.maven

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager

class MavenImporterJdkConfigurer(val project: Project) : ToolConfigurer {

    override fun getToolIdentifier(): String {
        return "jdk"
    }

    override fun configureTool(toolInfo: ToolInfo) {
        ApplicationManager.getApplication().runWriteAction {
            MavenProjectsManager.getInstance(project).importingSettings.jdkForImporter = MavenRunnerSettings.USE_PROJECT_JDK
        }
    }
}

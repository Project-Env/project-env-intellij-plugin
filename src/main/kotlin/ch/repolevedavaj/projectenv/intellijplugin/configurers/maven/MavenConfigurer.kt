package ch.repolevedavaj.projectenv.intellijplugin.configurers.maven

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import ch.repolevedavaj.projectenv.intellijplugin.services.ExecutionEnvironmentService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.execution.MavenRunner
import org.jetbrains.idea.maven.project.MavenProjectsManager

class MavenConfigurer(val project: Project) : ToolConfigurer {

    override fun supportsType(type: ProjectToolType): Boolean {
        return type == ProjectToolType.MAVEN
    }

    override fun configureTool(toolDetails: ProjectToolDetails) {
        ApplicationManager.getApplication().runWriteAction {
            MavenProjectsManager.getInstance(project).generalSettings.mavenHome = toolDetails.location.canonicalPath

            val environment = project.service<ExecutionEnvironmentService>().createEnvironment();
            MavenRunner.getInstance(project).settings.environmentProperties.putAll(environment)
        }
    }

}
package ch.repolevedavaj.projectenv.intellijplugin.configurers.gradle

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import com.intellij.execution.RunManager
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.execution.build.GradleExecutionEnvironmentProvider
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings

class GradleConfigurer(val project: Project) : ToolConfigurer {

    override fun supportsType(type: ProjectToolType): Boolean {
        return type == ProjectToolType.GRADLE
    }

    override fun configureTool(toolDetails: ProjectToolDetails) {
        GradleSettings.getInstance(project)
            .subscribe(GradleSettingsListener(toolDetails), this)
    }

    private class GradleSettingsListener(val toolDetails: ProjectToolDetails) :
        ExternalSystemSettingsListenerAdapter<GradleProjectSettings>() {

        override fun onProjectsLinked(settings: MutableCollection<GradleProjectSettings>) {
            for (projectSettings in settings) {
                projectSettings.distributionType = DistributionType.LOCAL
                projectSettings.gradleHome = toolDetails.location.canonicalPath
            }
        }
    }

}
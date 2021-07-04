package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings

class GradleConfigurer(project: Project) : ToolConfigurer,
    ExternalSystemSettingsListenerAdapter<GradleProjectSettings>() {

    private var toolInfo: ToolInfo? = null

    init {
        val disposableParent = project.service<ProjectEnvService>()
        GradleSettings.getInstance(project).subscribe(this, disposableParent)
    }

    override fun getToolIdentifier(): String {
        return "gradle"
    }

    override fun configureTool(toolInfo: ToolInfo) {
        this.toolInfo = toolInfo
    }

    override fun onProjectsLinked(settings: MutableCollection<GradleProjectSettings>) {
        updateProjectsConfiguration(settings)
    }

    override fun onProjectsLoaded(settings: MutableCollection<GradleProjectSettings>) {
        updateProjectsConfiguration(settings)
    }

    private fun updateProjectsConfiguration(settings: MutableCollection<GradleProjectSettings>) {
        if (toolInfo == null) {
            return
        }

        for (projectSettings in settings) {
            projectSettings.distributionType = DistributionType.LOCAL
            projectSettings.gradleHome = toolInfo!!.toolBinariesRoot.get().canonicalPath
        }
    }
}

package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.openapi.project.Project
import io.projectenv.core.tools.info.GradleInfo
import io.projectenv.core.tools.info.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings

class GradleConfigurer(project: Project) : ToolConfigurer<GradleInfo>,
    ExternalSystemSettingsListenerAdapter<GradleProjectSettings>() {

    private var toolInfo: GradleInfo? = null

    init {
        val disposableParent = project.service<ProjectEnvService>()
        GradleSettings.getInstance(project).subscribe(this, disposableParent)
    }

    override fun supportsType(toolInfo: ToolInfo): Boolean {
        return toolInfo is GradleInfo
    }

    override fun configureTool(toolInfo: GradleInfo) {
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
            projectSettings.gradleHome = toolInfo!!.location.canonicalPath
        }
    }
}

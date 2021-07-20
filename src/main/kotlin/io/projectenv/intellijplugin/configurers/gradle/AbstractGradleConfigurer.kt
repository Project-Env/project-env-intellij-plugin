package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings

abstract class AbstractGradleConfigurer(val project: Project) : ToolConfigurer,
    ExternalSystemSettingsListenerAdapter<GradleProjectSettings>() {

    private var toolInfo: ToolInfo? = null

    init {
        val disposableParent = project.service<ProjectEnvService>()
        GradleSettings.getInstance(project).subscribe(this, disposableParent)
    }

    override fun configureTool(toolInfo: ToolInfo) {
        this.toolInfo = toolInfo

        updateProjectsConfiguration(GradleSettings.getInstance(project).linkedProjectsSettings)
    }

    override fun onProjectsLinked(settings: MutableCollection<GradleProjectSettings>) {
        updateProjectsConfiguration(settings)
    }

    override fun onProjectsLoaded(settings: MutableCollection<GradleProjectSettings>) {
        updateProjectsConfiguration(settings)
    }

    private fun updateProjectsConfiguration(settings: Collection<GradleProjectSettings>) {
        if (toolInfo == null) {
            return
        }

        ApplicationManager.getApplication().runWriteAction {
            for (projectSettings in settings) {
                updateProjectConfiguration(toolInfo!!, projectSettings)
            }
        }
    }

    protected abstract fun updateProjectConfiguration(toolInfo: ToolInfo, projectSettings: GradleProjectSettings)
}

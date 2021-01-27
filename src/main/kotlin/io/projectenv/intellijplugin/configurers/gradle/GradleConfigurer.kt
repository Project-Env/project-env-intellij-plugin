package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.openapi.project.Project
import io.projectenv.core.toolinfo.GradleInfo
import io.projectenv.core.toolinfo.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings

class GradleConfigurer(val project: Project) : ToolConfigurer<GradleInfo> {

    override fun supportsType(toolInfo: ToolInfo): Boolean {
        return toolInfo is GradleInfo
    }

    override fun configureTool(toolInfo: GradleInfo) {
        GradleSettings.getInstance(project)
            .subscribe(GradleSettingsListener(toolInfo), this)
    }

    private class GradleSettingsListener(val toolInfo: GradleInfo) :
        ExternalSystemSettingsListenerAdapter<GradleProjectSettings>() {

        override fun onProjectsLinked(settings: MutableCollection<GradleProjectSettings>) {
            for (projectSettings in settings) {
                projectSettings.distributionType = DistributionType.LOCAL
                projectSettings.gradleHome = toolInfo.location.canonicalPath
            }
        }

        override fun onProjectsLoaded(settings: MutableCollection<GradleProjectSettings>) {
            for (projectSettings in settings) {
                projectSettings.distributionType = DistributionType.LOCAL
                projectSettings.gradleHome = toolInfo.location.canonicalPath
            }
        }
    }
}

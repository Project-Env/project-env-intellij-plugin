package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.ToolInfo
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings

class GradleConfigurer(project: Project) : AbstractGradleConfigurer(project) {

    override fun getToolIdentifier(): String {
        return "gradle"
    }

    override fun updateProjectConfiguration(toolInfo: ToolInfo, projectSettings: GradleProjectSettings) {
        projectSettings.distributionType = DistributionType.LOCAL
        projectSettings.gradleHome = toolInfo.toolBinariesRoot?.canonicalPath
    }
}

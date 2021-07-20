package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.project.Project
import io.projectenv.core.cli.api.ToolInfo
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings

class GradleJdkConfigurer(project: Project) : AbstractGradleConfigurer(project) {

    override fun getToolIdentifier(): String {
        return "jdk"
    }

    override fun updateProjectConfiguration(toolInfo: ToolInfo, projectSettings: GradleProjectSettings) {
        projectSettings.gradleJvm = ExternalSystemJdkUtil.USE_PROJECT_JDK
    }
}

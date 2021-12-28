package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import org.jetbrains.plugins.gradle.config.GradleSettingsListenerAdapter
import org.jetbrains.plugins.gradle.settings.DistributionType
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.settings.GradleSettingsListener

class GradleConfigurer(val project: Project) : ProjectEnvToolsListener {

    private var toolInfos: ToolInfos? = null

    init {
        project.messageBus.connect().subscribe(
            GradleSettingsListener.TOPIC,
            object : GradleSettingsListenerAdapter() {
                override fun onProjectsLinked(settings: MutableCollection<GradleProjectSettings>) {
                    updateProjectsConfiguration(settings)
                }

                override fun onProjectsLoaded(settings: MutableCollection<GradleProjectSettings>) {
                    updateProjectsConfiguration(settings)
                }
            }
        )
    }

    override fun toolsUpdated(toolInfos: ToolInfos) {
        this.toolInfos = toolInfos

        updateProjectsConfiguration(GradleSettings.getInstance(project).linkedProjectsSettings)
    }

    private fun updateProjectsConfiguration(settings: Collection<GradleProjectSettings>) {
        val gradleInfo = toolInfos?.getToolInfo("gradle") ?: return
        val hasJdkInfo = toolInfos!!.hasToolInfo("jdk")

        WriteAction.runAndWait<Throwable> {
            for (projectSettings in settings) {
                projectSettings.distributionType = DistributionType.LOCAL
                projectSettings.gradleHome = gradleInfo.toolBinariesRoot?.canonicalPath

                if (hasJdkInfo) {
                    projectSettings.gradleJvm = ExternalSystemJdkUtil.USE_PROJECT_JDK
                }
            }
        }
    }
}

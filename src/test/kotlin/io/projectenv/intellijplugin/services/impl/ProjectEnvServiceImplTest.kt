package io.projectenv.intellijplugin.services.impl

import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.replaceService
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName
import io.projectenv.intellijplugin.AbstractProjectEnvTest
import io.projectenv.intellijplugin.ProjectEnvCliHelper
import io.projectenv.intellijplugin.services.ProjectEnvCliResolverService
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.junit.Test
import java.io.File

class ProjectEnvServiceImplTest : AbstractProjectEnvTest() {

    @Test
    fun testRefreshUsesEmbeddedCli() {
        copyResourceToProjectRootAndRefresh("build.gradle")
        copyResourceToProjectRootAndRefresh("project-env.toml")
        clearFiredNotifications()

        hideProjectEnvCli()

        val service = project.service<ProjectEnvService>()
        service.refreshProjectEnv(true)

        assertMavenSettings()
        assertJdkSettings()
        assertNodeSettings()
        assertGradleSettings()
    }

    @Test
    fun testRefreshFailsBecauseConfigFileIsInvalid() {
        copyResourceToProjectRootAndRefresh("build.gradle")
        copyResourceToProjectRootAndRefresh("invalid-project-env.toml", "project-env.toml")
        clearFiredNotifications()

        val pathElement = ProjectEnvCliHelper.setupProjectEnvCli("3.8.0", tempDir.createDir().toFile())
        withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute {
            val service = project.service<ProjectEnvService>()
            service.refreshProjectEnv(true)

            assertNotificationFired("Failed to execute Project-Env CLI", NotificationType.WARNING)
        }
    }

    @Test
    fun testRefreshSuccessful() {
        copyResourceToProjectRootAndRefresh("build.gradle")
        copyResourceToProjectRootAndRefresh("project-env.toml")
        clearFiredNotifications()

        val pathElement = ProjectEnvCliHelper.setupProjectEnvCli("3.8.0", tempDir.createDir().toFile())
        withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute {
            val service = project.service<ProjectEnvService>()
            service.refreshProjectEnv(true)

            assertMavenSettings()
            assertJdkSettings()
            assertNodeSettings()
            assertGradleSettings()
        }
    }

    private fun assertMavenSettings() {
        assertToolPath(MavenProjectsManager.getInstance(project).generalSettings.mavenHome)
    }

    private fun assertJdkSettings() {
        val jdk = ProjectRootManager.getInstance(project).projectSdk as ProjectJdkImpl
        assertToolPath(jdk.homePath)
        assertThat(jdk.getUrls(OrderRootType.CLASSES)).hasSize(77)
        assertThat(MavenProjectsManager.getInstance(project).importingSettings.jdkForImporter)
            .isEqualTo(MavenRunnerSettings.USE_PROJECT_JDK)
    }

    private fun assertNodeSettings() {
        val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
        assertThat(interpreter).isInstanceOf(NodeJsLocalInterpreter::class.java)
        assertToolPath((interpreter as NodeJsLocalInterpreter).interpreterSystemDependentPath)
    }

    private fun assertGradleSettings() {
        linkAndRefreshGradleProject(getProjectRoot().canonicalPath, project)

        val linkedProjectsSettings = GradleSettings.getInstance(project).linkedProjectsSettings
        assertThat(linkedProjectsSettings).hasSize(1)

        val projectSettings = linkedProjectsSettings.first()
        assertToolPath(projectSettings.gradleHome)
        assertThat(projectSettings.gradleJvm).isEqualTo(ExternalSystemJdkUtil.USE_PROJECT_JDK)
    }

    private fun assertToolPath(path: String?) {
        assertThat(normalizePath(path)).startsWith(normalizePath(getProjectRoot().canonicalPath))
        assertThat(File(path!!)).exists()
    }

    private fun normalizePath(path: String?): String? {
        return path?.replace('\\', '/')
    }

    private fun hideProjectEnvCli() {
        project.replaceService(
            ProjectEnvCliResolverService::class.java,
            object : ProjectEnvCliResolverService {

                override fun resolveCli(): File? {
                    return null
                }

                override fun dispose() {
                    // noop
                }
            },
            project
        )
    }
}

package io.projectenv.intellijplugin.listeners

import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import com.intellij.openapi.components.service
import io.projectenv.core.commons.process.ProcessEnvironmentHelper
import io.projectenv.intellijplugin.AbstractProjectEnvTest
import io.projectenv.intellijplugin.ProjectEnvCliHelper
import io.projectenv.intellijplugin.services.ProjectEnvService
import org.junit.Test

class ProjectEnvFilesListenerTest : AbstractProjectEnvTest() {

    @Test
    fun testRelevantFilesChanged() {
        val pathElement = ProjectEnvCliHelper.setupProjectEnvCli("3.8.0", tempDir.createDir().toFile())

        withEnvironmentVariable(
            ProcessEnvironmentHelper.getPathVariableName(),
            ProcessEnvironmentHelper.createExtendedPathValue(pathElement)
        ).execute {
            copyResourceToProjectRootAndRefresh("project-env.toml")
            assertNotificationFired("Project-Env config file has been updated")

            project.service<ProjectEnvService>().refreshProjectEnv(true)
            assertNoNotificationFired()

            copyResourceToProjectRootAndRefresh("global-settings-1.xml", "global-settings.xml")
            assertNoNotificationFired()

            copyResourceToProjectRootAndRefresh("user-settings-1.xml", "user-settings.xml")
            assertNoNotificationFired()

            project.service<ProjectEnvService>().refreshProjectEnv(true)
            assertNoNotificationFired()

            copyResourceToProjectRootAndRefresh("user-settings-2.xml", "user-settings.xml")
            assertNoNotificationFired()

            copyResourceToProjectRootAndRefresh("global-settings-2.xml", "global-settings.xml")
            assertNotificationFired("Project-Env relevant files have been updated")
        }
    }
}

package io.projectenv.intellijplugin.listeners

import io.projectenv.intellijplugin.AbstractProjectEnvTest
import org.junit.Test

class ProjectEnvConfigFileListenerTest : AbstractProjectEnvTest() {

    @Test
    fun testConfigFileChanged() {
        copyResourceToProjectRootAndRefresh("project-env-1.toml", "project-env.toml")
        assertReloadNotificationFired()

        copyResourceToProjectRootAndRefresh("project-env-2.toml", "project-env.toml")
        assertReloadNotificationFired()
    }

    private fun assertReloadNotificationFired() {
        assertNotificationFired("Project-Env config file has been updated")
    }
}

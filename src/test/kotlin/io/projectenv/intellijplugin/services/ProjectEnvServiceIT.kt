package io.projectenv.intellijplugin.services

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ProjectEnvServiceIT : BasePlatformTestCase() {

    @Test
    fun testRefreshProjectEnv() {
        copyResourceToProjectRoot("project-env.yml")

        val service = myFixture.project.service<ProjectEnvService>()
        service.refreshProjectEnv()

        assertMavenSettings()
        assertJdkSettings()
        assertNodeSettings()
    }

    override fun tearDown() {
        val jdk = ProjectRootManager.getInstance(myFixture.project).projectSdk
        if (jdk != null) {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(jdk)
            }
        }

        super.tearDown()
    }

    private fun assertMavenSettings() {
        assertToolPath(MavenProjectsManager.getInstance(project).generalSettings.mavenHome)
    }

    private fun assertJdkSettings() {
        assertToolPath(ProjectRootManager.getInstance(project).projectSdk?.homePath)
    }

    private fun assertNodeSettings() {
        val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
        assertThat(interpreter).isInstanceOf(NodeJsLocalInterpreter::class.java)
        assertToolPath((interpreter as NodeJsLocalInterpreter).interpreterSystemDependentPath)
    }

    private fun assertToolPath(path: String?) {
        assertThat(path).startsWith(getProjectRoot().canonicalPath)
        assertThat(File(path)).exists()
    }

    private fun copyResourceToProjectRoot(resource: String) {
        val resultingFile = File(getProjectRoot(), resource)
        javaClass.getResourceAsStream(resource).use { inputStream ->
            FileOutputStream(resultingFile).use { outputStream ->
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }

    private fun getProjectRoot(): File {
        return File(myFixture.project.basePath)
    }
}

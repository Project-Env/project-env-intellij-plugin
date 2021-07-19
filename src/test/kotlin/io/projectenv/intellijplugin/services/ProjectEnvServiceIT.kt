package io.projectenv.intellijplugin.services

import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.replaceService
import io.projectenv.core.commons.archive.ArchiveExtractorFactory
import io.projectenv.core.commons.download.DownloadUrlSubstitutorFactory
import io.projectenv.core.commons.download.ImmutableDownloadUrlDictionary
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue
import io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName
import io.projectenv.core.commons.system.CPUArchitecture
import io.projectenv.core.commons.system.OperatingSystem
import io.projectenv.intellijplugin.services.impl.PROJECT_ENV_NOTIFICATION_GROUP_NAME
import io.projectenv.intellijplugin.services.impl.ProjectEnvCliResolverServiceImpl
import io.projectenv.intellijplugin.services.impl.getProjectEnvNotificationGroup
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.Map

const val PROJECT_ENV_CLI_VERSION = "3.0.3"

class ProjectEnvServiceIT : HeavyPlatformTestCase() {

    private var tempDirectory: File? = null
    private var notificationGroup: NotificationGroup? = null

    override fun setUp() {
        super.setUp()

        tempDirectory = createTempDirectory()

        notificationGroup = spy(getProjectEnvNotificationGroup())

        val application = ApplicationManager.getApplication()
        val notificationGroupManager = spy(application.getService(NotificationGroupManager::class.java))
        whenever(notificationGroupManager.getNotificationGroup(PROJECT_ENV_NOTIFICATION_GROUP_NAME)).thenReturn(
            notificationGroup
        )
        application.replaceService(NotificationGroupManager::class.java, notificationGroupManager, testRootDisposable)
    }

    @Test
    fun testRefreshProjectEnv() {
        copyResourceToProjectRoot("project-env.toml")
        copyResourceToProjectRoot("build.gradle")

        val service = project.service<ProjectEnvService>()

        // the first time Project-Env is refreshed, the CLI is not available and therefore are warning is shown
        hideProjectEnvCli()
        service.refreshProjectEnv()
        verify(notificationGroup!!).createNotification(
            anyString(),
            eq(NotificationType.WARNING)
        )

        // the second time, the CLI is available and executed
        unhideProjectEnvCli()
        val pathElement = setupProjectEnvCli()
        withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute {
            service.refreshProjectEnv()

            assertMavenSettings()
            assertJdkSettings()
            assertNodeSettings()
            assertGradleSettings()
        }
    }

    override fun tearDown() {
        val jdk = ProjectRootManager.getInstance(project).projectSdk
        if (jdk != null) {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(jdk)
            }
        }

        FileUtils.forceDelete(tempDirectory)

        super.tearDown()
    }

    private fun setupProjectEnvCli(): File {
        val downloadUrl = getProjectEnvCliDownloadUrl()
        val tempArchiveFile = getTempArchiveFile(downloadUrl)
        downloadArchive(downloadUrl, tempArchiveFile)

        val tempArchiveExtractionDirectory = getTempArchiveExtractionDirectory(downloadUrl)
        extractArchive(tempArchiveFile, tempArchiveExtractionDirectory)

        return tempArchiveExtractionDirectory
    }

    private fun getProjectEnvCliDownloadUrl(): String {
        val dictionary = ImmutableDownloadUrlDictionary.builder()
            .putParameters("VERSION", PROJECT_ENV_CLI_VERSION)
            .putOperatingSystemSpecificParameters(
                "OS",
                Map.of(
                    OperatingSystem.MACOS, "macos",
                    OperatingSystem.LINUX, "linux",
                    OperatingSystem.WINDOWS, "windows"
                )
            )
            .putOperatingSystemSpecificParameters(
                "FILE_EXT",
                Map.of(
                    OperatingSystem.MACOS, "tar.gz",
                    OperatingSystem.LINUX, "tar.gz",
                    OperatingSystem.WINDOWS, "zip"
                )
            )
            .putCPUArchitectureSpecificParameters(
                "CPU_ARCH",
                Map.of(
                    CPUArchitecture.X64, "amd64"
                )
            )
            .build()

        return DownloadUrlSubstitutorFactory
            .createDownloadUrlVariableSubstitutor(dictionary)
            .replace("https://github.com/Project-Env/project-env-core/releases/download/v\${VERSION}/cli-\${VERSION}-\${OS}-\${CPU_ARCH}.\${FILE_EXT}")
    }

    private fun getTempArchiveFile(downloadUrl: String): File {
        val archiveFilename = FilenameUtils.getName(downloadUrl)

        return File.createTempFile("junit", archiveFilename, tempDirectory)
    }

    private fun getTempArchiveExtractionDirectory(downloadUrl: String): File {
        val archiveFilename = FilenameUtils.getName(downloadUrl)
        val archiveFileExtension = FilenameUtils.getExtension(downloadUrl)

        return createTempDirectory(archiveFilename.removeSuffix(".$archiveFileExtension"), tempDirectory)
    }

    private fun downloadArchive(downloadUrl: String, target: File) {
        BufferedInputStream(URI(downloadUrl).toURL().openStream()).use { inputStream ->
            FileOutputStream(target).use { outputStream ->
                IOUtils.copy(
                    inputStream,
                    outputStream
                )
            }
        }
    }

    private fun extractArchive(archive: File, target: File) {
        ArchiveExtractorFactory.createArchiveExtractor().extractArchive(archive, target)
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

    private fun assertGradleSettings() {
        linkAndRefreshGradleProject(getProjectRoot().canonicalPath, project)

        val linkedProjectsSettings = GradleSettings.getInstance(project).linkedProjectsSettings
        assertThat(linkedProjectsSettings).hasSize(1)

        val projectSettings = linkedProjectsSettings.first()
        assertToolPath(projectSettings.gradleHome)
    }

    private fun assertToolPath(path: String?) {
        assertThat(path).startsWith(getProjectRoot().canonicalPath)
        assertThat(File(path!!)).exists()
    }

    private fun copyResourceToProjectRoot(resource: String): File {
        val resultingFile = File(getProjectRoot(), resource)
        FileUtils.forceMkdirParent(resultingFile)

        javaClass.getResourceAsStream(resource).use { inputStream ->
            FileOutputStream(resultingFile).use { outputStream ->
                IOUtils.copy(inputStream, outputStream)
            }
        }

        return resultingFile
    }

    fun hideProjectEnvCli() {
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

    fun unhideProjectEnvCli() {
        project.replaceService(ProjectEnvCliResolverService::class.java, ProjectEnvCliResolverServiceImpl(), project)
    }

    fun createTempDirectory(suffix: String? = null, parent: File? = null): File {
        val temporaryFolder = File.createTempFile("junit", suffix, parent)
        assertThat(temporaryFolder.delete()).isTrue()
        FileUtils.forceMkdir(temporaryFolder)

        return temporaryFolder
    }

    private fun getProjectRoot(): File {
        return File(project.basePath!!)
    }
}

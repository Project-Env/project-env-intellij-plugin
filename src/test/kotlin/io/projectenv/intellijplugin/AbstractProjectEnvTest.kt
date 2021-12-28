package io.projectenv.intellijplugin

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.replaceService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.mockito.ArgumentMatchers.startsWith
import org.mockito.Mockito.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.io.File
import java.io.FileOutputStream

abstract class AbstractProjectEnvTest : HeavyPlatformTestCase() {

    private var notificationGroup: NotificationGroup? = null

    override fun setUp() {
        super.setUp()

        val application = ApplicationManager.getApplication()

        val notificationGroupManager = application.getService(NotificationGroupManager::class.java)
        val notificationGroup = notificationGroupManager.getNotificationGroup("Project-Env")

        val spiedNotificationGroupManager = spy(notificationGroupManager)
        this.notificationGroup = spy(notificationGroup)

        whenever(spiedNotificationGroupManager.getNotificationGroup("Project-Env")).thenReturn(
            this.notificationGroup
        )

        application.replaceService(
            NotificationGroupManager::class.java,
            spiedNotificationGroupManager,
            testRootDisposable
        )

        createProjectRootAndRefresh()
    }

    private fun createProjectRootAndRefresh() {
        FileUtils.forceMkdir(getProjectRoot())

        VirtualFileManager.getInstance().refreshAndFindFileByNioPath(getProjectRoot().toPath())
    }

    override fun tearDown() {
        val jdk = ProjectRootManager.getInstance(project).projectSdk
        if (jdk != null) {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(jdk)
            }
        }

        super.tearDown()
    }

    protected fun copyResourceToProjectRootAndRefresh(resource: String, target: String? = null): File {
        val resultingFile = File(getProjectRoot(), target ?: resource)
        FileUtils.forceMkdirParent(resultingFile)

        javaClass.getResourceAsStream(resolveResourceLocation(resource)).use { inputStream ->
            FileOutputStream(resultingFile).use { outputStream ->
                IOUtils.copy(inputStream, outputStream)
            }
        }

        VirtualFileManager.getInstance()
            .refreshAndFindFileByNioPath(resultingFile.toPath())
            ?.refresh(false, false)

        return resultingFile
    }

    private fun resolveResourceLocation(resource: String): String {
        return "${getResourcesBasePath()}/$resource"
    }

    private fun getResourcesBasePath(): String {
        return "/${javaClass.name.replace('.', '/')}"
    }

    protected fun getProjectRoot(): File {
        return File(project.basePath!!)
    }

    protected fun assertNotificationFired(content: String, type: NotificationType = NotificationType.INFORMATION) {
        verify(notificationGroup)!!.createNotification(
            startsWith(content),
            eq(type)
        )
        verify(notificationGroup)!!.createNotification(
            eq(""),
            startsWith(content),
            eq(type)
        )
        verify(notificationGroup)!!.icon
        verifyNoMoreInteractions(notificationGroup)
        clearFiredNotifications()
    }

    protected fun assertNoNotificationFired() {
        verifyNoMoreInteractions(notificationGroup)
        clearFiredNotifications()
    }

    protected fun clearFiredNotifications() {
        clearInvocations(notificationGroup)
    }
}

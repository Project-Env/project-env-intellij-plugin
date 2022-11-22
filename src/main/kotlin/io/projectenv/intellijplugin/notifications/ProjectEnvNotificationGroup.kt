package io.projectenv.intellijplugin.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.ProjectEnvPluginIcons
import io.projectenv.intellijplugin.services.ProjectEnvService

object ProjectEnvNotificationGroup {

    fun showReloadNotification(reason: String, project: Project) {
        val notification = createNotification(
            reason,
            NotificationType.INFORMATION
        )

        notification.addAction(
            NotificationAction.create("Reload Project-Env") { _ ->
                notification.expire()
                project.service<ProjectEnvService>().refreshProjectEnv()
            }
        )

        notification.notify(project)
    }

    fun createNotification(content: String, type: NotificationType): Notification {
        val notificationGroup = getNotificationGroup()

        val notification = notificationGroup.createNotification(content, type)
        notification.icon = ProjectEnvPluginIcons.Default

        return notification
    }

    private fun getNotificationGroup(): NotificationGroup {
        return NotificationGroupManager.getInstance()
            .getNotificationGroup("Project-Env")
    }
}

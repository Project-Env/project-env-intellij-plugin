package io.projectenv.intellijplugin.services.impl

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager

const val PROJECT_ENV_NOTIFICATION_GROUP_NAME = "Project-Env"

fun getProjectEnvNotificationGroup(): NotificationGroup {
    return NotificationGroupManager.getInstance()
        .getNotificationGroup(PROJECT_ENV_NOTIFICATION_GROUP_NAME)
}

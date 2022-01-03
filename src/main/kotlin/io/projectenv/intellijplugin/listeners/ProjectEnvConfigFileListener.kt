package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import io.projectenv.intellijplugin.notifications.ProjectEnvNotificationGroup
import io.projectenv.intellijplugin.services.ProjectEnvConfigFileResolverService
import org.apache.commons.lang3.StringUtils

class ProjectEnvConfigFileListener(val project: Project) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        val projectEnvConfigFileUrl = getProjectEnvConfigFileUrl()

        for (event in events) {
            val virtualFile = event.file ?: continue
            if (StringUtils.equals(projectEnvConfigFileUrl, virtualFile.url)) {
                ProjectEnvNotificationGroup.showReloadNotification(
                    "Project-Env config file has been updated",
                    project
                )

                return
            }
        }
    }

    private fun getProjectEnvConfigFileUrl(): String? {
        val projectEnvConfigFile = project.service<ProjectEnvConfigFileResolverService>().resolveConfig() ?: return null

        return VirtualFileManager.getInstance()
            .findFileByNioPath(projectEnvConfigFile.toPath())?.url
    }
}

package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import io.projectenv.intellijplugin.notifications.ProjectEnvNotificationGroup
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import java.io.File

class ProjectEnvFilesListener(val project: Project) : ProjectEnvToolsListener {

    var relevantFileUrls: List<String> = emptyList()

    init {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    if (relevantFilesUpdated(events)) {
                        ProjectEnvNotificationGroup.showReloadNotification(
                            "Project-Env relevant files have been updated",
                            project
                        )
                    }
                }
            }
        )
    }

    private fun relevantFilesUpdated(events: List<VFileEvent>): Boolean {
        for (event in events) {
            val virtualFile = event.file ?: continue
            if (relevantFileUrls.contains(virtualFile.url)) {
                return true
            }
        }

        return false
    }

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val relevantFileUrls = ArrayList<String>()
        for (infos in toolInfos.allToolInfos) {
            for (info in infos.value) {
                for (handledProjectResource in info.handledProjectResources) {
                    addRelevantFile(handledProjectResource, relevantFileUrls)
                }
            }
        }

        this.relevantFileUrls = relevantFileUrls
    }

    private fun addRelevantFile(relevantFile: File, relevantFileUrls: MutableList<String>) {
        val url = getVirtualFileUrlForFile(relevantFile)
        if (url != null) {
            relevantFileUrls.add(url)
        }
    }

    private fun getVirtualFileUrlForFile(file: File): String? {
        return VirtualFileManager.getInstance()
            .findFileByNioPath(file.toPath())?.url
    }
}

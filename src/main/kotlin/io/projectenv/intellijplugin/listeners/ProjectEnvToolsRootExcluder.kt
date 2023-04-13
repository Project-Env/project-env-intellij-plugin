package io.projectenv.intellijplugin.listeners

import com.intellij.ProjectTopics
import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import io.projectenv.intellijplugin.toolinfo.ToolInfos

class ProjectEnvToolsRootExcluder(val project: Project) : ProjectEnvToolsListener {

    private var binaryRoots: Set<VirtualFile> = emptySet()

    init {
        project.messageBus.connect().subscribe(
            ProjectTopics.MODULES,
            object : ModuleListener {
                override fun modulesAdded(project: Project, modules: List<Module>) {
                    for (module in modules) {
                        addExcludeFolders(module, binaryRoots)
                    }
                }
            }
        )
    }

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val oldBinaryRoots = binaryRoots
        val newBinaryRoots = getBinaryRoots(toolInfos)

        val removedRoots = oldBinaryRoots.minus(newBinaryRoots)
        removeExcludeFolders(removedRoots)

        addExcludeFolders(newBinaryRoots)

        binaryRoots = newBinaryRoots
    }

    private fun getBinaryRoots(toolInfos: ToolInfos): Set<VirtualFile> {
        val roots = HashSet<VirtualFile>()
        for (infos in toolInfos.allToolInfos.values) {
            for (info in infos) {
                if (info.toolBinariesRoot == null || info.toolBinariesRoot.isEmpty) {
                    continue
                }

                val toolBinariesRootAsVirtualFile = VirtualFileManager.getInstance()
                    .refreshAndFindFileByNioPath(info.toolBinariesRoot.get().toPath()) ?: continue

                roots.add(toolBinariesRootAsVirtualFile)
            }
        }

        return roots
    }

    private fun addExcludeFolders(directories: Set<VirtualFile>) {
        if (directories.isEmpty()) {
            return
        }

        for (module in ModuleManager.getInstance(project).modules) {
            addExcludeFolders(module, directories)
        }
    }

    private fun addExcludeFolders(module: Module, directories: Set<VirtualFile>) {
        forEachContentEntry(module, directories) { contentEntry, directory ->
            contentEntry.addExcludeFolder(directory.url)
        }
    }

    private fun removeExcludeFolders(directories: Set<VirtualFile>) {
        if (directories.isEmpty()) {
            return
        }

        for (module in ModuleManager.getInstance(project).modules) {
            removeExcludeFolders(module, directories)
        }
    }

    private fun removeExcludeFolders(module: Module, directories: Set<VirtualFile>) {
        forEachContentEntry(module, directories) { contentEntry, directory ->
            contentEntry.removeExcludeFolder(directory.url)
        }
    }

    private fun forEachContentEntry(
        module: Module,
        directories: Set<VirtualFile>,
        callback: (ContentEntry, VirtualFile) -> Any
    ) {
        ModuleRootModificationUtil.updateModel(module) { model ->
            for (directory in directories) {
                val contentEntry = MarkRootActionBase.findContentEntry(model, directory)
                if (contentEntry != null) {
                    callback.invoke(contentEntry, directory)
                }
            }
        }
    }
}

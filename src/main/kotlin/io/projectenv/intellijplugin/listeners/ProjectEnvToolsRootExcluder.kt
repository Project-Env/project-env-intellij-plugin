package io.projectenv.intellijplugin.listeners

import com.intellij.ProjectTopics
import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import java.io.File

class ProjectEnvToolsRootExcluder(val project: Project) : ProjectEnvToolsListener {

    private var binaryRoots: Map<File, VirtualFile> = emptyMap()

    init {
        project.messageBus.connect().subscribe(
            ProjectTopics.MODULES,
            object : ModuleListener {
                override fun moduleAdded(project: Project, module: Module) {
                    addExcludeFolders(module, binaryRoots)
                }
            }
        )
    }

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val oldBinaryRoots = binaryRoots
        val newBinaryRoots = getBinaryRoots(toolInfos)

        val removedRoots: Map<File, VirtualFile> = oldBinaryRoots.minus(newBinaryRoots.keys)
        removeExcludeFolders(removedRoots)

        addExcludeFolders(newBinaryRoots)

        binaryRoots = newBinaryRoots
    }

    private fun getBinaryRoots(toolInfos: ToolInfos): Map<File, VirtualFile> {
        val roots = HashMap<File, VirtualFile>()
        for (infos in toolInfos.allToolInfos.values) {
            for (info in infos) {
                if (info.toolBinariesRoot == null) {
                    continue
                }

                val toolBinariesRootAsVirtualFile = LocalFileSystem.getInstance()
                    .refreshAndFindFileByIoFile(info.toolBinariesRoot) ?: continue

                roots[info.toolBinariesRoot] = toolBinariesRootAsVirtualFile
            }
        }

        return roots
    }

    private fun addExcludeFolders(directories: Map<File, VirtualFile>) {
        if (directories.isEmpty()) {
            return
        }

        for (module in ModuleManager.getInstance(project).modules) {
            addExcludeFolders(module, directories)
        }
    }

    private fun addExcludeFolders(module: Module, directories: Map<File, VirtualFile>) {
        forEachContentEntry(module, directories) { contentEntry, directory ->
            contentEntry.addExcludeFolder(fileToUrl(directory.key))
        }
    }

    private fun removeExcludeFolders(directories: Map<File, VirtualFile>) {
        if (directories.isEmpty()) {
            return
        }

        for (module in ModuleManager.getInstance(project).modules) {
            removeExcludeFolders(module, directories)
        }
    }

    private fun removeExcludeFolders(module: Module, directories: Map<File, VirtualFile>) {
        forEachContentEntry(module, directories) { contentEntry, directory ->
            contentEntry.removeExcludeFolder(fileToUrl(directory.key))
        }
    }

    private fun forEachContentEntry(
        module: Module,
        directories: Map<File, VirtualFile>,
        callback: (ContentEntry, Map.Entry<File, VirtualFile>) -> Any
    ) {
        ModuleRootModificationUtil.updateModel(module) { model ->
            for (directory in directories) {
                val contentEntry = MarkRootActionBase.findContentEntry(model, directory.value)
                if (contentEntry != null) {
                    callback.invoke(contentEntry, directory)
                }
            }
        }
    }

    private fun fileToUrl(file: File): String {
        return file.toPath().toUri().toString()
    }
}

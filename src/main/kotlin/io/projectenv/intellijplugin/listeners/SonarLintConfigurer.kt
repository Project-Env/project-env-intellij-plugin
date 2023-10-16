package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.toolinfo.ToolInfos
import org.sonarlint.intellij.config.Settings

class SonarLintConfigurer(val project: Project) : ProjectEnvToolsListener {

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val nodejsInfo = toolInfos.getToolInfo("nodejs") ?: return

        WriteAction.runAndWait<Throwable> {
            // This is obviously not intended to be used like this, but for now there seems to be no easy way
            // of updating the path correctly. For now this solves at least the issue that you have to configure
            // the path manually.
            Settings.getGlobalSettings().nodejsPath = nodejsInfo.primaryExecutable.get().canonicalPath
        }
    }
}

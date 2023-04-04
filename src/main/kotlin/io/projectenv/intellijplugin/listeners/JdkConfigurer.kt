package io.projectenv.intellijplugin.listeners

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import io.projectenv.intellijplugin.toolinfo.ToolInfos

class JdkConfigurer(val project: Project) : ProjectEnvToolsListener {

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val jdkInfo = toolInfos.getToolInfo("jdk") ?: return

        WriteAction.runAndWait<Throwable> {
            val jdkName = createJdkName()
            val newJdk = JavaSdk.getInstance().createJdk(jdkName, jdkInfo.toolBinariesRoot.get().canonicalPath, false)

            val oldJdk = ProjectJdkTable.getInstance().findJdk(jdkName)
            if (oldJdk != null) {
                ProjectJdkTable.getInstance().updateJdk(oldJdk, newJdk)
            } else {
                ProjectJdkTable.getInstance().addJdk(newJdk)
            }

            ProjectRootManager.getInstance(project).projectSdk = newJdk
        }
    }

    private fun createJdkName(): String {
        return "JDK of project ${project.name} (Project-Env)"
    }
}

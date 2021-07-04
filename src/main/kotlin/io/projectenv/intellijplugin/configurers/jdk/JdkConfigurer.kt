package io.projectenv.intellijplugin.configurers.jdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import io.projectenv.core.cli.api.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer

class JdkConfigurer(val project: Project) : ToolConfigurer {

    override fun getToolIdentifier(): String {
        return "jdk"
    }

    override fun configureTool(toolInfo: ToolInfo) {
        ApplicationManager.getApplication().runWriteAction {
            val jdkName = createJdkName()
            removeOldJdk(jdkName)
            createNewJdk(toolInfo, jdkName)
        }
    }

    private fun createJdkName(): String {
        return "JDK of project ${project.name} (Project-Env)"
    }

    private fun removeOldJdk(jdkName: String) {
        val oldJdk = ProjectJdkTable.getInstance().findJdk(jdkName)
        if (oldJdk != null) {
            ProjectJdkTable.getInstance().removeJdk(oldJdk)
        }
    }

    private fun createNewJdk(toolInfo: ToolInfo, jdkName: String) {
        val newJdk = JavaSdk.getInstance().createJdk(jdkName, toolInfo.toolBinariesRoot.get().canonicalPath)
        ProjectJdkTable.getInstance().addJdk(newJdk)
        ProjectRootManager.getInstance(project).projectSdk = newJdk
    }
}

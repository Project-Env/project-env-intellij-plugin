package io.projectenv.intellijplugin.configurers.jdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import io.projectenv.core.tools.info.JdkInfo
import io.projectenv.core.tools.info.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer

class JdkConfigurer(val project: Project) : ToolConfigurer<JdkInfo> {

    override fun supportsType(toolInfo: ToolInfo): Boolean {
        return toolInfo is JdkInfo
    }

    override fun configureTool(toolInfo: JdkInfo) {
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

    private fun createNewJdk(toolInfo: JdkInfo, jdkName: String) {
        val newJdk = JavaSdk.getInstance().createJdk(jdkName, toolInfo.location.canonicalPath)
        ProjectJdkTable.getInstance().addJdk(newJdk)
        ProjectRootManager.getInstance(project).projectSdk = newJdk
    }
}

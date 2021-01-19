package ch.repolevedavaj.projectenv.intellijplugin.configurers.jdk

import ch.projectenv.core.toolinfo.JdkInfo
import ch.projectenv.core.toolinfo.ToolInfo
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager

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

package ch.repolevedavaj.projectenv.intellijplugin.configurers.jdk

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager

class JdkConfigurer(val project: Project) : ToolConfigurer {

    override fun supportsType(type: ProjectToolType): Boolean {
        return type == ProjectToolType.JDK
    }

    override fun configureTool(toolDetails: ProjectToolDetails) {
        ApplicationManager.getApplication().runWriteAction {
            val jdkName = createJdkName()
            removeOldJdk(jdkName)
            createNewJdk(toolDetails, jdkName)
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

    private fun createNewJdk(toolDetails: ProjectToolDetails, jdkName: String) {
        val newJdk = JavaSdk.getInstance().createJdk(jdkName, toolDetails.location.canonicalPath)
        ProjectJdkTable.getInstance().addJdk(newJdk)
        ProjectRootManager.getInstance(project).projectSdk = newJdk
    }
}

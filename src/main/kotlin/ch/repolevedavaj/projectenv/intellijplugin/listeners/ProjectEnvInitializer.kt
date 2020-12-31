package ch.repolevedavaj.projectenv.intellijplugin.listeners

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import ch.repolevedavaj.projectenv.core.configuration.ConfigurationFactory
import ch.repolevedavaj.projectenv.core.configuration.ProjectEnv
import ch.repolevedavaj.projectenv.core.installer.ToolInstallerCollection
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import org.apache.commons.io.FileUtils
import org.jetbrains.idea.maven.execution.MavenRunner
import org.jetbrains.idea.maven.project.MavenProjectsManager
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class ProjectEnvInitializer : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        NotificationGroupManager.getInstance().getNotificationGroup("Project-Env")
            .createNotification("Setting up Project-Env...", NotificationType.INFORMATION)
            .notify(project)

        val file = File(project.basePath, "project-env.xml")
        val projectEnvConfiguration: ProjectEnv = ConfigurationFactory.createFromFile(file)

        val toolDetailsList = installTools(project, projectEnvConfiguration)
        configureIntelliJTools(project, toolDetailsList)
    }

    fun installTools(project: Project, projectEnvConfiguration: ProjectEnv): List<ProjectToolDetails> {
        val toolsDirectory = File(project.basePath, projectEnvConfiguration.tools.directory)
        FileUtils.forceMkdir(toolsDirectory)

        val installers = projectEnvConfiguration.tools.installers

        val toolDetails: MutableList<ProjectToolDetails> = ArrayList()
        if (installers.jdk != null) {
            toolDetails.add(ToolInstallerCollection.installTool(installers.jdk, toolsDirectory))
        }
        if (installers.maven != null) {
            toolDetails.add(ToolInstallerCollection.installTool(installers.maven, toolsDirectory))
        }
        if (installers.node != null) {
            toolDetails.add(ToolInstallerCollection.installTool(installers.node, toolsDirectory))
        }
        for (configuration in installers.generic) {
            toolDetails.add(ToolInstallerCollection.installTool(configuration, toolsDirectory))
        }

        return toolDetails
    }

    private fun configureIntelliJTools(project: Project, toolDetailsList: List<ProjectToolDetails>) {
        val projectEnvironment = extractAllExports(toolDetailsList)

        for (toolDetails in toolDetailsList) {
            when (toolDetails.type) {
                ProjectToolType.JDK -> configureIntelliJJdk(project, toolDetails, projectEnvironment)
                ProjectToolType.MAVEN -> configureIntelliJMaven(project, toolDetails, projectEnvironment)
                ProjectToolType.NODE -> configureIntelliJNode(project, toolDetails, projectEnvironment)
                else -> {
                    // ignore
                }
            }
        }
    }

    private fun configureIntelliJJdk(
        project: Project,
        jdkDetails: ProjectToolDetails,
        projectEnvironment: Map<String, String>
    ) {
        ApplicationManager.getApplication().runWriteAction {
            val jdkName = "JDK of project " + project.name + " (Project-Env)"

            // remove old JDK if existing
            val oldJdk = ProjectJdkTable.getInstance().findJdk(jdkName)
            if (oldJdk != null) {
                ProjectJdkTable.getInstance().removeJdk(oldJdk)
            }

            val newJdk = JavaSdk.getInstance().createJdk(jdkName, jdkDetails.location.canonicalPath)
            ProjectJdkTable.getInstance().addJdk(newJdk)
            ProjectRootManager.getInstance(project).projectSdk = newJdk
        }
    }

    private fun configureIntelliJMaven(
        project: Project,
        mavenDetails: ProjectToolDetails,
        projectEnvironment: Map<String, String>
    ) {
        ApplicationManager.getApplication().runWriteAction {
            MavenProjectsManager.getInstance(project).generalSettings.mavenHome = mavenDetails.location.canonicalPath
            MavenRunner.getInstance(project).settings.environmentProperties.putAll(projectEnvironment)
        }
    }

    private fun configureIntelliJNode(
        project: Project,
        nodeDetails: ProjectToolDetails,
        projectEnvironment: Map<String, String>
    ) {
        ApplicationManager.getApplication().runWriteAction {
            val interpreter = NodeJsLocalInterpreter(getNodeExecutablePath(nodeDetails).canonicalPath)

            NodeJsLocalInterpreterManager.getInstance().interpreters.stream().filter { existingInterpreter ->
                interpreter.interpreterSystemDependentPath == existingInterpreter.interpreterSystemDependentPath
            }
                .findFirst()
                .ifPresent { existingInterpreter ->
                    NodeJsLocalInterpreterManager.getInstance().interpreters.remove(existingInterpreter)
                }

            NodeJsLocalInterpreterManager.getInstance().interpreters.add(interpreter)
            NodeJsInterpreterManager.getInstance(project).setInterpreterRef(interpreter.toRef())
        }
    }

    private fun getNodeExecutablePath(nodeDetails: ProjectToolDetails): File {
        val executableName = if (SystemInfo.isWindows) "node.exe" else "node"
        for (pathElement in nodeDetails.pathElements) {
            val candidate = File(pathElement, executableName)
            if (candidate.exists()) {
                return candidate
            }
        }

        throw IllegalStateException()
    }

    private fun extractAllExports(toolDetailsList: List<ProjectToolDetails>): Map<String, String> {
        val exports = HashMap<String, String>()
        for (toolDetails in toolDetailsList) {
            for (export in toolDetails.exports) {
                exports[export.key] = export.value.canonicalPath
            }
        }

        return exports
    }

}

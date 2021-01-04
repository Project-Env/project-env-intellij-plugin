package ch.repolevedavaj.projectenv.intellijplugin.configurers.node

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import ch.repolevedavaj.projectenv.intellijplugin.configurers.ToolConfigurer
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File

class NodeConfigurer(val project: Project) : ToolConfigurer {

    override fun supportsType(type: ProjectToolType): Boolean {
        return type == ProjectToolType.NODE
    }

    override fun configureTool(toolDetails: ProjectToolDetails) {
        ApplicationManager.getApplication().runWriteAction {
            val interpreter = NodeJsLocalInterpreter(getNodeExecutablePath(toolDetails).canonicalPath)

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

    fun getNodeExecutablePath(nodeDetails: ProjectToolDetails): File {
        val executableName = if (SystemInfo.isWindows) "node.exe" else "node"
        for (pathElement in nodeDetails.pathElements) {
            val candidate = File(pathElement, executableName)
            if (candidate.exists()) {
                return candidate
            }
        }

        throw IllegalStateException()
    }


}
package io.projectenv.intellijplugin.configurers.node

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.projectenv.core.toolinfo.NodeInfo
import io.projectenv.core.toolinfo.ToolInfo
import io.projectenv.intellijplugin.configurers.ToolConfigurer

class NodeConfigurer(val project: Project) : ToolConfigurer<NodeInfo> {

    override fun supportsType(toolInfo: ToolInfo): Boolean {
        return toolInfo is NodeInfo
    }

    override fun configureTool(toolInfo: NodeInfo) {
        ApplicationManager.getApplication().runWriteAction {
            val interpreter = NodeJsLocalInterpreter(toolInfo.primaryExecutable.get().canonicalPath)

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
}

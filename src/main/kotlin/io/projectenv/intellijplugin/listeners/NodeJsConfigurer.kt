package io.projectenv.intellijplugin.listeners

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import io.projectenv.intellijplugin.toolinfo.ToolInfos

class NodeJsConfigurer(val project: Project) : ProjectEnvToolsListener {

    override fun toolsUpdated(toolInfos: ToolInfos) {
        val nodejsInfo = toolInfos.getToolInfo("nodejs") ?: return

        WriteAction.runAndWait<Throwable> {
            val interpreter = NodeJsLocalInterpreter(nodejsInfo.primaryExecutable!!.canonicalPath)

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

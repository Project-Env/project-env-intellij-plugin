package io.projectenv.intellijplugin.services.impl

import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import java.io.File

class ExecutionEnvironmentServiceImpl : ExecutionEnvironmentService {

    val exports = HashMap<String, String>()
    val pathElements = ArrayList<String>()

    override fun clear() {
        exports.clear()
        pathElements.clear()
    }

    override fun registerExport(name: String, value: File) {
        exports[name] = value.canonicalPath
    }

    override fun registerPathElement(pathElement: File) {
        pathElements.add(pathElement.canonicalPath)
    }

    override fun createEnvironment(): Map<String, String> {
        val environment = HashMap<String, String>()

        if (exports.isNotEmpty()) {
            environment.putAll(exports)
        }

        if (pathElements.isNotEmpty()) {
            environment["PATH"] = createPathVariableValue()
        }

        return environment
    }

    private fun createPathVariableValue(): String {
        val pathExtension = pathElements.joinToString(":")
        val pathBase = System.getenv("PATH")

        return "$pathExtension:$pathBase"
    }
}

package io.projectenv.intellijplugin.services.impl

import io.projectenv.core.commons.process.ProcessEnvironmentHelper.createProcessEnvironment
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService
import java.io.File

class ExecutionEnvironmentServiceImpl : ExecutionEnvironmentService {

    private val exports = HashMap<String, File>()
    private val pathElements = ArrayList<File>()

    override fun clear() {
        exports.clear()
        pathElements.clear()
    }

    override fun registerExport(name: String, value: File) {
        exports[name] = value
    }

    override fun registerPathElement(pathElement: File) {
        pathElements.add(pathElement)
    }

    override fun createEnvironment(): Map<String, String> {
        return createProcessEnvironment(exports, pathElements)
    }
}

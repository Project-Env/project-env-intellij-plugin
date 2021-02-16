package io.projectenv.intellijplugin.services

import java.io.File

interface ExecutionEnvironmentService {

    fun clear()

    fun registerExport(name: String, value: File)

    fun registerPathElement(pathElement: File)

    fun createEnvironment(): Map<String, String>
}

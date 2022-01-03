package io.projectenv.intellijplugin.services

import com.intellij.openapi.Disposable
import java.io.File

interface ProjectEnvConfigFileResolverService : Disposable {

    fun resolveConfig(): File?
}

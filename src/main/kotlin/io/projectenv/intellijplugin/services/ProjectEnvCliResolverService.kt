package io.projectenv.intellijplugin.services

import com.intellij.openapi.Disposable
import java.io.File

interface ProjectEnvCliResolverService : Disposable {

    fun resolveCli(): File?
}

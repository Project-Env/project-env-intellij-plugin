package io.projectenv.intellijplugin.services

import com.intellij.openapi.Disposable

interface ProjectEnvService : Disposable {

    fun refreshProjectEnv()
}

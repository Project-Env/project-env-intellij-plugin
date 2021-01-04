package ch.repolevedavaj.projectenv.intellijplugin.configurers

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface ToolConfigurer: Disposable {

    fun supportsType(type: ProjectToolType): Boolean

    fun configureTool(toolDetails: ProjectToolDetails)

    override fun dispose() {
        // noop
    }

}
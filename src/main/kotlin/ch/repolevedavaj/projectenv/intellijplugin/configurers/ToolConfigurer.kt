package ch.repolevedavaj.projectenv.intellijplugin.configurers

import ch.repolevedavaj.projectenv.core.ProjectToolDetails
import ch.repolevedavaj.projectenv.core.ProjectToolType
import com.intellij.openapi.Disposable

interface ToolConfigurer : Disposable {

    fun supportsType(type: ProjectToolType): Boolean

    fun configureTool(toolDetails: ProjectToolDetails)

    override fun dispose() {
        // noop
    }
}

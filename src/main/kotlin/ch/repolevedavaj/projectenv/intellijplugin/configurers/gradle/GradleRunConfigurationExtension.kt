package ch.repolevedavaj.projectenv.intellijplugin.configurers.gradle

import ch.repolevedavaj.projectenv.intellijplugin.services.ExecutionEnvironmentService
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfigurationExtension
import com.intellij.openapi.options.SettingsEditorGroup
import org.jdom.Element

class GradleRunConfigurationExtension : ExternalSystemRunConfigurationExtension {

    override fun updateVMParameters(
        configuration: ExternalSystemRunConfiguration,
        javaParameters: SimpleJavaParameters,
        settings: RunnerSettings?,
        executor: Executor
    ) {
        javaParameters.env.putAll(configuration.project.service<ExecutionEnvironmentService>().createEnvironment())
    }

    override fun readExternal(configuration: ExternalSystemRunConfiguration, element: Element) {
        // noop
    }

    override fun writeExternal(configuration: ExternalSystemRunConfiguration, element: Element) {
        // noop
    }

    override fun appendEditors(
        configuration: ExternalSystemRunConfiguration,
        group: SettingsEditorGroup<ExternalSystemRunConfiguration>
    ) {
        // noop
    }

    override fun attachToProcess(
        configuration: ExternalSystemRunConfiguration,
        processHandler: ExternalSystemProcessHandler,
        settings: RunnerSettings?
    ) {
        // noop
    }
}

package io.projectenv.intellijplugin.configurers.gradle

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.externalSystem.service.execution.configuration.ExternalSystemRunConfigurationExtension
import io.projectenv.intellijplugin.services.ExecutionEnvironmentService

class GradleRunConfigurationExtension : ExternalSystemRunConfigurationExtension() {

    override fun updateVMParameters(
        configuration: ExternalSystemRunConfiguration,
        javaParameters: SimpleJavaParameters,
        runnerSettings: RunnerSettings?,
        executor: Executor
    ) {
        javaParameters.env.putAll(configuration.project.service<ExecutionEnvironmentService>().createEnvironment())
    }
}

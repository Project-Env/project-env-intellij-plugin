package io.projectenv.intellijplugin.services

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.projectenv.intellijplugin.services.impl.ExecutionEnvironmentServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class ExecutionEnvironmentServiceImplTest {

    @Test
    fun testBehavior() {
        val service = ExecutionEnvironmentServiceImpl()
        assertThat(service.createEnvironment()).isEmpty()

        service.registerExport("key", File("export"))
        service.registerPathElement(File("path"))

        val environment = service.createEnvironment()
        assertThat(environment).hasSize(2)
            .hasEntrySatisfying("key") {
                assertThat(it).isEqualTo(File("export").canonicalPath)
            }
            .hasEntrySatisfying("PATH") {
                assertThat(it).startsWith("${File("path").canonicalPath}:")
            }

        service.clear()
        assertThat(service.createEnvironment()).isEmpty()
    }
}

package io.projectenv.intellijplugin.services

import io.projectenv.intellijplugin.services.impl.ExecutionEnvironmentServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class ExecutionEnvironmentServiceImplTest {

    @Test
    fun testBehavior() {
        val service = ExecutionEnvironmentServiceImpl()
        service.registerExport("key", File("export"))
        service.registerPathElement(File("path"))

        val environment = service.createEnvironment()
        assertThat(environment)
            .hasEntrySatisfying("key") {
                assertThat(it).isEqualTo(File("export").canonicalPath)
            }
            .hasEntrySatisfying("PATH") {
                assertThat(it).startsWith("${File("path").canonicalPath}:")
            }

        service.clear()
        assertThat(service.createEnvironment()).doesNotContainKey("key")
    }
}

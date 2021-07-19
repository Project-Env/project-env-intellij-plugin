package io.projectenv.intellijplugin.services.impl

import io.projectenv.core.commons.process.ProcessEnvironmentHelper
import io.projectenv.intellijplugin.services.ProjectEnvCliResolverService
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.util.Collections

private const val PROJECT_ENV_CLI_NAME = "project-env-cli"

class ProjectEnvCliResolverServiceImpl : ProjectEnvCliResolverService {

    override fun resolveCli(): File? {
        return ProcessEnvironmentHelper.resolveExecutableFromPathElements(
            PROJECT_ENV_CLI_NAME,
            getProjectEnvCliExecutableLocationCandidates()
        )
    }

    override fun dispose() {
        // noop
    }

    private fun getProjectEnvCliExecutableLocationCandidates(): ArrayList<File> {
        val candidates = ArrayList<File>()
        candidates.addAll(ProcessEnvironmentHelper.getPathElements())
        candidates.addAll(getKnownExecutableLocations())

        return candidates
    }

    private fun getKnownExecutableLocations(): List<File> {
        return if (!SystemUtils.IS_OS_WINDOWS) {
            Collections.singletonList(File("/usr/local/bin"))
        } else {
            Collections.emptyList()
        }
    }
}

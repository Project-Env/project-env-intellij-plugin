package io.projectenv.intellijplugin

import io.projectenv.core.commons.archive.ArchiveExtractorFactory
import io.projectenv.core.commons.download.DownloadUrlSubstitutorFactory
import io.projectenv.core.commons.download.ImmutableDownloadUrlDictionary
import io.projectenv.core.commons.system.CPUArchitecture
import io.projectenv.core.commons.system.OperatingSystem
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI

object ProjectEnvCliHelper {

    fun setupProjectEnvCli(version: String, tempDirectory: File): File {
        val downloadUrl = getProjectEnvCliDownloadUrl(version)
        val tempArchiveFile = getTempArchiveFile(tempDirectory, downloadUrl)
        downloadArchive(downloadUrl, tempArchiveFile)

        val tempArchiveExtractionDirectory = getTempArchiveExtractionDirectory(tempDirectory, downloadUrl)
        extractArchive(tempArchiveFile, tempArchiveExtractionDirectory)

        return tempArchiveExtractionDirectory
    }

    private fun getProjectEnvCliDownloadUrl(version: String): String {
        val dictionary = ImmutableDownloadUrlDictionary.builder()
            .putParameters("VERSION", version)
            .putOperatingSystemSpecificParameters(
                "OS",
                mapOf(
                    OperatingSystem.MACOS to "macos",
                    OperatingSystem.LINUX to "linux",
                    OperatingSystem.WINDOWS to "windows"
                )
            )
            .putOperatingSystemSpecificParameters(
                "FILE_EXT",
                mapOf(
                    OperatingSystem.MACOS to "tar.gz",
                    OperatingSystem.LINUX to "tar.gz",
                    OperatingSystem.WINDOWS to "zip"
                )
            )
            .putCPUArchitectureSpecificParameters(
                "CPU_ARCH",
                mapOf(
                    CPUArchitecture.X64 to "amd64"
                )
            )
            .build()

        return DownloadUrlSubstitutorFactory
            .createDownloadUrlVariableSubstitutor(dictionary)
            .replace("https://github.com/Project-Env/project-env-core/releases/download/v\${VERSION}/cli-\${VERSION}-\${OS}-\${CPU_ARCH}.\${FILE_EXT}")
    }

    private fun getTempArchiveFile(tempDirectory: File, downloadUrl: String): File {
        val archiveFilename = FilenameUtils.getName(downloadUrl)

        return File.createTempFile("junit", archiveFilename, tempDirectory)
    }

    private fun getTempArchiveExtractionDirectory(tempDirectory: File, downloadUrl: String): File {
        val archiveFilename = FilenameUtils.getName(downloadUrl)
        val archiveFileExtension = FilenameUtils.getExtension(downloadUrl)

        return createTempDirectory(archiveFilename.removeSuffix(".$archiveFileExtension"), tempDirectory)
    }

    private fun downloadArchive(downloadUrl: String, target: File) {
        BufferedInputStream(URI(downloadUrl).toURL().openStream()).use { inputStream ->
            FileOutputStream(target).use { outputStream ->
                IOUtils.copy(
                    inputStream,
                    outputStream
                )
            }
        }
    }

    private fun extractArchive(archive: File, target: File) {
        ArchiveExtractorFactory.createArchiveExtractor().extractArchive(archive, target)
    }

    private fun createTempDirectory(suffix: String? = null, parent: File? = null): File {
        val temporaryFolder = File.createTempFile("junit", suffix, parent)
        Assertions.assertThat(temporaryFolder.delete()).isTrue
        FileUtils.forceMkdir(temporaryFolder)

        return temporaryFolder
    }
}

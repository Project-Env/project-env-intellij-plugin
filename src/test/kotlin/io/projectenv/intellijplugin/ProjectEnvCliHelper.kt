package io.projectenv.intellijplugin

import io.projectenv.core.commons.archive.ArchiveExtractorFactory
import io.projectenv.core.commons.system.CpuArchitecture
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
        return "https://github.com/Project-Env/project-env-core/releases/download/v$version/cli-$version-${getOsDownloadUrlName()}-${getCpuArchDownloadUrlName()}.${getFileExtDownloadUrlName()}"
    }

    private fun getOsDownloadUrlName(): String {
        return if (OperatingSystem.getCurrentOperatingSystem() == OperatingSystem.WINDOWS) {
            "windows"
        } else if (OperatingSystem.getCurrentOperatingSystem() == OperatingSystem.MACOS) {
            "macos"
        } else {
            "linux"
        }
    }

    private fun getCpuArchDownloadUrlName(): String {
        return if (CpuArchitecture.getCurrentCpuArchitecture() == CpuArchitecture.AMD64) {
            "amd64"
        } else {
            "aarch64"
        }
    }

    private fun getFileExtDownloadUrlName(): String {
        return if (OperatingSystem.getCurrentOperatingSystem() == OperatingSystem.WINDOWS) {
            "zip"
        } else {
            "tar.gz"
        }
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

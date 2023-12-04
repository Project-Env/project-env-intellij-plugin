import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // JaCoCo report support
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.16.0"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2"
    // Sonar support
    id("org.sonarqube") version "4.4.1.3373"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "projectEnvCommonsJava"
        url = uri("https://maven.pkg.github.com/Project-Env/project-env-commons-java")
        credentials(PasswordCredentials::class)
    }
    maven {
        name = "projectEnvCli"
        url = uri("https://maven.pkg.github.com/Project-Env/project-env-cli")
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    val projectEnvCliVersion = "3.16.0"
    implementation("io.projectenv.core:cli:$projectEnvCliVersion")

    val projectEnvCommonsVersion = "1.2.1"
    implementation("io.projectenv.commons:process:$projectEnvCommonsVersion")
    implementation("io.projectenv.commons:gson:$projectEnvCommonsVersion")
    testImplementation("io.projectenv.commons:archive:$projectEnvCommonsVersion")
    testImplementation("io.projectenv.commons:string-substitutor:$projectEnvCommonsVersion")

    implementation("io.sentry:sentry:6.32.0")

    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

sonarqube {
    properties {
        property("sonar.projectName", "project-env-intellij-plugin")
        property("sonar.projectKey", "Project-Env_project-env-intellij-plugin")
        property("sonar.organization", "project-env")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.exclusions", "**/*Exception.kt")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")
    }
}

tasks {
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set("")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        changeNotes.set(properties("pluginChangelogHtml"))
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.')[0]))
    }
}

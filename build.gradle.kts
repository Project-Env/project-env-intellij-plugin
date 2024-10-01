import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // JaCoCo report support
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    // intellij-platform-gradle-plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    id("org.jetbrains.intellij.platform") version "2.1.0"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.1"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    // Sonar support
    id("org.sonarqube") version "5.1.0.4882"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    maven {
        name = "projectEnvCli"
        url = uri("https://maven.pkg.github.com/Project-Env/project-env-cli")
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        bundledPlugins(properties("platformBundledPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }

    val projectEnvCliVersion = "3.19.0"
    implementation("io.projectenv.core:cli:$projectEnvCliVersion")

    implementation("io.sentry:sentry:7.14.0")

    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

// Configure intellij-platform-gradle-plugin.
intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")
        description = projectDir.resolve("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }
        changeNotes = properties("pluginChangelogHtml")
    }
    publishing {
        token = System.getenv("PUBLISH_TOKEN")
        channels = listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.')[0])
    }
}

sonar {
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
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(it))
        }
    }

    test {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

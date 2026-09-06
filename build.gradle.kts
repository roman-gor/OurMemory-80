import java.util.Properties
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.detekt)
}

/**
 * Detekt analyzer configuration fields
 */

val projectSource = file(projectDir)
val configFile = files("$rootDir/config/detekt.yml")
val baselineFile = file("$rootDir/config/baseline.xml")
val kotlinFiles = "**/*.kt"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

/**
 * Task to run Detekt analysis for all project and included modules
 */

tasks.register("detektAll", Detekt::class) {
    val autoFix = project.hasProperty("detektAutoFix")

    description = "detekt build for all modules in this project"

    parallel = true
    ignoreFailures = false
    autoCorrect = autoFix
    buildUponDefaultConfig = true

    // To generate reports with relative paths
    basePath = projectDir.canonicalPath

    setSource(projectSource)
    baseline.set(baselineFile)
    config.setFrom(configFile)
    include(kotlinFiles)
    exclude(resourceFiles, buildFiles)
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
    }
}

/**
 * Task to generate baseline file and add all warnings to the SmellBaseline block
 */

tasks.register("detektGenerateBaseline", DetektCreateBaselineTask::class) {
    description = "build baseline for all modules in this project"

    parallel.set(true)
    ignoreFailures.set(false)
    buildUponDefaultConfig.set(true)

    setSource(projectSource)
    baseline.set(baselineFile)
    config.setFrom(configFile)
    include(kotlinFiles)
    exclude(resourceFiles, buildFiles)
}

dependencies {
    detektPlugins(libs.detekt.formatting.plugin)
}

fun getMapkitApiKey(): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties.getProperty("MAPKIT_API_KEY", "")
}

extra["mapkitApiKey"] = getMapkitApiKey()

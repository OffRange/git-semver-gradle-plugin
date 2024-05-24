plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.pluginPublish)
}

group = "de.davis"
version = "0.1.0"

dependencies {
    implementation(libs.semantic.versioning)
    implementation(libs.jGit)
    implementation(libs.kotlinpoet)
    implementation(libs.android.gradleApi)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = "https://github.com/OffRange/git-semver-gradle-plugin"
    vcsUrl = "https://github.com/OffRange/git-semver-gradle-plugin.git"
    plugins {
        create("versioning") {
            displayName = "Gradle Git Semantic Versioning Plugin"
            description =
                "A Gradle plugin that automatically versions your project based on Git tags, following the Semantic Versioning 2.0 specification."
            tags = listOf("git", "vcs", "semantic version", "semver", "versioning")
            id = "de.davis.git-semantic-versioning"
            implementationClass = "de.davis.gradle.plugin.versioning.VersioningPlugin"
        }
    }
}
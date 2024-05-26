package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import org.eclipse.jgit.api.Git
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*
import javax.inject.Inject

class VersioningPlugin @Inject constructor(private val objectFactory: ObjectFactory) : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val ext = extensions.create<VersioningExtension>(VersioningExtension.EXTENSION_NAME, objectFactory)

        val nextVersion = Git.open(rootDir).computeVersion(ext.channel, ext.defaultIncrement, ext.useShortHash)

        runInAndroidAppExtension {
            defaultConfig {
                versionName = nextVersion.toString()
                logger.lifecycle("Setting android version $versionName")
                versionCode = ext.run { nextVersion.versionCode }.toInt()
            }
        }

        afterEvaluate {
            version = nextVersion

            addDependencies()
            createVersionProviderFile(ext.versionCodeGenerator, nextVersion)

            tasks.register<VersionPrinter>("printVersion") {
                versionCodeGenerator = ext.versionCodeGenerator
                version = target.version as Version
            }
        }

        tasks.register("generateVersionProviderFile") {
            description =
                "Generates a Kotlin file that provides functions to receive the version of the current project"
            group = "versioning"

            doLast {
                createVersionProviderFile(ext.versionCodeGenerator, nextVersion)
            }
        }
    }

    private fun Project.addDependencies() {
        dependencies {
            implementation("io.github.z4kn4fein:semver:2.0.0")
        }
    }

    private fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
        runCatching {
            add("implementation", dependencyNotation)
        }
    }
}

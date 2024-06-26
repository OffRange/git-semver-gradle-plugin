package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*
import javax.inject.Inject

class VersioningPlugin @Inject constructor(private val objectFactory: ObjectFactory) : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val ext =
            extensions.create<VersioningExtension>(VersioningExtension.EXTENSION_NAME, objectFactory, rootDir, logger)


        afterEvaluate {
            val nextVersion = ext.computedVersion
            version = nextVersion

            addDependencies()
            createVersionProviderFile(nextVersion, ext.computedVersionCode)

            val versionPrinter = tasks.register<VersionPrinter>("printVersion") {
                versionCodeGenerator = ext.versionCodeGenerator
                version = target.version as Version
            }

            val generatorTask = tasks.register("generateVersionProviderFile") {
                description =
                    "Generates a Kotlin file that provides functions to receive the version of the current project"
                group = "versioning"

                dependsOn(versionPrinter.get())
                doLast {
                    createVersionProviderFile(nextVersion, ext.computedVersionCode)
                }
            }

            project.tasks.matching { it.name.startsWith("process") }.configureEach {
                dependsOn(generatorTask)
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

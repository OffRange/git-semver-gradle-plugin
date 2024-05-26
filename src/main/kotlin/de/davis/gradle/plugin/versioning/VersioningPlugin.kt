package de.davis.gradle.plugin.versioning

import com.android.build.api.dsl.ApplicationExtension
import io.github.z4kn4fein.semver.Version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import java.util.*
import javax.inject.Inject

class VersioningPlugin @Inject constructor(
    private val objectFactory: ObjectFactory,
) : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val ext = extensions.create<VersioningExtension>(VersioningExtension.EXTENSION_NAME, rootDir, objectFactory)

        afterEvaluate {
            version = ext.computeVersion()

            plugins.withId("com.android.application") {
                extensions.getByType<ApplicationExtension>().apply {
                    defaultConfig {
                        logger.lifecycle("Setting android version")
                        versionName = version.toString()
                        versionCode = ext.run { (version as Version).versionCode }.toInt()
                    }
                }
            }

            addDependencies()
            createVersionProviderFile(ext.versionCodeGenerator)

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
                createVersionProviderFile(ext.versionCodeGenerator)
            }
        }
    }

    private fun Project.addDependencies() {
        dependencies {
            implementation("io.github.z4kn4fein:semver:2.0.0")
        }
    }

    private fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
        add("implementation", dependencyNotation)
    }

    private fun Optional<Provider<MinimalExternalModuleDependency>>.dependencyNotation() = get().get()


}

package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class VersionPrinter : DefaultTask() {

    init {
        description = "Prints the current version of the project"
        group = "versioning"
    }

    @get:Input
    abstract val version: Property<Version>

    @get:Input
    abstract val versionCodeGenerator: Property<VersionCodeGenerator>


    private val lines = mutableListOf<Content>()

    @TaskAction
    fun execute() {
        lines.add(Content("Version Name", version.get()))
        lines.add(Content("Version Code", versionCodeGenerator.get()(version.get())))
        val maxDescription = lines.maxOf { it.description.length }
        val maxContent = lines.maxOf { it.content.toString().length }

        with(logger) {
            lifecycle("+" + "-".repeat(maxDescription + maxContent + 5) + "+")
            lines.forEach {
                lifecycle(
                    "| " + it.description.padEnd(maxDescription) +
                            " : " + it.content.toString().padEnd(maxContent) +
                            " |"
                )
            }
            lifecycle("+" + "-".repeat(maxDescription + maxContent + 5) + "+")
        }
    }
}

private data class Content(val description: String, val content: Any)

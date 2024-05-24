package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersionOrNull
import org.eclipse.jgit.api.Git


fun Git.getLatestCommit(short: Boolean): String = with(repository) {
    val parsedCommit = parseCommit(resolve("HEAD"))
    if (short) return@with parsedCommit.abbreviate(7).name()

    parsedCommit.name
}

fun Git.getLatestTag(): Version? =
    tagList().call().map { it.name.substringAfterLast("/").toVersionOrNull() }.sortedBy { it }.lastOrNull()


fun Git.commitsSinceLastTag() = log().apply {
    getLatestTag()?.let { not(repository.resolve(it.toString())) }
}.call().count()
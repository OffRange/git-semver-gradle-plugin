package de.davis.gradle.plugin.versioning

import org.eclipse.jgit.api.Git


fun Git.getLatestCommit(short: Boolean): String? = with(repository) {
    resolve("HEAD")?.let {
        val parsedCommit = parseCommit(resolve("HEAD"))
        if (short) return@with parsedCommit.abbreviate(7).name()

        parsedCommit.name
    }
}

fun Git.getLatestTag(): Version? =
    tagList().call().map { it.name.substringAfterLast("/").toVersionOrNull() }.sortedBy { it }.lastOrNull()

fun Git.getLatestTagName(): TagName? = tagList().call().map { it.name.substringAfterLast("/") }.lastOrNull()

fun Git.commitsSinceLastTag() = runCatching {
    log().apply {
        getLatestTagName()?.let { not(repository.resolve(it)) }
    }.call().count()
}.getOrNull()
package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.toVersionOrNull
import org.eclipse.jgit.api.Git

/**
 * Retrieves the latest commit hash from the Git repository.
 *
 * @receiver An instance of the [Git] class representing the current Git repository.
 * @param short Whether to return a shortened version of the commit hash (7 characters) or the full hash.
 * @return The commit hash of the latest commit, either shortened to 7 characters or in its full form.
 */
fun Git.getLatestCommit(short: Boolean): String? = with(repository) {
    resolve("HEAD")?.let {
        val parsedCommit = parseCommit(resolve("HEAD"))
        if (short) return@with parsedCommit.abbreviate(7).name()

        parsedCommit.name
    }
}

/**
 * Retrieves the name of the latest valid semantic version tag in the Git repository.
 *
 * @receiver An instance of the [Git] class representing the current Git repository.
 * @return The name of the latest tag, or null if no tags are found.
 */
fun Git.getLatestVersionTagName(): TagName? =
    tagList().call().map { it.name.substringAfterLast("/") }.sortedDescending()
        .firstNotNullOfOrNull { it.toVersionOrNull() }?.toString()


/**
 * Calculates the number of commits since the last tag in the Git repository.
 *
 * @receiver An instance of the [Git] class representing the current Git repository.
 * @return The number of commits since the last tag, or null if no HEAD exists.
 */
fun Git.commitsSinceLastTag() = runCatching {
    log().apply {
        getLatestVersionTagName()?.let { not(repository.resolve(it)) }
    }.call().count()
}.getOrNull()
package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.*
import io.github.z4kn4fein.semver.constraints.satisfiedBy
import io.github.z4kn4fein.semver.constraints.toConstraint
import org.eclipse.jgit.api.Git

/**
 * Computes the next version based on the current Git repository state and the specified [channel].
 *
 * The computed version is determined based on the following steps:
 *
 * 1. Retrieve the latest tag name from the Git repository. If no tag is found, use [minVersion].toString() as the last tag name.
 * 2. Retrieve the number of commits since the last tag using [commitsSinceLastTag].
 * 3. Retrieve the hash of the latest commit using [getLatestCommit].
 * 4. Call the [computeVersionInternal] function with the last tag name, a [CommitInfo] instance created from the commits since last tag and the latest commit hash, and the specified [channel].
 *
 * @receiver An instance of the [Git] class representing the current Git repository to retrieve the latest tag and commit information.
 * @param channel The release channel that the next version should have, defaults to [Channel.STABLE].
 * @param defaultIncrement The default increment type to apply when computing the next version (e.g., minor, patch), defaults to [Inc.MINOR].
 * @param useShortHash Flag indicating whether to use the short form of the commit hash, default is true.
 * @param minVersion The minimum version to use, defaults to [MIN_VERSION]
 * @return The computed version based on the Git repository state and the specified channel.
 * @see getLatestTagName
 * @see commitsSinceLastTag
 * @see getLatestCommit
 */
fun Git.computeVersion(
    channel: Channel = Channel.STABLE,
    defaultIncrement: Inc = Inc.MINOR,
    useShortHash: Boolean = true,
    minVersion: Version = MIN_VERSION
): Version {
    val lastTag = getLatestTagName() ?: minVersion.toString()
    val commitsSinceLastTag = commitsSinceLastTag() ?: 0
    val lastHash = getLatestCommit(useShortHash)
    return computeVersionInternal(
        lastTag,
        CommitInfo(commitsSinceLastTag, lastHash),
        channel,
        defaultIncrement,
        minVersion
    )
}


/**
 * Computes the next version based on the given [lastTagName], [commitInfo], [channel], [defaultIncrement], and [minVersion].
 *
 * @param lastTagName The name of the last tag in the repository.
 * @param commitInfo Information about the current commit, including the number of commits since the last tag and the hash of the current commit, defaults to [CommitInfo.LATEST].
 * @param channel The release channel for the next version (e.g., alpha, beta, rc, stable), defaults to [Channel.STABLE].
 * @param defaultIncrement The default increment type to apply when computing the next version (e.g., minor, patch), defaults to [Inc.MINOR].
 * @param minVersion The minimum version to use as the base version if the converted [lastTagName] is less than this value, defaults to [MIN_VERSION].
 * @return The next version of the project.
 */
internal fun computeVersionInternal(
    lastTagName: TagName,
    commitInfo: CommitInfo = CommitInfo.LATEST,
    channel: Channel = Channel.STABLE,
    defaultIncrement: Inc = Inc.MINOR,
    minVersion: Version = MIN_VERSION
): Version = with(commitInfo) {
    val constrainedVersion = lastTagName.toVersion().let {
        val alphaVersion = "${it.withoutSuffixes()}-alpha"
        if ("<$alphaVersion".toConstraint() satisfiedBy it) minVersion else it
    }
    if (commitsSinceTag == 0)
        return constrainedVersion

    val metadata = "dev.$commitsSinceTag.$hash"
    val preRelease = channel.channelName

    if (constrainedVersion == minVersion)
        return constrainedVersion.copy(
            preRelease = if (channel != Channel.STABLE) preRelease else null,
            buildMetadata = metadata
        )

    val lastVersionChannel = constrainedVersion.preRelease?.toChannel() ?: Channel.STABLE

    val incrementBy = getIncrementType(lastVersionChannel, channel, defaultIncrement)

    return constrainedVersion.inc(by = incrementBy, preRelease = preRelease).copy(buildMetadata = metadata).let {
        if (channel == Channel.STABLE)
            it.copy(preRelease = null)
        else
            it
    }
}

/**
 * Determines the appropriate increment type based on the [lastVersionChannel], [channel], and [defaultIncrement].
 *
 * If the [lastVersionChannel] is [Channel.STABLE] and [channel] is [Channel.STABLE], or if [lastVersionChannel]
 * is greater than [channel], the [defaultIncrement] increment type is returned. Otherwise, [Inc.PRE_RELEASE] is returned.
 *
 * @param lastVersionChannel The channel of the last version.
 * @param channel The release channel that the next version should have.
 * @param defaultIncrement The default increment type.
 * @return The appropriate increment type.
 */
private fun getIncrementType(lastVersionChannel: Channel, channel: Channel, defaultIncrement: Inc): Inc = when {
    (lastVersionChannel == Channel.STABLE && channel == Channel.STABLE) || lastVersionChannel > channel -> defaultIncrement
    else -> Inc.PRE_RELEASE
}

val MIN_VERSION = Version.min.inc(Inc.MINOR)
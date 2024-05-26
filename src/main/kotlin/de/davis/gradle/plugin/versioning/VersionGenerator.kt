package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.inc
import io.github.z4kn4fein.semver.toVersion
import org.eclipse.jgit.api.Git

/**
 * Computes the next version based on the current Git repository state and the specified [channel].
 *
 * The computed version is determined based on the following steps:
 *
 * 1. Retrieve the latest tag name from the Git repository. If no tag is found, use [MIN_VERSION].toString() as the last tag name.
 * 2. Retrieve the number of commits since the last tag using [commitsSinceLastTag].
 * 3. Retrieve the hash of the latest commit using [getLatestCommit].
 * 4. Call the [computeVersionInternal] function with the last tag name, a [CommitInfo] instance created from the commits since last tag and the latest commit hash, and the specified [channel].
 *
 * @receiver An instance of the [Git] class representing the current Git repository to retrieve the latest tag and commit information.
 * @param channel The release channel that the next version should have, defaults to [Channel.STABLE].
 * @param defaultIncrement The default increment type to apply when computing the next version (e.g., minor, patch), defaults to [Inc.MINOR].
 * @param useShortHash Flag indicating whether to use the short form of the commit hash, default is true.
 * @return The computed version based on the Git repository state and the specified channel.
 * @see getLatestTagName
 * @see commitsSinceLastTag
 * @see getLatestCommit
 */
fun Git.computeVersion(
    channel: Channel = Channel.STABLE,
    defaultIncrement: Inc = Inc.MINOR,
    useShortHash: Boolean = true
): Version {
    val lastTag = getLatestTagName() ?: MIN_VERSION.toString()
    val commitsSinceLastTag = commitsSinceLastTag() ?: 0
    val lastHash = getLatestCommit(useShortHash)
    return computeVersionInternal(lastTag, CommitInfo(commitsSinceLastTag, lastHash), channel, defaultIncrement)
}


/**
 * Computes the next version of the project based on the last git tag, commit info, and the release channel.
 *
 * @param lastTagName The name of the last tag in the repository.
 * @param commitInfo Information about the current commit, including the number of commits since the last tag and the hash of the current commit, defaults to [CommitInfo.LATEST].
 * @param channel The release channel for the next version (e.g., alpha, beta, rc, stable), defaults to [Channel.STABLE].
 * @param defaultIncrement The default increment type to apply when computing the next version (e.g., minor, patch), defaults to [Inc.MINOR].
 * @return The next version of the project.
 */
internal fun computeVersionInternal(
    lastTagName: TagName,
    commitInfo: CommitInfo = CommitInfo.LATEST,
    channel: Channel = Channel.STABLE,
    defaultIncrement: Inc = Inc.MINOR
): Version = with(commitInfo) {
    val lastTagVersion = lastTagName.toVersion()
    if (commitsSinceTag == 0)
        return lastTagVersion

    val metadata = "dev.$commitsSinceTag.$hash"
    val preRelease = channel.channelName

    if (lastTagVersion == MIN_VERSION)
        return lastTagVersion.copy(
            preRelease = if (channel != Channel.STABLE) preRelease else null,
            buildMetadata = metadata
        )

    val lastVersionChannel = lastTagVersion.preRelease?.toChannel() ?: Channel.STABLE

    val incrementBy = getIncrementType(lastVersionChannel, channel, defaultIncrement)

    return lastTagVersion.inc(by = incrementBy, preRelease = preRelease).copy(buildMetadata = metadata).let {
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
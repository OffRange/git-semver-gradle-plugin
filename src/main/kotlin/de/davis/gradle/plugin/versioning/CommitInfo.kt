package de.davis.gradle.plugin.versioning

/**
 * Represents information about a commit in a Git repository, including the number of commits since the last tag
 * and the commit hash.
 *
 * The [commitsSinceTag] property represents the number of commits since the last tag.
 * The [hash] property represents the commit hash of the last commit. If [commitsSinceTag] is non-zero,
 * [hash] must be provided.
 *
 * An [IllegalArgumentException] is thrown during object initialization if [commitsSinceTag] is non-zero and
 * [hash] is null.
 *
 * The companion object provides a constant [LATEST] that represents the latest commit (zero commits since the last tag).
 *
 * @property commitsSinceTag The number of commits since the last tag.
 * @property hash The commit hash of the last commit, or null if [commitsSinceTag] is zero.
 *
 * @constructor Creates a new instance of [CommitInfo] with the specified [commitsSinceTag] and [hash].
 *
 * @throws IllegalArgumentException If [commitsSinceTag] is non-zero and [hash] is null.
 */
data class CommitInfo(val commitsSinceTag: Int, val hash: String? = null) {
    init {
        require(commitsSinceTag == 0 || hash != null) { "The hash of the last commit must be provided" }
    }

    companion object {

        /**
         * A default instance representing the latest commit with zero commits since the last tag.
         */
        val LATEST = CommitInfo(0)
    }
}

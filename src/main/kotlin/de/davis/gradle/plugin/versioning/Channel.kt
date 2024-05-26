package de.davis.gradle.plugin.versioning

private const val MAX_VERSION_BUILD_NUMBER = 31u

/**
 * Enum class representing different release channels with associated build number constraints.
 *
 * @property maxVersionBuildNumber The maximum build number for the version within this channel.
 * @property startBuildCode The starting build code for the channel.
 */
enum class Channel(
    val maxVersionBuildNumber: UInt,
    private val startBuildCode: UInt
) {
    
    /**
     * Alpha release channel.
     */
    ALPHA(
        startBuildCode = 0u,
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    /**
     * Beta release channel.
     */
    BETA(
        startBuildCode = (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    /**
     * Release Candidate (RC) channel.
     */
    RC(
        startBuildCode = 2u * (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    /**
     * Stable release channel.
     */
    STABLE(
        startBuildCode = 3u * (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = 1u
    );

    /**
     * Computes the build code for a given build number within the constraints of this channel.
     *
     * @param buildNumber The build number to compute the build code for, default is 1.
     * @return The computed build code.
     * @throws IllegalArgumentException If the build number is not within the valid range for the channel.
     */
    fun computeBuildCode(buildNumber: UInt = 1u): UInt {
        require(buildNumber in (1u..maxVersionBuildNumber)) {
            "The specified build number must be between 1 and $maxVersionBuildNumber (inclusive) [1 ≤ x ≤ $maxVersionBuildNumber]"
        }

        return buildNumber - 1u + startBuildCode
    }

    /**
     * The lowercase name of the channel.
     */
    val channelName = name.lowercase()

    companion object {
        /**
         * Returns the Channel enum corresponding to the provided channel name.
         *
         * @param channelName The name of the channel.
         * @return The Channel enum corresponding to the channel name, or [STABLE] if the name is invalid.
         */
        operator fun invoke(channelName: String) = runCatching { valueOf(channelName.uppercase()) }.getOrElse { STABLE }
    }
}

/**
 * Extension function to convert a [TagName] to a [Channel].
 *
 * @receiver The [TagName] instance.
 * @return The corresponding [Channel].
 */
fun TagName.toChannel() = Channel(this.split('.')[0])
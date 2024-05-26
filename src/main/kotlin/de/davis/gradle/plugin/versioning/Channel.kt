package de.davis.gradle.plugin.versioning

private const val MAX_VERSION_BUILD_NUMBER = 31u

enum class Channel(
    val maxVersionBuildNumber: UInt,
    private val startBuildCode: UInt
) {

    ALPHA(
        startBuildCode = 0u,
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    BETA(
        startBuildCode = (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    RC(
        startBuildCode = 2u * (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = MAX_VERSION_BUILD_NUMBER
    ),

    STABLE(
        startBuildCode = 3u * (MAX_VERSION_BUILD_NUMBER + 1u),
        maxVersionBuildNumber = 1u
    );

    fun computeBuildCode(buildNumber: UInt = 1u): UInt {
        require(buildNumber in (1u..maxVersionBuildNumber)) {
            "The specified build number must be between 1 and $maxVersionBuildNumber (inclusive) [1 ≤ x ≤ $maxVersionBuildNumber]"
        }

        return buildNumber - 1u + startBuildCode
    }

    val channelName = name.lowercase()

    companion object {
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
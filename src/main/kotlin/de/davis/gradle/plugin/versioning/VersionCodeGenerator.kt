package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import kotlin.math.pow

/**
 * A type alias representing a function that generates a version code from a [Version] object.
 *
 * The function takes a [Version] object as input and returns a [UInt] value representing the generated version code.
 *
 * This type alias can be used to define custom version code generation strategies or to pass a version code generator
 * function as a parameter to other functions or classes.
 *
 * Example usage:
 * ```
 * val customGenerator: VersionCodeGenerator = { version ->
 *     // Custom version code generation logic
 *     generateCustomVersionCode(version)
 * }
 *
 * val versionCode = customGenerator(myVersion)
 * ```
 *
 * Or for the gradle plugin extension:
 * ```
 * versioning {
 *     versionCodeGenerator = ::customGenerator
 * }
 * ```
 */
internal typealias VersionCodeGenerator = (version: Version) -> UInt

/**
 * Generates an Android version code from the provided [Version] object.
 *
 * The version code is calculated based on the following rules:
 *
 * 1. If the [Version.preRelease] property is not null, the build code is computed using the [Channel.computeBuildCode]
 *    function, where the channel is derived from the first part of the pre-release string, and the build number is
 *    extracted from the second part (or defaulted to 1 if not present).
 * 2. If the [Version.preRelease] property is null, the build code is computed using [Channel.STABLE.computeBuildCode].
 * 3. The version code is then calculated by combining the major, minor, and patch components of the [Version] object,
 *    and adding the build code.
 *
 * The generated version code must not exceed the maximum allowed value of 2,100,000,000. Additionally, the major,
 * minor, and patch components of the [Version] object must not exceed their respective maximum allowed values:
 *
 * - Major: 290
 * - Minor: 999
 * - Patch: 99
 *
 * If any of these conditions are not met, an [IllegalArgumentException] is thrown with an appropriate error message.
 *
 * This function is specifically designed to generate version codes for Android applications, which have strict
 * requirements and limitations on the format and size of the version code.
 *
 * @receiver A function that takes a [Version] object as input and returns a [UInt] value representing the Android
 *           version code.
 * @param version The [Version] object from which to generate the Android version code.
 * @return The generated Android version code as a [UInt] value.
 * @throws IllegalArgumentException If the generated version code exceeds the maximum allowed value of 2,100,000,000,
 * or if the major, minor, or patch components exceed their respective maximum allowed values.
 */
@Suppress("FunctionName")
fun AndroidVersionCodeGenerator(version: Version) = with(version) {
    require(major <= 290) { "Major version exceeds the maximum allowed value of 290" }
    require(minor <= 999) { "Minor version exceeds the maximum allowed value of 999" }
    require(patch <= 99) { "Patch version exceeds the maximum allowed value of 999" }

    val buildCode = preRelease?.let {
        val parts = it.split('.')
        parts[0].toChannel().computeBuildCode(parts.getOrNull(1)?.toUInt() ?: 1u)
    } ?: Channel.STABLE.computeBuildCode()

    val versionCode = (major * (10.0).pow(7) + minor * (10.0).pow(4) + patch * (10.0).pow(2)).toUInt() + buildCode

    versionCode.also {
        require(versionCode <= 2_100_000_000u) {
            "Version code $versionCode exceeds the maximum allowed value of 2,100,000,000. Adjust your versioning strategy to generate a smaller version code. If you are building a non-Android application, you can apply a custom version code generator to resolve this problem."
        }
    }
}
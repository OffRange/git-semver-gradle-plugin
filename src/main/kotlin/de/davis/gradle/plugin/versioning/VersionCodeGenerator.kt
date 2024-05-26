package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import kotlin.math.pow

internal typealias VersionCodeGenerator = (version: Version) -> UInt

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
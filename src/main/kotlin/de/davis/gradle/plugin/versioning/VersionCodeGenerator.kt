package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import kotlin.math.pow

internal typealias VersionCodeGenerator = (version: Version) -> UInt

@Suppress("FunctionName")
fun AndroidVersionCodeGenerator(version: Version) = with(version) {
    val buildCode = preRelease?.let {
        val parts = it.split('.')
        Channel(parts[0]).computeBuildCode(parts.getOrNull(1)?.toUInt() ?: 1u)
    } ?: Channel.STABLE.computeBuildCode()

    val versionCode = (major * (10.0).pow(7) + minor * (10.0).pow(4) + patch * (10.0).pow(2)).toUInt() + buildCode

    versionCode.also {
        require(versionCode <= 2_100_000_000u) { "Version code exceeded maximum length of 2.100.000.000. Was $versionCode" }
    }

}
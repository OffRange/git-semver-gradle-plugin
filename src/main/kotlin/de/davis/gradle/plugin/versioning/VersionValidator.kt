package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version

internal typealias VersionValidator = (version: Version) -> Version

@Suppress("FunctionName")
fun AndroidVersionValidator(version: Version) = version.also { (major, minor, patch) ->
    // max stable version: 209.999.99
    require(major <= 210) { "Major version can not be greater then 210" }
    require(minor <= 999) { "Minor version can not be greater then 999" }
    require(patch <= 99) { "Patch version can not be greater then 99" }
}
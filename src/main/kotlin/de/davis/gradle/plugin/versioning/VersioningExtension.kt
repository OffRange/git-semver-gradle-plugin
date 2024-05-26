package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import org.gradle.api.model.ObjectFactory

open class VersioningExtension(objectFactory: ObjectFactory) {

    var channel by objectFactory.createGradleProperty<Channel>(Channel.STABLE)
    var useShortHash by objectFactory.createGradleProperty<Boolean>(true)
    var defaultIncrement by objectFactory.createGradleProperty<Inc>(Inc.MINOR)

    var versionCodeGenerator by objectFactory.createGradleProperty<VersionCodeGenerator>(::AndroidVersionCodeGenerator)

    companion object {
        const val EXTENSION_NAME = "versioning"
    }

    val Version.versionCode: UInt
        get() = versionCodeGenerator(this)
}
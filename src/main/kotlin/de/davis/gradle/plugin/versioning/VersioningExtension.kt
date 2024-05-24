package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.inc
import io.github.z4kn4fein.semver.nextMinor
import org.eclipse.jgit.api.Git
import org.gradle.api.model.ObjectFactory
import java.io.File

open class VersioningExtension(
    private val rootDir: File,
    objectFactory: ObjectFactory
) {

    var channel by objectFactory.createOptionalGradleProperty<Channel>()
    var useShortHash by objectFactory.createGradleProperty<Boolean>(true)
    var incrementBy by objectFactory.createGradleProperty<Inc>(Inc.PRE_RELEASE)
    var incrementByWhenChannelIsOlder by objectFactory.createGradleProperty<Inc>(Inc.PATCH)

    var versionValidator by objectFactory.createGradleProperty<VersionValidator>(::AndroidVersionValidator)
    var versionCodeGenerator by objectFactory.createGradleProperty<VersionCodeGenerator>(::AndroidVersionCodeGenerator)

    fun computeVersion(): Version {
        val final = with(Git.open(rootDir)) {
            val latestVersion = getLatestTag() ?: Version.min.nextMinor()

            when (val commitsSinceLastTag = commitsSinceLastTag()) {
                0 -> latestVersion
                else -> {
                    val inc = channel?.let { channel ->
                        latestVersion.preRelease?.let {
                            if (channel < Channel(it.split('.')[0])) incrementByWhenChannelIsOlder else null
                        }
                    } ?: incrementBy

                    latestVersion.inc(by = inc, preRelease = channel?.channelName)
                        .copy(buildMetadata = "dev.${commitsSinceLastTag}.${getLatestCommit(useShortHash)}")
                }
            }
        }

        return versionValidator(final)
    }

    companion object {
        const val EXTENSION_NAME = "versioning"
    }

    val Version.versionCode: UInt
        get() = versionCodeGenerator(this)
}
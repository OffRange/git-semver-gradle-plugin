package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import org.eclipse.jgit.api.Git
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import java.io.File

open class VersioningExtension(objectFactory: ObjectFactory, rootDir: File, logger: Logger) {

    /**
     * The channel to use for version computation.
     * Defaults to [Channel.STABLE].
     */
    var channel by objectFactory.createGradleProperty<Channel>(Channel.STABLE)

    /**
     * Whether to use a shortened version of the commit hash (7 characters) or the full hash.
     * Defaults to `true` (use shortened hash).
     */
    var useShortHash by objectFactory.createGradleProperty<Boolean>(true)

    /**
     * The default increment type to use when computing the next version.
     * Defaults to [Inc.MINOR].
     */
    var defaultIncrement by objectFactory.createGradleProperty<Inc>(Inc.MINOR)

    /**
     * The minimum version to use as the base version if the last tag name is less than this value.
     * If the specified value is a stable version (e.g., "1.0.0"), unstable versions like "1.0.0-alpha.1" are also accepted.
     * Defaults to [MIN_VERSION].
     */
    var minVersion by objectFactory.createGradleProperty<Version>(MIN_VERSION)

    /**
     * The function used to generate version codes for Android applications.
     * Defaults to [AndroidVersionCodeGenerator].
     */
    var versionCodeGenerator by objectFactory.createGradleProperty<VersionCodeGenerator>(::AndroidVersionCodeGenerator)

    /**
     * The computed version based on the Git repository state, channel, defaultIncrement, useShortHash, and minVersion.
     *
     * The version computation is performed using the [Git.computeVersion] function, which takes into account the latest tag,
     * commits since the last tag, the specified channel, default increment strategy, whether to use a short hash, and the minimum version.
     *
     * If the version computation fails due to any reason, a warning message is logged, and the computed version is set to null.
     * @see computeVersion
     */
    val computedVersion by lazy {
        runCatching {
            Git.open(rootDir).computeVersion(channel, defaultIncrement, useShortHash, minVersion)
        }.onFailure {
            logger.warn("Version could not be computed", it)
        }.getOrNull()
    }

    /**
     * The computed version code based on the [computedVersion] and the configured [versionCodeGenerator].
     *
     * If the [computedVersion] is not null, the version code is generated using the [versionCodeGenerator] function with the computed version as input.
     * If the [computedVersion] is null (e.g., due to a failure in version computation), the computed version code is set to 0.
     *
     * @see versionCodeGenerator
     */
    val computedVersionCode by lazy { computedVersion?.let { versionCodeGenerator(it) } ?: 0u }

    companion object {
        internal const val EXTENSION_NAME = "versioning"
    }
}
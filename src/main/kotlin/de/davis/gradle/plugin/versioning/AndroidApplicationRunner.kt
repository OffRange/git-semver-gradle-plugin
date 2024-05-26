package de.davis.gradle.plugin.versioning

import com.android.build.api.dsl.ApplicationBaseFlavor
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Executes the given action within the context of the Android [ApplicationExtension] if the Android Application plugin (`com.android.application`) is applied.
 *
 * @receiver The [Project] instance in which to execute the action.
 * @param action The action to be executed within the [ApplicationExtension] context.
 */
internal fun Project.runInAndroidAppExtension(action: ApplicationExtension.() -> Unit) {
    plugins.withId("com.android.application") {
        extensions.getByType<ApplicationExtension>().apply(action)
    }
}

/**
 * Applies versioning configuration to the Android application using the provided [VersioningExtension].
 *
 * This infix function sets the [ApplicationBaseFlavor.versionName] and [ApplicationBaseFlavor.versionCode] in the
 * [ApplicationExtension.defaultConfig] block of the Android application based on the computed version and version code
 * from the [VersioningExtension].
 *
 * @receiver The [ApplicationExtension] instance to which the versioning configuration will be applied.
 * @param extension The [VersioningExtension] instance containing the computed version and version code.
 */
infix fun ApplicationExtension.versionedBy(extension: VersioningExtension) {
    defaultConfig {
        applyVersioning(extension)
    }
}

/**
 * Applies versioning configuration to the Android application flavor using the provided [VersioningExtension].
 *
 * This function sets the [ApplicationBaseFlavor.versionName] and [ApplicationBaseFlavor.versionCode] of the
 * application flavor based on the computed version and version code from the [VersioningExtension].
 *
 * @receiver The [ApplicationBaseFlavor] instance to which the versioning configuration will be applied.
 * @param extension The [VersioningExtension] instance containing the computed version and version code.
 */
fun ApplicationBaseFlavor.applyVersioning(extension: VersioningExtension) {
    versionName = extension.computedVersion.toString()
    versionCode = extension.computedVersionCode.toInt()
}
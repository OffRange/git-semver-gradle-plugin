package de.davis.gradle.plugin.versioning

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
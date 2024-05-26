# Git Semantic Versioning Gradle Plugin

This Gradle plugin leverages Git to manage project versioning, adhering to semantic versioning principles. It provides
several customization options, especially useful for development and debugging purposes.

## Getting Started

Add the plugin to your `build.gradle.kts` file:

```kotlin
plugins {
    id("de.davis.git-semantic-versioning")
}
```

## Usage

### Development Version Customizations

> [!NOTE]
> Customization options like `defaultIncrement` and `channel` are intended for development versions where no Git tag has
> been set yet. These customizations help in displaying the correct version during development and debugging.

> [!NOTE]
> An active Git tag on the latest commit will always override these custom modifications.

#### Pre-Release Channels

For instance, if you've published a beta version `1.0.0-beta.1` and are working on a release candidate, set the channel
to `Channel.RC` to ensure the version updates to `1.0.0-rc<+build-metadata>`.

```kotlin
versioning {
    channel = Channel.RC
}
```

#### Incrementing Version Parts

If the release channel of the last version (determined by the latest git tag) and the `channel` property of the
extension is `Channel.STABLE` , or if the release channel of the last version
is newer than the release channel defined in the extension, the version is incremented by a strategy defined by
the `defaultIncrement` property

```kotlin
versioning {
    defaultIncrement = Inc.MINOR
}
```

#### Incrementing Version Parts

The `defaultIncrement` property is used to define which part of the version should be incremented under certain
conditions. Specifically, this property comes into play when:

1. The current git tag indicates that the project's version is stable, and there has been at least one commit since this
   tag. If the `channel` property is also set to `Channel.STABLE`, it implies that a new version is required.
   The `defaultIncrement` property will determine which version part (e.g., major, minor, or patch) should be
   incremented.

2. The release channel defined by the `channel` property is set to a "more unstable" version than the release channel of
   the current/latest git tag version. For example, if the latest git tag is "1.0.0-rc.2" and the `channel` parameter is
   set to `Channel.BETA`, the version will be incremented to "1.1.0-beta.1" (see the configuration below).

Here is how you can set the `defaultIncrement` property in your `versioning` configuration:

```kotlin
versioning {
    defaultIncrement = Inc.MINOR
}
```

#### Full Commit Hash in Version Names

If you prefer including the full commit hash in the version name, set the `useShortHash` property to `false`.

```kotlin
versioning {
    useShortHash = false
}
```

### Custom Version Code Generator

For Android projects, a default version code generator is provided. If you need a custom generator, specify it like
this:

```kotlin
versioning {
    versionCodeGenerator = { version ->
        computeUIntVersionCode(version)
    }
}
```

### Applying the Plugin to Android Projects

To use this plugin for versioning your Android app, follow these steps:

1. Apply the plugin in your `build.gradle.kts` or `build.gradle` file as described in the Setup section.
2. Remove or comment out the `versionName` and `versionCode` properties in the `defaultConfig` block within
   the `application` extension.

```kotlin
android {
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        // versionName = "1.0.0"
        // versionCode = 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }
}
```

The plugin will automatically set the appropriate version name and version code based on the Git tags.

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
> Customization options like `incrementBy` and `channel` are intended for development versions where no Git tag has
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

When moving towards a new stable version and implementing new features, increment the minor version number by
setting `incrementBy` to `Inc.MINOR`.

```kotlin
versioning {
    incrementBy = Inc.MINOR
}
```

#### Full Commit Hash in Version Names

If you prefer including the full commit hash in the version name, set the `useShortHash` property to `false`.

```kotlin
versioning {
    useShortHash = false
}
```

### Custom Version Validator

For systems with specific version requirements (e.g., Android apps with version codes less than 2100000000), set up a
custom version validator. This example ensures the major version does not exceed 210.

```kotlin
versioning {
    versionValidator = { (major, minor, patch, preRelease, buildMetadata) ->
        require(major < 210) { "Major version cannot be greater than 210" }
    }
}
```

To disable custom validation:

```kotlin
versioning {
    versionValidator = { it }
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

package de.davis.gradle.plugin.versioning

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import io.github.z4kn4fein.semver.Version
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.findByType

internal fun Project.createVersionProviderFile(versionCodeGenerator: VersionCodeGenerator, version: Version) {
    val funSpec = FunSpec.builder("getVersion")
        .returns(Version::class)
        .addStatement(
            "return %T.parse(%S)",
            Version::class.asClassName(),
            version
        )
        .build()

    val versionExtension = PropertySpec.builder("versionCode", UInt::class)
        .receiver(Version::class.asClassName())
        .getter(FunSpec.getterBuilder().addStatement("return %Lu", versionCodeGenerator(version)).build())
        .build()

    val fileSpec = FileSpec.builder("de.davis.versioning", "VersionProvider")
        .addFunction(funSpec)
        .addProperty(versionExtension)
        .build()


    val outputDir = layout.buildDirectory.dir("generated/versioning/kotlin").get().asFile
    fileSpec.writeTo(outputDir)

    runInAndroidAppExtension {
        sourceSets.forEach {
            it.kotlin.srcDir(outputDir)
        }
    }

    val sourceSets = project.extensions.findByType<SourceSetContainer>()
    sourceSets?.findByName("main")?.extensions?.findByType<SourceDirectorySet>()?.srcDir(outputDir)
}
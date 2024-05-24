plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.semantic.versioning)
    implementation(libs.jGit)
    implementation(libs.kotlinpoet)
    implementation(libs.android.gradleApi)
}

gradlePlugin {
    plugins {
        create("versioning") {
            id = "de.davis.git-semantic-versioning"
            implementationClass = "de.davis.gradle.plugin.versioning.VersioningPlugin"
        }
    }
}
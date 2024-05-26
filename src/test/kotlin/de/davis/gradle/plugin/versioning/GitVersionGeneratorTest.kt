package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.toVersion
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GitVersionGeneratorTest {

    @Test
    fun `computes minimum version when no commits`() {
        val tempFolder = createTempFolder()
        val git = Git.init().setDirectory(tempFolder.root).call()
        assertEquals(git.computeVersion(), MIN_VERSION)
    }

    @Test
    fun `computes version correctly when commits exist and no tags`() {
        val tempFolder = createTempFolder()
        val git = Git.init().setDirectory(tempFolder.root).call()
        val commitHash = git.commit().setAll(true).setMessage("Test").call().id.abbreviate(7).name()

        assertEquals("0.1.0+dev.1.$commitHash".toVersion(), git.computeVersion())
    }

    private fun createTempFolder() = TemporaryFolder().apply {
        create()

        newFile("settings.gradle.kts").writeText("rootProject.name = \"testproject\"")
        newFile("build.gradle.kts").writeText(
            """
                plugins{
                   id("de.davis.git-semantic-versioning")
                }
            """.trimIndent()
        )
    }
}
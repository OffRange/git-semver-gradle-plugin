package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.toVersion
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
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

    @Test
    fun `computes min version when no git initiated`() {
        val tempFolder = createTempFolder()

        val output = GradleRunner.create()
            .withProjectDir(tempFolder.root)
            .withPluginClasspath()
            .withArguments("printVersion")
            .build().output.lines().filter {
                it.contains("^\\|.*\\|".toRegex())
            }

        assertContains(
            output,
            "| Version Name : 0.1.0 |"
        )
        assertContains(
            output,
            "| Version Code : 10096 |"
        )
    }

    @Test
    fun `gets latest valid version tag name`() {
        fun Git.commitAndTag(tagName: String) {
            commit().setMessage("Test").call()
            tag().setName(tagName).call()
        }

        with(createTempFolder()) {
            val git = Git.init().setDirectory(root).call()
            git.commitAndTag("v1.0.0")
            newFile("File.txt")
            git.commitAndTag("1.0.0")
            newFile("File2.txt")
            git.commitAndTag("a.b.c")
            newFile("File3.txt")
            git.commitAndTag("1.2.0")

            assertEquals("1.2.0", git.getLatestVersionTagName())
        }
    }

    @Test
    fun `stable release tag outranks its own prerelease tags`() {
        fun Git.commitAndTag(tagName: String) {
            commit().setMessage("Test").call()
            tag().setName(tagName).call()
        }

        with(createTempFolder()) {
            val git = Git.init().setDirectory(root).call()
            git.commitAndTag("2.0.0-beta.1")
            newFile("File.txt")
            git.commitAndTag("2.0.0-rc.1")
            newFile("File2.txt")
            git.commitAndTag("2.0.0-rc.2")
            newFile("File3.txt")
            git.commitAndTag("2.0.0")

            // "2.0.0" is a prefix of "2.0.0-rc.2", so naive string sorting ranks
            // the prerelease tag above the stable one. Must compare by parsed
            // semver precedence instead.
            assertEquals("2.0.0", git.getLatestVersionTagName())
        }
    }

    private fun createTempFolder() = TemporaryFolder().apply {
        create()

        newFile("settings.gradle.kts").writeText("rootProject.name = \"testproject\"")
        newFile("build.gradle.kts").writeText(
            """
                plugins{
                   id("io.github.offrange.git-semantic-versioning")
                }
            """.trimIndent()
        )
    }
}
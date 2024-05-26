package de.davis.gradle.plugin.versioning

import de.davis.gradle.plugin.versioning.Channel.*
import de.davis.gradle.plugin.versioning.CommitInfo.Companion.LATEST
import io.github.z4kn4fein.semver.Inc
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class InternalVersionGeneratorTest {

    companion object {

        @JvmStatic
        fun provideTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(MIN_VERSION.toString(), LATEST, STABLE, MIN_VERSION.toString()),
            Arguments.of(MIN_VERSION.toString(), CommitInfo(5, "def456"), STABLE, "$MIN_VERSION+dev.5.def456"),
            Arguments.of("1.0.0", LATEST, STABLE, "1.0.0"),
            Arguments.of("1.0.0", CommitInfo(5, "def456"), STABLE, "1.1.0+dev.5.def456"),
            Arguments.of("1.0.0", CommitInfo(5, "def456"), ALPHA, "1.1.0-alpha+dev.5.def456"),
            Arguments.of("1.0.0-beta.1", LATEST, BETA, "1.0.0-beta.1"),
            Arguments.of("1.0.0-beta.1", CommitInfo(3, "jkl012"), BETA, "1.0.0-beta.2+dev.3.jkl012"),
            Arguments.of("1.0.0-beta.1", CommitInfo(3, "jkl012"), STABLE, "1.0.0+dev.3.jkl012"),
            Arguments.of("1.0.0-beta.1", CommitInfo(3, "jkl012"), RC, "1.0.0-rc+dev.3.jkl012"),
            Arguments.of("1.0.0-beta.1", CommitInfo(3, "jkl012"), ALPHA, "1.1.0-alpha+dev.3.jkl012"),
        )
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    fun `computeVersion computes correct version`(
        lastTagName: String,
        commitInfo: CommitInfo,
        channel: Channel,
        expectedVersion: String
    ) {
        val version = computeVersionInternal(lastTagName, commitInfo, channel, Inc.MINOR)

        assertEquals(expectedVersion, version.toString())
    }

    @Test
    fun `test computeVersion with MIN_VERSION`() {
        val actualVersion = computeVersionInternal("0.0.0", LATEST, STABLE, Inc.PRE_RELEASE)
        assertEquals("0.0.0", actualVersion.toString())
    }
}
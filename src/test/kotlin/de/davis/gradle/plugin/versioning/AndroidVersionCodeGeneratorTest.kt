package de.davis.gradle.plugin.versioning

import io.github.z4kn4fein.semver.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertTrue

class AndroidVersionCodeGeneratorTest {

    companion object {
        @JvmStatic
        fun versionCodeProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(Version(1, 0, 0), 10000096),
            Arguments.of(Version(1, 2, 3), 10020396),
            Arguments.of(Version(0, 1, 0, "alpha.1"), 10000),
            Arguments.of(Version(2, 3, 4, "beta"), 20030432),
            Arguments.of(Version(2, 3, 4, "beta.5"), 20030436),
            Arguments.of(Version(209, 999, 99), 2099999996),
        )
    }

    @ParameterizedTest
    @MethodSource("versionCodeProvider")
    fun `generates correct AndroidVersionCodeGenerator`(version: Version, expectedCode: Int) {
        val result = AndroidVersionCodeGenerator(version)
        assertEquals(expectedCode.toUInt(), result)
    }

    @Test
    fun `AndroidVersionCodeGenerator exceeds limit`() {
        val exception = assertThrows<IllegalArgumentException> {
            AndroidVersionCodeGenerator(Version(210, 0, 0))
        }
        assertTrue(exception.message!!.startsWith("Version code"))
    }
}
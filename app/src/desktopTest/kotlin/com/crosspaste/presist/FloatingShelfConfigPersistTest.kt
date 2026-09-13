package com.crosspaste.presist

import com.crosspaste.ui.floating.FloatingShelfConfig
import com.crosspaste.ui.floating.FloatingShelfPosition
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class FloatingShelfConfigPersistTest {

    @Test
    fun `saved free position survives a new persistence instance`() {
        val path = Files.createTempDirectory("floating-shelf-config-test").resolve("config.json")
        val expected =
            FloatingShelfConfig(
                enabled = true,
                position = FloatingShelfPosition.Free(-720, 420),
                autoHide = false,
                opacity = 0.8f,
            )

        FloatingShelfConfigPersist(path).save(expected)

        assertEquals(expected, FloatingShelfConfigPersist(path).read())
    }
}

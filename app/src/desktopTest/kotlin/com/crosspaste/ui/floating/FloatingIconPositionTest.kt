package com.crosspaste.ui.floating

import java.awt.Point
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals

class FloatingIconPositionTest {

    private val secondaryScreen = Rectangle(-1920, 40, 1920, 1040)

    @Test
    fun `free position is preserved while visible`() {
        assertEquals(
            Point(-1200, 300),
            FloatingIconPosition.clampToVisibleBounds(
                requested = Point(-1200, 300),
                width = 60,
                height = 60,
                bounds = secondaryScreen,
            ),
        )
    }

    @Test
    fun `position is clamped back onto screen after display changes`() {
        assertEquals(
            Point(-1920, 1020),
            FloatingIconPosition.clampToVisibleBounds(
                requested = Point(-2500, 1400),
                width = 60,
                height = 60,
                bounds = secondaryScreen,
            ),
        )
    }
}

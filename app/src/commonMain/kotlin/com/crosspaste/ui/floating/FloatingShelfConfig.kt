package com.crosspaste.ui.floating

import kotlinx.serialization.Serializable

@Serializable
data class FloatingShelfConfig(
    val enabled: Boolean = true,
    val position: FloatingShelfPosition = FloatingShelfPosition.LEFT,
    val autoHide: Boolean = true,
    val autoHideDelayMs: Long = 2000,
    val opacity: Float = 0.9f,
)

@Serializable
sealed class FloatingShelfPosition {
    @Serializable
    data object LEFT : FloatingShelfPosition()

    @Serializable
    data object RIGHT : FloatingShelfPosition()

    @Serializable
    data class Free(
        val x: Int,
        val y: Int,
    ) : FloatingShelfPosition()

    companion object {
        fun fromInt(value: Int): FloatingShelfPosition =
            when (value) {
                0 -> LEFT
                1 -> RIGHT
                2 -> Free(0, 0)
                else -> LEFT
            }

        fun toInt(position: FloatingShelfPosition): Int =
            when (position) {
                LEFT -> 0
                RIGHT -> 1
                is Free -> 2
            }
    }
}

data class FloatingShelfWindowInfo(
    val show: Boolean = false,
    val position: FloatingShelfPosition = FloatingShelfPosition.LEFT,
    val isAutoHiding: Boolean = false,
)

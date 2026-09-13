package com.crosspaste.ui.floating

import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit

object FloatingIconPosition {

    private const val EDGE_PADDING_PX = 16

    fun currentUsableBounds(): Rectangle {
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val pointer = runCatching { MouseInfo.getPointerInfo()?.location }.getOrNull()
        val configuration =
            environment.screenDevices
                .map { it.defaultConfiguration }
                .firstOrNull { pointer != null && it.bounds.contains(pointer) }
                ?: environment.defaultScreenDevice.defaultConfiguration
        return usableBounds(configuration)
    }

    fun iconLocation(
        position: FloatingShelfPosition,
        iconSizePx: Int,
    ): Point {
        val bounds = currentUsableBounds()
        val defaultY = bounds.y + bounds.height - iconSizePx - EDGE_PADDING_PX
        val requested =
            when (position) {
                FloatingShelfPosition.LEFT -> Point(bounds.x + EDGE_PADDING_PX, defaultY)
                FloatingShelfPosition.RIGHT ->
                    Point(bounds.x + bounds.width - iconSizePx - EDGE_PADDING_PX, defaultY)
                is FloatingShelfPosition.Free -> Point(position.x, position.y)
            }
        return clampToVisibleBounds(requested, iconSizePx, iconSizePx, bounds)
    }

    fun shelfLocation(
        position: FloatingShelfPosition,
        shelfWidthPx: Int,
        shelfHeightPx: Int,
    ): Point {
        val bounds = currentUsableBounds()
        val centeredY = bounds.y + (bounds.height - shelfHeightPx) / 2
        val requested =
            when (position) {
                FloatingShelfPosition.LEFT -> Point(bounds.x + EDGE_PADDING_PX, centeredY)
                FloatingShelfPosition.RIGHT ->
                    Point(bounds.x + bounds.width - shelfWidthPx - EDGE_PADDING_PX, centeredY)
                is FloatingShelfPosition.Free -> {
                    val iconOnLeftHalf = position.x + shelfWidthPx / 2 < bounds.centerX
                    val x =
                        if (iconOnLeftHalf) {
                            position.x + EDGE_PADDING_PX
                        } else {
                            position.x - shelfWidthPx + EDGE_PADDING_PX
                        }
                    Point(x, position.y - shelfHeightPx / 2)
                }
            }
        return clampToVisibleBounds(requested, shelfWidthPx, shelfHeightPx, bounds)
    }

    internal fun clampToVisibleBounds(
        requested: Point,
        width: Int,
        height: Int,
        bounds: Rectangle,
    ): Point =
        Point(
            requested.x.coerceIn(bounds.x, (bounds.x + bounds.width - width).coerceAtLeast(bounds.x)),
            requested.y.coerceIn(bounds.y, (bounds.y + bounds.height - height).coerceAtLeast(bounds.y)),
        )

    private fun usableBounds(configuration: GraphicsConfiguration): Rectangle {
        val bounds = Rectangle(configuration.bounds)
        val insets =
            runCatching { Toolkit.getDefaultToolkit().getScreenInsets(configuration) }.getOrNull()
                ?: return bounds
        return Rectangle(
            bounds.x + insets.left,
            bounds.y + insets.top,
            (bounds.width - insets.left - insets.right).coerceAtLeast(1),
            (bounds.height - insets.top - insets.bottom).coerceAtLeast(1),
        )
    }
}

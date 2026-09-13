package com.crosspaste.ui.floating

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Content_paste
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.platform.Platform
import com.crosspaste.ui.theme.AppUISize
import org.koin.compose.koinInject
import java.awt.MouseInfo
import java.awt.Point

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FloatingIconWindow() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val platform = koinInject<Platform>()
    val config by appWindowManager.floatingShelfConfig.collectAsState()

    if (!config.enabled) return

    val windowSize = AppUISize.huge
    val windowSizePx = with(LocalDensity.current) { windowSize.roundToPx() }
    val iconLocation =
        remember(config.position, windowSizePx) {
            FloatingIconPosition.iconLocation(config.position, windowSizePx)
        }
    val windowState =
        remember(config.position, iconLocation) {
            WindowState(
                size = DpSize(windowSize, windowSize),
                position = WindowPosition(iconLocation.x.dp, iconLocation.y.dp),
            )
        }

    var hovered by remember { mutableStateOf(false) }
    var externalDragActive by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (hovered || externalDragActive) 1.08f else 1f,
        label = "floating_icon_scale",
    )
    val dropTarget = rememberShelfDropTarget { externalDragActive = it }

    Window(
        onCloseRequest = {},
        state = windowState,
        title = "CrossPaste Shelf",
        transparent = !platform.isLinux(),
        undecorated = true,
        alwaysOnTop = true,
        resizable = false,
    ) {
        var pointerStart by remember { mutableStateOf<Point?>(null) }
        var windowStart by remember { mutableStateOf<Point?>(null) }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = dropTarget,
                    ).pointerInput(window) {
                        detectDragGestures(
                            onDragStart = {
                                pointerStart = MouseInfo.getPointerInfo()?.location
                                windowStart = window.location
                            },
                            onDragEnd = {
                                appWindowManager.updateFloatingShelfPosition(
                                    FloatingShelfPosition.Free(window.x, window.y),
                                )
                                pointerStart = null
                                windowStart = null
                            },
                            onDragCancel = {
                                pointerStart = null
                                windowStart = null
                            },
                            onDrag = { change, _ ->
                                val pointer = MouseInfo.getPointerInfo()?.location
                                val originPointer = pointerStart
                                val originWindow = windowStart
                                if (pointer != null && originPointer != null && originWindow != null) {
                                    window.setLocation(
                                        originWindow.x + pointer.x - originPointer.x,
                                        originWindow.y + pointer.y - originPointer.y,
                                    )
                                    change.consume()
                                }
                            },
                        )
                    }.onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false }
                    .padding(AppUISize.tiny3X)
                    .scale(scale)
                    .background(
                        color =
                            if (externalDragActive) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        shape = RoundedCornerShape(AppUISize.large2X),
                    ).clickable { appWindowManager.toggleFloatingShelf() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Content_paste,
                contentDescription = "CrossPaste Shelf",
                modifier = Modifier.size(AppUISize.xxLarge),
                tint = Color.White,
            )
        }
    }
}

package com.crosspaste.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Content_paste
import com.crosspaste.app.DesktopAppWindowManager
import org.koin.compose.koinInject

@Composable
fun FloatingIconWindow() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val iconSize = 24.dp
    val padding = 16.dp

    val displayBounds =
        androidx.compose.ui.geometry
            .Rect(0f, 0f, 1920f, 1080f)

    val windowPosition =
        WindowPosition(
            x = ((displayBounds.right - iconSize.value - padding.value).dp),
            y = ((displayBounds.bottom - iconSize.value - padding.value).dp),
        )

    Window(
        onCloseRequest = { /* Do nothing - icon window should not be closed */ },
        visible = true,
        state = WindowState(size = DpSize(iconSize, iconSize), position = windowPosition),
        title = "",
        transparent = true,
        undecorated = true,
        alwaysOnTop = true,
        resizable = false,
    ) {
        WindowDraggableArea {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable { appWindowManager.showFloatingShelf() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Content_paste,
                    contentDescription = "Floating Icon",
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

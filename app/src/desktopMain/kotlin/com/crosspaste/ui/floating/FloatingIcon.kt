package com.crosspaste.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Content_paste
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.ui.theme.AppUISize
import org.koin.compose.koinInject

@Composable
fun FloatingIcon() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val config by appWindowManager.floatingShelfConfig.collectAsState()

    if (!config.enabled) return

    val iconSize = AppUISize.iconLarge

    Window(
        onCloseRequest = { /* Do nothing - keep icon visible */ },
        visible = true,
        state =
            WindowState(
                size = DpSize(iconSize, iconSize),
                position = WindowPosition.Aligned(Alignment.BottomEnd),
            ),
        title = "",
        transparent = true,
        undecorated = true,
        alwaysOnTop = true,
    ) {
        Box(
            modifier =
                Modifier
                    .size(iconSize)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { appWindowManager.showFloatingShelf() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Content_paste,
                contentDescription = "Paste",
                modifier = Modifier.size(iconSize / 2),
                tint = Color.White,
            )
        }
    }
}

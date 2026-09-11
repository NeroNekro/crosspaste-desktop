package com.crosspaste.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.awt.Toolkit

@Composable
fun FloatingIconWindow() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val platform = koinInject<Platform>()
    val config by appWindowManager.floatingShelfConfig.collectAsState()

    if (!config.enabled) return

    val iconSize = AppUISize.iconLarge
    val padding = 20

    val iconSizePx = (iconSize.value * Toolkit.getDefaultToolkit().screenResolution * 0.75f).toInt()
    val (x, y) = getIconPosition(platform, iconSizePx, padding)

    Window(
        onCloseRequest = { },
        visible = true,
        state =
            WindowState(
                size =
                    androidx.compose.ui.unit
                        .DpSize(iconSize, iconSize),
                position = WindowPosition(x.dp, y.dp),
            ),
        title = "",
        transparent = true,
        undecorated = true,
        alwaysOnTop = true,
        resizable = false,
    ) {
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
                modifier = Modifier.size(iconSize / 2),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

private fun getIconPosition(
    platform: Platform,
    iconSize: Int,
    padding: Int,
): Pair<Int, Int> {
    val (screenWidth, screenHeight) = getScreenSize(platform)
    val x = screenWidth - iconSize - padding
    val y = screenHeight - iconSize - padding
    return Pair(x, y)
}

private fun getScreenSize(platform: Platform): Pair<Int, Int> {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    return Pair(screenSize.width, screenSize.height)
}

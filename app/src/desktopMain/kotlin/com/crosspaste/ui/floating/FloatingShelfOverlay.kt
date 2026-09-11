package com.crosspaste.ui.floating

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.app.WindowTrigger
import com.crosspaste.paste.PasteData
import com.crosspaste.ui.LocalDesktopAppSizeValueState
import com.crosspaste.ui.model.PasteSearchViewModel
import com.crosspaste.ui.theme.AppUISize
import org.koin.compose.koinInject

@Composable
fun FloatingShelfOverlay() {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val pasteSearchViewModel = koinInject<PasteSearchViewModel>()
    val appSizeValue = LocalDesktopAppSizeValueState.current

    val shelfInfo by appWindowManager.floatingShelfWindowInfo.collectAsState()
    val searchResults by pasteSearchViewModel.searchResults.collectAsState()

    if (!shelfInfo.show) return

    Window(
        onCloseRequest = { appWindowManager.hideFloatingShelf() },
        visible = shelfInfo.show,
        state = calculateFloatingShelfWindowState(appWindowManager, appSizeValue),
        title = "CrossPaste Shelf",
        transparent = true,
        undecorated = true,
        alwaysOnTop = true,
        resizable = false,
    ) {
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(AppUISize.medium)
                    .width(appSizeValue.sidePasteSize.width + 16.dp)
                    .heightIn(max = 500.dp),
            contentAlignment = Alignment.Center,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(searchResults) { pasteData: PasteData ->
                    ShelfItem(
                        pasteData = pasteData,
                        onClick = {
                            appWindowManager.hideFloatingShelf()
                            appWindowManager.showSearchWindow(WindowTrigger.SYSTEM)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ShelfItem(
    pasteData: PasteData,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() }
                .background(Color.Transparent),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO: Add proper icon based on paste type
            Box(
                modifier =
                    Modifier
                        .size(AppUISize.iconSmall)
                        .background(Color(0xFFE0E0E0)),
            )
            Spacer(modifier = Modifier.width(AppUISize.medium))
            Text(
                text = pasteData.getTypeName(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun calculateFloatingShelfWindowState(
    appWindowManager: DesktopAppWindowManager,
    appSizeValue: com.crosspaste.app.DesktopAppSizeValue,
): androidx.compose.ui.window.WindowState {
    val config = appWindowManager.floatingShelfConfig.value
    val position = config.position
    val shelfSize =
        androidx.compose.ui.unit.DpSize(
            appSizeValue.sidePasteSize.width + 16.dp,
            500.dp,
        )

    val displayBounds =
        androidx.compose.ui.geometry
            .Rect(0f, 0f, 1920f, 1080f)

    val windowPosition =
        when (position) {
            FloatingShelfPosition.LEFT -> {
                androidx.compose.ui.window.WindowPosition(
                    x = (displayBounds.left + 20).dp,
                    y = ((displayBounds.top + displayBounds.height / 2 - (shelfSize.height / 2).value).dp),
                )
            }
            FloatingShelfPosition.RIGHT -> {
                androidx.compose.ui.window.WindowPosition(
                    x = ((displayBounds.right - shelfSize.width.value - 20).dp),
                    y = ((displayBounds.top + displayBounds.height / 2 - (shelfSize.height / 2).value).dp),
                )
            }
            is FloatingShelfPosition.Free -> {
                androidx.compose.ui.window.WindowPosition(
                    x = position.x.dp,
                    y = position.y.dp,
                )
            }
        }

    return androidx.compose.ui.window.WindowState(
        size = shelfSize,
        position = windowPosition,
    )
}

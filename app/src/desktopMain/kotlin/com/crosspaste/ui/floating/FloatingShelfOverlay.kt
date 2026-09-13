package com.crosspaste.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Close
import com.composables.icons.materialsymbols.rounded.Content_paste
import com.crosspaste.app.AppInfo
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.i18n.GlobalCopywriter
import com.crosspaste.paste.DesktopPasteMenuService
import com.crosspaste.paste.DesktopWriteTransferable
import com.crosspaste.paste.PasteData
import com.crosspaste.paste.TransferableConsumer
import com.crosspaste.paste.TransferableProducer
import com.crosspaste.paste.getIconData
import com.crosspaste.paste.item.PasteColor
import com.crosspaste.paste.item.PasteFiles
import com.crosspaste.paste.item.PasteText
import com.crosspaste.paste.item.PasteUrl
import com.crosspaste.platform.Platform
import com.crosspaste.sync.SyncManager
import com.crosspaste.ui.theme.AppUISize
import com.crosspaste.utils.getFileUtils
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject

private val SHELF_WIDTH = 360.dp
private val SHELF_HEIGHT = 480.dp
private const val SHELF_ITEM_LIMIT = 30

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FloatingShelfOverlay() {
    val appInfo = koinInject<AppInfo>()
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val copywriter = koinInject<GlobalCopywriter>()
    val pasteConsumer = koinInject<TransferableConsumer>()
    val pasteSearchViewModel = koinInject<com.crosspaste.ui.model.PasteSearchViewModel>()
    val platform = koinInject<Platform>()
    val syncManager = koinInject<SyncManager>()

    val config by appWindowManager.floatingShelfConfig.collectAsState()
    val searchResults by pasteSearchViewModel.searchResults.collectAsState()
    val devices by syncManager.realTimeSyncRuntimeInfos.collectAsState()

    val density = LocalDensity.current
    val shelfWidthPx = with(density) { SHELF_WIDTH.roundToPx() }
    val shelfHeightPx = with(density) { SHELF_HEIGHT.roundToPx() }
    val shelfLocation =
        remember(config.position, shelfWidthPx, shelfHeightPx) {
            FloatingIconPosition.shelfLocation(config.position, shelfWidthPx, shelfHeightPx)
        }
    val windowState =
        remember(config.position, shelfLocation) {
            WindowState(
                size = DpSize(SHELF_WIDTH, SHELF_HEIGHT),
                position = WindowPosition(shelfLocation.x.dp, shelfLocation.y.dp),
            )
        }

    Window(
        onCloseRequest = { appWindowManager.hideFloatingShelf() },
        state = windowState,
        title = "CrossPaste Shelf",
        transparent = !platform.isLinux(),
        undecorated = true,
        alwaysOnTop = true,
        resizable = false,
    ) {
        var externalDragActive by remember { mutableStateOf(false) }
        val dropTarget = rememberShelfDropTarget { externalDragActive = it }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = dropTarget,
                    ).onPreviewKeyEvent { event ->
                        when {
                            event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                                appWindowManager.hideFloatingShelf()
                                true
                            }
                            event.type == KeyEventType.KeyDown &&
                                event.key == Key.V &&
                                (event.isCtrlPressed || event.isMetaPressed) -> {
                                importClipboardIntoShelf(appWindowManager, pasteConsumer)
                                true
                            }
                            else -> false
                        }
                    },
            shape = RoundedCornerShape(AppUISize.medium),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AppUISize.tiny4X,
            shadowElevation = AppUISize.tiny3X,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ShelfHeader(
                        title = copywriter.getText("pasteboard"),
                        onClose = appWindowManager::hideFloatingShelf,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (searchResults.isEmpty()) {
                        EmptyShelf(copywriter.getText("drop_to_clipboard_and_sync"))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                androidx.compose.foundation.layout
                                    .PaddingValues(vertical = AppUISize.tiny),
                        ) {
                            items(
                                items = searchResults.take(SHELF_ITEM_LIMIT),
                                key = PasteData::id,
                            ) { pasteData ->
                                val source =
                                    when {
                                        pasteData.appInstanceId == appInfo.appInstanceId ->
                                            pasteData.source
                                                ?: appInfo.userName
                                        else ->
                                            devices
                                                .firstOrNull { it.appInstanceId == pasteData.appInstanceId }
                                                ?.let { it.noteName ?: it.deviceName }
                                                ?: pasteData.source
                                    }
                                CompactShelfItem(pasteData, source)
                            }
                        }
                    }
                }

                if (externalDragActive) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Content_paste,
                                contentDescription = null,
                                modifier = Modifier.size(AppUISize.xxxxLarge),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.height(AppUISize.medium))
                            Text(
                                text = copywriter.getText("drop_to_clipboard_and_sync"),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelfHeader(
    title: String,
    onClose: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppUISize.huge)
                .padding(start = AppUISize.medium, end = AppUISize.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyShelf(hint: String) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(AppUISize.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.Content_paste,
            contentDescription = null,
            modifier = Modifier.size(AppUISize.xxxxLarge),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(AppUISize.medium))
        Text(
            text = hint,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CompactShelfItem(
    pasteData: PasteData,
    source: String?,
) {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val pasteMenuService = koinInject<DesktopPasteMenuService>()
    val pasteProducer = koinInject<TransferableProducer>()
    val iconData = pasteData.getType().getIconData()
    val title = remember(pasteData) { pasteData.shelfTitle() }
    val subtitle =
        remember(pasteData, source) {
            listOfNotNull(
                source?.takeIf(String::isNotBlank),
                pasteData.size.takeIf { it > 0 }?.let(getFileUtils()::formatBytes),
            ).joinToString(" · ")
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .dragAndDropSource { offset ->
                    DragAndDropTransferData(
                        transferable =
                            DragAndDropTransferable(
                                runBlocking {
                                    pasteProducer.produce(
                                        pasteData = pasteData,
                                        localOnly = true,
                                        primary = true,
                                    ) as? DesktopWriteTransferable ?: DesktopWriteTransferable(LinkedHashMap())
                                },
                            ),
                        supportedActions = listOf(DragAndDropTransferAction.Copy),
                        dragDecorationOffset = offset,
                    )
                }.clickable {
                    pasteMenuService.copyPasteData(pasteData)
                    appWindowManager.hideFloatingShelf()
                }.padding(horizontal = AppUISize.medium, vertical = AppUISize.small3X),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconData.IconContent()
        Spacer(modifier = Modifier.width(AppUISize.small2X))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppUISize.tiny4X))
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun PasteData.shelfTitle(): String {
    val item = pasteAppearItem
    return when (item) {
        is PasteText ->
            item
                .previewText()
                .lineSequence()
                .firstOrNull()
                ?.trim()
                .orEmpty()
        is PasteUrl -> item.getTitle()?.takeIf(String::isNotBlank) ?: item.url
        is PasteFiles -> {
            val names = item.relativePathList.map { it.replace('\\', '/').substringAfterLast('/') }
            when {
                names.isEmpty() -> getTypeName()
                names.size == 1 -> names.first()
                else -> "${names.first()} +${names.size - 1}"
            }
        }
        is PasteColor -> item.toHexString()
        else -> getTypeName()
    }.ifBlank { getTypeName() }
}

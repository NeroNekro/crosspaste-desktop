package com.crosspaste.ui.floating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.paste.DesktopReadTransferable
import com.crosspaste.paste.PasteSourceContext
import com.crosspaste.paste.TransferableConsumer
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import java.awt.Toolkit
import java.awt.datatransfer.Transferable

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun rememberShelfDropTarget(onDraggingChanged: (Boolean) -> Unit): DragAndDropTarget {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val pasteConsumer = koinInject<TransferableConsumer>()

    return remember(appWindowManager, pasteConsumer, onDraggingChanged) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                onDraggingChanged(true)
            }

            override fun onEnded(event: DragAndDropEvent) {
                onDraggingChanged(false)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                onDraggingChanged(false)
                return importTransferable(
                    transferable = event.awtTransferable,
                    source = appWindowManager.getCurrentActiveAppName(),
                    pasteConsumer = pasteConsumer,
                    dragAndDrop = true,
                )
            }
        }
    }
}

internal fun importClipboardIntoShelf(
    appWindowManager: DesktopAppWindowManager,
    pasteConsumer: TransferableConsumer,
): Boolean {
    val contents =
        runCatching { Toolkit.getDefaultToolkit().systemClipboard.getContents(null) }.getOrNull()
            ?: return false
    return importTransferable(
        transferable = contents,
        source = appWindowManager.getCurrentActiveAppName(),
        pasteConsumer = pasteConsumer,
        dragAndDrop = false,
    )
}

private fun importTransferable(
    transferable: Transferable,
    source: String?,
    pasteConsumer: TransferableConsumer,
    dragAndDrop: Boolean,
): Boolean =
    runBlocking {
        pasteConsumer
            .consume(
                DesktopReadTransferable(transferable),
                PasteSourceContext(
                    source = source,
                    remote = false,
                    dragAndDrop = dragAndDrop,
                ),
            ).isSuccess
    }
